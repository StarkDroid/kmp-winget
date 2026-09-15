package com.velocity.kmpwinget.data.repository

import com.velocity.kmpwinget.data.datasource.WinGetExecutor
import com.velocity.kmpwinget.data.datasource.WinGetParser
import com.velocity.kmpwinget.data.datasource.WindowsRegistryScanner
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDeduplicator
import com.velocity.kmpwinget.domain.model.VersionComparator
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PackageRepositoryImpl : IPackageRepository {

    private var cachedPackages: List<Package> = emptyList()
    private var cachedUpgrades: List<Package> = emptyList()

    override fun getInstalledPackages(forceRefresh: Boolean): Flow<List<Package>> = flow {
        if (!forceRefresh && cachedPackages.isNotEmpty()) {
            emit(cachedPackages)
            return@flow
        }

        // 1. Fast initial emission from registry if cache is empty
        if (cachedPackages.isEmpty()) {
            val fastRegistryApps = WindowsRegistryScanner.scanInstalledApps()
            if (fastRegistryApps.isNotEmpty()) {
                emit(PackageDeduplicator.deduplicate(fastRegistryApps))
            }
        }

        // 2. Real-time authoritative list from WinGet CLI
        val result = WinGetExecutor.execute("list", "--accept-source-agreements", "--disable-interactivity")
        val packages = WinGetParser.parseListOutput(result.stdout)

        if (packages.isNotEmpty()) {
            val upgradeMap = cachedUpgrades.associate { it.id to (it.availableVersion ?: "") }
            val mergedPackages = packages.map { pkg ->
                if (upgradeMap.containsKey(pkg.id)) {
                    pkg.copy(availableVersion = upgradeMap[pkg.id])
                } else {
                    pkg
                }
            }

            val deduplicated = PackageDeduplicator.deduplicate(mergedPackages)
            cachedPackages = deduplicated
            emit(deduplicated)
        } else if (cachedPackages.isNotEmpty()) {
            emit(cachedPackages)
        }
    }.flowOn(Dispatchers.IO)

    override fun getUpgradablePackages(forceRefresh: Boolean): Flow<List<Package>> = flow {
        if (!forceRefresh && cachedUpgrades.isNotEmpty()) {
            emit(cachedUpgrades)
            return@flow
        }

        val result = WinGetExecutor.execute("list", "--upgrade-available", "--accept-source-agreements", "--disable-interactivity")
        val upgrades = WinGetParser.parseListOutput(result.stdout).filter { it.hasUpdate }
        val deduplicated = PackageDeduplicator.deduplicate(upgrades)

        cachedUpgrades = deduplicated
        emit(deduplicated)
    }.flowOn(Dispatchers.IO)

    override suspend fun resolveUpdatesForLocalPackages(packages: List<Package>): List<Package> = withContext(Dispatchers.IO) {
        val eligible = packages.filter { it.availableVersion.isNullOrBlank() && it.version.isNotBlank() && !it.version.contains("Unknown", ignoreCase = true) }
        if (eligible.isEmpty()) return@withContext packages

        val resolvedMap = mutableMapOf<String, Package>()

        // Process in chunks of 4 concurrent coroutines for fast background resolution
        eligible.chunked(4).forEach { chunk ->
            coroutineScope {
                val deferreds = chunk.map { pkg ->
                    async {
                        try {
                            val cleanName = cleanNameForQuery(pkg.name)
                            if (cleanName.length < 2) return@async pkg

                            // 1. Try winget show to get exact untruncated ID and version
                            val showResult = WinGetExecutor.execute("show", "-q", cleanName, "--accept-source-agreements", "--disable-interactivity")
                            val showDetails = WinGetParser.parseShowOutput(showResult.stdout)

                            if (showDetails != null && showDetails.version.isNotEmpty()) {
                                if (VersionComparator.isNewer(current = pkg.version, available = showDetails.version)) {
                                    return@async pkg.copy(
                                        availableVersion = showDetails.version,
                                        matchedWingetId = showDetails.id
                                    )
                                }
                            }

                            // 2. Fallback to winget search
                            val searchResult = WinGetExecutor.execute("search", "--count", "3", "-q", cleanName, "--accept-source-agreements", "--disable-interactivity")
                            val candidates = WinGetParser.parseListOutput(searchResult.stdout)

                            val matched = candidates.firstOrNull { cand ->
                                cand.name.equals(cleanName, ignoreCase = true) ||
                                        cand.name.contains(cleanName, ignoreCase = true) ||
                                        cleanName.contains(cand.name, ignoreCase = true)
                            }

                            if (matched != null && VersionComparator.isNewer(current = pkg.version, available = matched.version)) {
                                val fullId = if (matched.id.endsWith("…")) {
                                    showDetails?.id ?: matched.id.removeSuffix("…")
                                } else {
                                    matched.id
                                }

                                pkg.copy(
                                    availableVersion = matched.version,
                                    matchedWingetId = fullId
                                )
                            } else {
                                pkg
                            }
                        } catch (_: Throwable) {
                            pkg
                        }
                    }
                }
                deferreds.awaitAll().forEach { resolvedPkg ->
                    resolvedMap[resolvedPkg.id] = resolvedPkg
                }
            }
        }

        PackageDeduplicator.deduplicate(packages.map { resolvedMap[it.id] ?: it })
    }

    override suspend fun upgradePackage(
        packageId: String,
        packageName: String,
        matchedWingetId: String?
    ): Flow<OperationResult> = flow {
        emit(
            OperationResult.Loading(
                title = "Updating $packageName",
                message = "Connecting to repository...",
                currentProgress = 0.2f
            )
        )

        val targetId = matchedWingetId ?: if (!packageId.startsWith("ARP\\")) packageId else null
        val logOutput = StringBuilder()
        var isSuccess = false

        val args = if (!targetId.isNullOrBlank()) {
            listOf("upgrade", "--id", targetId, "--exact", "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
        } else {
            val cleanName = cleanNameForQuery(packageName)
            listOf("upgrade", "--name", cleanName, "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
        }

        try {
            WinGetExecutor.streamExecution(*args.toTypedArray()).collect { line ->
                logOutput.appendLine(line)
                emit(
                    OperationResult.Loading(
                        title = "Updating $packageName",
                        message = line.take(80),
                        currentProgress = 0.6f,
                        logOutput = logOutput.toString()
                    )
                )
                if (line.contains("Successfully installed", ignoreCase = true) ||
                    line.contains("No applicable update found", ignoreCase = true)) {
                    isSuccess = true
                }
            }

            var finalLog = logOutput.toString()

            // If "No installed package found" or upgrade couldn't match local ARP, run in-place install
            if (!isSuccess && !targetId.isNullOrBlank() && (finalLog.contains("No installed package found", ignoreCase = true) || finalLog.contains("Failed", ignoreCase = true))) {
                emit(
                    OperationResult.Loading(
                        title = "Updating $packageName",
                        message = "Running in-place package installer...",
                        currentProgress = 0.7f,
                        logOutput = finalLog
                    )
                )

                val installArgs = listOf("install", "--id", targetId, "--exact", "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
                WinGetExecutor.streamExecution(*installArgs.toTypedArray()).collect { line ->
                    logOutput.appendLine(line)
                    emit(
                        OperationResult.Loading(
                            title = "Updating $packageName",
                            message = line.take(80),
                            currentProgress = 0.85f,
                            logOutput = logOutput.toString()
                        )
                    )
                    if (line.contains("Successfully installed", ignoreCase = true)) {
                        isSuccess = true
                    }
                }
                finalLog = logOutput.toString()
            }

            if (isSuccess || finalLog.contains("Successfully installed", ignoreCase = true) || finalLog.contains("No applicable update found", ignoreCase = true)) {
                cachedPackages = emptyList()
                cachedUpgrades = emptyList()
                emit(
                    OperationResult.Success(
                        message = "Successfully updated $packageName",
                        logOutput = finalLog
                    )
                )
            } else {
                emit(
                    OperationResult.Error(
                        message = "Update could not be completed for $packageName",
                        details = finalLog.lines().takeLast(5).joinToString("\n"),
                        logOutput = finalLog
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                OperationResult.Error(
                    message = "Error during upgrade: ${e.message}",
                    logOutput = logOutput.toString()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uninstallPackage(packageId: String, packageName: String): Flow<OperationResult> = flow {
        emit(
            OperationResult.Loading(
                title = "Uninstalling $packageName",
                message = "Running uninstaller in background...",
                currentProgress = 0.3f
            )
        )

        val args = if (packageId.startsWith("ARP\\")) {
            val cleanName = cleanNameForQuery(packageName)
            listOf("uninstall", "--name", cleanName, "--silent", "--accept-source-agreements", "--disable-interactivity")
        } else {
            listOf("uninstall", "--id", packageId, "--exact", "--silent", "--accept-source-agreements", "--disable-interactivity")
        }

        val logOutput = StringBuilder()
        var isSuccess = false

        try {
            WinGetExecutor.streamExecution(*args.toTypedArray()).collect { line ->
                logOutput.appendLine(line)
                emit(
                    OperationResult.Loading(
                        title = "Uninstalling $packageName",
                        message = line.take(80),
                        currentProgress = 0.6f,
                        logOutput = logOutput.toString()
                    )
                )
                if (line.contains("Successfully uninstalled", ignoreCase = true)) {
                    isSuccess = true
                }
            }

            val finalLog = logOutput.toString()
            if (isSuccess || finalLog.contains("Successfully uninstalled", ignoreCase = true)) {
                cachedPackages = emptyList()
                cachedUpgrades = emptyList()
                emit(
                    OperationResult.Success(
                        message = "Successfully uninstalled $packageName",
                        logOutput = finalLog
                    )
                )
            } else {
                emit(
                    OperationResult.Error(
                        message = "Failed to uninstall $packageName",
                        details = finalLog.lines().takeLast(5).joinToString("\n"),
                        logOutput = finalLog
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                OperationResult.Error(
                    message = "Error during uninstallation: ${e.message}",
                    logOutput = logOutput.toString()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upgradeMultiplePackages(packages: List<Package>): Flow<OperationResult> = flow {
        if (packages.isEmpty()) {
            emit(OperationResult.Idle)
            return@flow
        }

        val total = packages.size
        var succeeded = 0
        var failed = 0
        val combinedLogs = StringBuilder()

        packages.forEachIndexed { index, pkg ->
            val progress = (index.toFloat()) / total.toFloat()
            emit(
                OperationResult.Loading(
                    title = "Updating Packages (${index + 1}/$total)",
                    message = "Currently updating ${pkg.name}...",
                    currentProgress = progress,
                    step = index + 1,
                    totalSteps = total,
                    logOutput = combinedLogs.toString()
                )
            )

            val targetId = pkg.targetUpgradeId.takeIf { !it.startsWith("ARP\\") }
            val args = if (!targetId.isNullOrBlank()) {
                listOf("upgrade", "--id", targetId, "--exact", "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
            } else {
                val cleanName = cleanNameForQuery(pkg.name)
                listOf("upgrade", "--name", cleanName, "--silent", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity")
            }

            var pkgSuccess = false
            try {
                WinGetExecutor.streamExecution(*args.toTypedArray()).collect { line ->
                    combinedLogs.appendLine("[${pkg.name}] $line")
                    if (line.contains("Successfully installed", ignoreCase = true) ||
                        line.contains("No applicable update found", ignoreCase = true)) {
                        pkgSuccess = true
                    }
                }
                if (pkgSuccess || combinedLogs.contains("Successfully installed")) {
                    succeeded++
                } else {
                    failed++
                }
            } catch (_: Exception) {
                failed++
            }
        }

        cachedPackages = emptyList()
        cachedUpgrades = emptyList()

        if (failed == 0) {
            emit(
                OperationResult.Success(
                    message = "All $succeeded packages updated successfully!",
                    logOutput = combinedLogs.toString()
                )
            )
        } else {
            emit(
                OperationResult.Success(
                    message = "Updated $succeeded packages ($failed failed)",
                    details = "Check operation logs for details.",
                    logOutput = combinedLogs.toString()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uninstallMultiplePackages(packages: List<Package>): Flow<OperationResult> = flow {
        if (packages.isEmpty()) {
            emit(OperationResult.Idle)
            return@flow
        }

        val total = packages.size
        var succeeded = 0
        var failed = 0
        val combinedLogs = StringBuilder()

        packages.forEachIndexed { index, pkg ->
            val progress = (index.toFloat()) / total.toFloat()
            emit(
                OperationResult.Loading(
                    title = "Uninstalling Packages (${index + 1}/$total)",
                    message = "Removing ${pkg.name}...",
                    currentProgress = progress,
                    step = index + 1,
                    totalSteps = total,
                    logOutput = combinedLogs.toString()
                )
            )

            val args = if (pkg.id.startsWith("ARP\\")) {
                val cleanName = cleanNameForQuery(pkg.name)
                listOf("uninstall", "--name", cleanName, "--silent", "--accept-source-agreements", "--disable-interactivity")
            } else {
                listOf("uninstall", "--id", pkg.id, "--exact", "--silent", "--accept-source-agreements", "--disable-interactivity")
            }

            var pkgSuccess = false
            try {
                WinGetExecutor.streamExecution(*args.toTypedArray()).collect { line ->
                    combinedLogs.appendLine("[${pkg.name}] $line")
                    if (line.contains("Successfully uninstalled", ignoreCase = true)) {
                        pkgSuccess = true
                    }
                }
                if (pkgSuccess || combinedLogs.contains("Successfully uninstalled")) {
                    succeeded++
                } else {
                    failed++
                }
            } catch (_: Exception) {
                failed++
            }
        }

        cachedPackages = emptyList()
        cachedUpgrades = emptyList()

        if (failed == 0) {
            emit(
                OperationResult.Success(
                    message = "All $succeeded packages uninstalled successfully!",
                    logOutput = combinedLogs.toString()
                )
            )
        } else {
            emit(
                OperationResult.Success(
                    message = "Uninstalled $succeeded packages ($failed failed)",
                    details = "Check logs for any packages requiring manual removal.",
                    logOutput = combinedLogs.toString()
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun searchWingetStore(query: String): List<Package> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val result = WinGetExecutor.execute("search", query, "--accept-source-agreements", "--disable-interactivity")
        PackageDeduplicator.deduplicate(WinGetParser.parseListOutput(result.stdout))
    }

    private fun cleanNameForQuery(name: String): String {
        return name
            .replace(Regex("\\s*\\(.*?\\)"), "")
            .replace(Regex("\\s*v?\\d+(\\.\\d+)*.*$"), "")
            .replace(Regex("\\s*version\\s*\\d+.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\d+-bit", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifBlank { name.trim() }
    }
}
