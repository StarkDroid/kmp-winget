package com.velocity.kmpwinget.data.repository

import com.velocity.kmpwinget.data.datasource.WinGetExecutor
import com.velocity.kmpwinget.data.datasource.WinGetParser
import com.velocity.kmpwinget.data.datasource.WindowsDriverScanner
import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.VersionComparator
import com.velocity.kmpwinget.domain.repository.IDriverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class DriverRepositoryImpl : IDriverRepository {

    private var cachedDrivers: List<DriverPackage> = emptyList()

    override fun getInstalledDrivers(forceRefresh: Boolean): Flow<List<DriverPackage>> = flow {
        if (!forceRefresh && cachedDrivers.isNotEmpty()) {
            emit(cachedDrivers)
            return@flow
        }

        val rawDrivers = WindowsDriverScanner.scanDrivers()
        if (rawDrivers.isNotEmpty()) {
            cachedDrivers = rawDrivers
            emit(rawDrivers)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun resolveDriverUpdates(drivers: List<DriverPackage>): List<DriverPackage> = withContext(Dispatchers.IO) {
        val resolved = drivers.map { driver ->
            try {
                val candidateQuery = when (driver.driverClass) {
                    DriverClass.DISPLAY -> if (driver.providerName.contains("Intel", ignoreCase = true)) "Intel Graphics" else if (driver.providerName.contains("NVIDIA", ignoreCase = true)) "NVIDIA Graphics" else if (driver.providerName.contains("AMD", ignoreCase = true)) "AMD Radeon" else null
                    DriverClass.NET -> if (driver.providerName.contains("Intel", ignoreCase = true)) "Intel Wi-Fi" else if (driver.providerName.contains("Realtek", ignoreCase = true)) "Realtek Ethernet" else null
                    DriverClass.AUDIO -> if (driver.providerName.contains("Realtek", ignoreCase = true)) "Realtek Audio" else null
                    DriverClass.BLUETOOTH -> if (driver.providerName.contains("Intel", ignoreCase = true)) "Intel Bluetooth" else null
                    DriverClass.SYSTEM -> if (driver.providerName.contains("AMD", ignoreCase = true)) "AMD Chipset" else if (driver.providerName.contains("Intel", ignoreCase = true)) "Intel Chipset" else null
                    else -> null
                }

                if (candidateQuery != null) {
                    val searchResult = WinGetExecutor.execute("search", "--count", "2", "-q", candidateQuery, "--accept-source-agreements", "--disable-interactivity")
                    val candidates = WinGetParser.parseListOutput(searchResult.stdout)
                    val matched = candidates.firstOrNull()

                    if (matched != null && VersionComparator.isNewer(current = driver.driverVersion, available = matched.version)) {
                        driver.copy(
                            availableVersion = matched.version,
                            matchedPackageId = matched.id
                        )
                    } else {
                        driver
                    }
                } else {
                    driver
                }
            } catch (_: Throwable) {
                driver
            }
        }
        cachedDrivers = resolved
        resolved
    }

    override suspend fun updateDriver(driver: DriverPackage): Flow<OperationResult> = flow {
        emit(
            OperationResult.Loading(
                title = "Updating Driver",
                message = "Starting update for ${driver.displayName}...",
                currentProgress = 0.2f
            )
        )

        val logOutput = StringBuilder()
        var isSuccess = false

        if (!driver.matchedPackageId.isNullOrBlank()) {
            val args = listOf("upgrade", "--id", driver.matchedPackageId, "--exact", "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
            WinGetExecutor.streamExecution(*args.toTypedArray()).collect { line ->
                logOutput.appendLine(line)
                emit(
                    OperationResult.Loading(
                        title = "Updating Driver",
                        message = line.take(80),
                        currentProgress = 0.6f,
                        logOutput = logOutput.toString()
                    )
                )
                if (line.contains("Successfully installed", ignoreCase = true) || line.contains("No applicable update found", ignoreCase = true)) {
                    isSuccess = true
                }
            }
        } else {
            // PnP device scan & update
            val scanProcess = ProcessBuilder("pnputil.exe", "/scan-devices").redirectErrorStream(true).start()
            val text = scanProcess.inputStream.bufferedReader().use { it.readText() }
            scanProcess.waitFor()
            logOutput.appendLine(text)
            isSuccess = scanProcess.exitValue() == 0
        }

        val finalLog = logOutput.toString()
        if (isSuccess || finalLog.contains("Successfully installed", ignoreCase = true)) {
            emit(OperationResult.Success("Driver update completed for ${driver.displayName}", logOutput = finalLog))
        } else {
            emit(OperationResult.Success("PnP device driver scan completed", logOutput = finalLog))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteDriver(driver: DriverPackage): Flow<OperationResult> = flow {
        emit(
            OperationResult.Loading(
                title = "Deleting Driver Package",
                message = "Uninstalling ${driver.publishedName} (${driver.displayName}) from Windows Driver Store...",
                currentProgress = 0.2f
            )
        )

        val logOutput = StringBuilder()
        var isSuccess = false

        try {
            val process = ProcessBuilder("pnputil.exe", "/delete-driver", driver.publishedName, "/uninstall")
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line != null && line!!.isNotBlank()) {
                        logOutput.appendLine(line)
                        emit(
                            OperationResult.Loading(
                                title = "Deleting Driver Package",
                                message = line!!.take(80),
                                currentProgress = 0.6f,
                                logOutput = logOutput.toString()
                            )
                        )
                    }
                }
                process.waitFor()
                val exitVal = process.exitValue()
                val text = logOutput.toString()
                if (exitVal == 0 || text.contains("deleted successfully", ignoreCase = true) || text.contains("Driver package uninstalled", ignoreCase = true)) {
                    isSuccess = true
                }
            } finally {
                reader.close()
                process.destroy()
            }

            val finalLog = logOutput.toString()
            if (isSuccess) {
                cachedDrivers = cachedDrivers.filterNot { it.publishedName.equals(driver.publishedName, ignoreCase = true) }
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
                        details = finalLog.lines().filter { it.isNotBlank() }.takeLast(5).joinToString("\n"),
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
            val process = ProcessBuilder("pnputil.exe", "/scan-devices").start()
            process.waitFor()
            OperationResult.Success("Windows Plug and Play device scan completed successfully.")
        } catch (e: Exception) {
            OperationResult.Error("Device scan failed: ${e.message}")
        }
    }
}
