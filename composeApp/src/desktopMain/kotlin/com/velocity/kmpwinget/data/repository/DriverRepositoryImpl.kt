package com.velocity.kmpwinget.data.repository

import com.velocity.kmpwinget.data.datasource.WindowsDriverStoreScanner
import com.velocity.kmpwinget.data.datasource.WindowsUpdateDriver
import com.velocity.kmpwinget.data.datasource.WindowsUpdateDriverScanner
import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.repository.IDriverRepository
import com.velocity.kmpwinget.domain.service.VersionComparator
import com.velocity.kmpwinget.platform.WindowsUpdateLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class DriverRepositoryImpl : IDriverRepository {

    private var cachedDrivers: List<DriverPackage> = emptyList()
    private var cachedUpdates: List<WindowsUpdateDriver> = emptyList()
    private var lastUpdateSearchAt: Long = 0L

    private val updateSearchTtlMs = 10 * 60 * 1000L

    override fun getInstalledDrivers(forceRefresh: Boolean): Flow<List<DriverPackage>> = flow {
        if (!forceRefresh && cachedDrivers.isNotEmpty()) {
            emit(cachedDrivers)
            return@flow
        }

        val installed = WindowsDriverStoreScanner.scanInstalledDrivers()
        cachedDrivers = installed
        emit(installed)
    }.flowOn(Dispatchers.IO)

    override suspend fun resolveDriverUpdates(drivers: List<DriverPackage>): List<DriverPackage> = withContext(Dispatchers.IO) {
        if (drivers.isEmpty()) return@withContext emptyList()

        val now = System.currentTimeMillis()
        val updates = if (lastUpdateSearchAt > 0 && now - lastUpdateSearchAt < updateSearchTtlMs) {
            cachedUpdates
        } else {
            WindowsUpdateDriverScanner.searchDriverUpdates().also {
                cachedUpdates = it
                lastUpdateSearchAt = System.currentTimeMillis()
            }
        }

        val resolved = drivers.map { driver ->
            val cleanDriver = driver.copy(availableVersion = null, availableUpdateTitle = null)
            val update = findUpdateForDriver(driver, updates)
            if (update?.version != null) {
                cleanDriver.copy(
                    availableVersion = update.version,
                    availableUpdateTitle = update.title
                )
            } else {
                cleanDriver
            }
        }
        cachedDrivers = resolved
        resolved
    }

    override suspend fun updateDriver(driver: DriverPackage): Flow<OperationResult> = flow {
        val status = if (driver.hasUpdate) {
            "Opening Windows Update for ${driver.displayName} ${driver.availableVersion}"
        } else {
            "Triggering a Windows Update driver scan for ${driver.displayName}"
        }

        emit(
            OperationResult.Loading(
                title = "Windows Update",
                message = status,
                currentProgress = 0.5f
            )
        )

        if (WindowsUpdateLauncher.openDriverUpdates()) {
            val message = if (driver.hasUpdate) {
                "Windows Update was opened. Review the driver update for ${driver.displayName} (${driver.availableVersion})."
            } else {
                "Windows Update was opened and a driver scan was requested for ${driver.displayName}."
            }
            emit(OperationResult.Success(message = message))
        } else {
            emit(
                OperationResult.Error(
                    message = "Could not open Windows Update on this system.",
                    details = "Open Settings > Windows Update manually to check for driver updates."
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteDriver(driver: DriverPackage): Flow<OperationResult> = flow {
        emit(
            OperationResult.Loading(
                title = "Deleting Driver Package",
                message = "Uninstalling ${driver.publishedName} (${driver.displayName}) from the Windows Driver Store...",
                currentProgress = 0.2f
            )
        )

        val logOutput = StringBuilder()
        try {
            val process = ProcessBuilder("pnputil.exe", "/delete-driver", driver.publishedName, "/uninstall")
                .redirectErrorStream(true)
                .start()

            BufferedReader(InputStreamReader(process.inputStream, Charset.defaultCharset())).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val outputLine = line ?: continue
                    if (outputLine.isBlank()) continue
                    logOutput.appendLine(outputLine)
                    emit(
                        OperationResult.Loading(
                            title = "Deleting Driver Package",
                            message = outputLine.take(80),
                            currentProgress = 0.6f,
                            logOutput = logOutput.toString()
                        )
                    )
                }
            }
            process.waitFor()

            val finalLog = logOutput.toString()
            if (process.exitValue() == 0) {
                cachedDrivers = cachedDrivers.filterNot {
                    it.publishedName.equals(driver.publishedName, ignoreCase = true)
                }
                emit(
                    OperationResult.Success(
                        message = "Driver package ${driver.publishedName} successfully deleted and uninstalled.",
                        logOutput = finalLog
                    )
                )
            } else {
                emit(
                    OperationResult.Error(
                        message = "Failed to delete driver package ${driver.publishedName}",
                        details = "This operation requires administrator privileges.",
                        logOutput = finalLog
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                OperationResult.Error(
                    message = "Error deleting driver: ${e.message}",
                    logOutput = logOutput.toString()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun scanPnpDevices(): OperationResult = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("pnputil.exe", "/scan-devices")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader(Charset.defaultCharset()).use { it.readText() }
            process.waitFor()

            if (process.exitValue() == 0) {
                OperationResult.Success("Windows Plug and Play device scan completed successfully.")
            } else {
                OperationResult.Error(
                    message = "Windows Plug and Play device scan failed.",
                    details = output.lines().filter { it.isNotBlank() }.takeLast(4).joinToString("\n")
                )
            }
        } catch (e: Exception) {
            OperationResult.Error("Device scan failed: ${e.message}")
        }
    }

    /**
     * Matches a Windows Update driver package to an installed Driver Store package.
     *
     * Windows Update titles follow the pattern "<Provider> - <Class> - <Version>", so a
     * match requires both the vendor and the device class to be present in the title.
     */
    private fun findUpdateForDriver(
        driver: DriverPackage,
        updates: List<WindowsUpdateDriver>
    ): WindowsUpdateDriver? {
        val providerTokens = providerTokens(driver.providerName)
        if (providerTokens.isEmpty()) return null

        val classTokens = classTokens(driver.driverClass)
        return updates.asSequence()
            .filter { update ->
                val title = normalize(update.title)
                val version = update.version
                !version.isNullOrBlank() &&
                    providerTokens.any { title.contains(it) } &&
                    (classTokens.isEmpty() || classTokens.any { title.contains(it) }) &&
                    VersionComparator.isNewer(driver.driverVersion, version)
            }
            .reduceOrNull { best, candidate ->
                val bestVersion = best.version.orEmpty()
                val candidateVersion = candidate.version.orEmpty()
                if (VersionComparator.compare(candidateVersion, bestVersion) > 0) candidate else best
            }
    }

    private fun providerTokens(provider: String): List<String> {
        val normalized = normalize(provider)
        if (normalized.length < 4) return emptyList()

        val tokens = mutableListOf(normalized)
        val lower = provider.lowercase()
        when {
            lower.contains("intel") -> tokens += "intel"
            lower.contains("amd") || lower.contains("advanced micro") -> tokens += "amd"
            lower.contains("nvidia") -> tokens += "nvidia"
            lower.contains("realtek") -> tokens += "realtek"
            lower.contains("qualcomm") -> tokens += "qualcomm"
            lower.contains("broadcom") -> tokens += "broadcom"
            lower.contains("mediatek") -> tokens += "mediatek"
            lower.contains("synaptics") -> tokens += "synaptics"
        }
        return tokens.filter { it.length >= 4 }.distinct()
    }

    private fun classTokens(driverClass: DriverClass): List<String> {
        return when (driverClass) {
            DriverClass.DISPLAY -> listOf("display", "graphics", "video")
            DriverClass.NETWORK -> listOf("network", "net", "wifi", "wlan", "ethernet", "wireless")
            DriverClass.AUDIO -> listOf("audio", "media", "sound")
            DriverClass.BLUETOOTH -> listOf("bluetooth")
            DriverClass.SYSTEM -> listOf("system", "chipset", "processor", "firmware")
            DriverClass.PERIPHERALS -> listOf("hid", "keyboard", "mouse", "usb", "input", "touchpad")
            DriverClass.OTHER -> emptyList()
        }
    }

    private fun normalize(value: String): String {
        return value.lowercase().replace(Regex("[^a-z0-9]"), "")
    }
}
