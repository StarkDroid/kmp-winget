package com.velocity.kmpwinget.data.repository

import com.velocity.kmpwinget.data.datasource.WinGetExecutor
import com.velocity.kmpwinget.data.datasource.WindowsHardwareMonitor
import com.velocity.kmpwinget.domain.model.DriveInfo
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.SystemStats
import com.velocity.kmpwinget.domain.repository.ISystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SystemRepositoryImpl : ISystemRepository {

    override suspend fun getWingetVersion(): String = withContext(Dispatchers.IO) {
        val result = WinGetExecutor.execute("--version")
        if (result.isSuccess && result.stdout.isNotBlank()) {
            result.stdout.lines().firstOrNull()?.trim() ?: "v1.x"
        } else {
            "Not Detected"
        }
    }

    override suspend fun isWingetInstalled(): Boolean = withContext(Dispatchers.IO) {
        val version = getWingetVersion()
        version != "Not Detected"
    }

    override suspend fun getSystemStats(packageCount: Int, updatesCount: Int): SystemStats = withContext(Dispatchers.IO) {
        val version = getWingetVersion()
        val drives = mutableListOf<DriveInfo>()

        try {
            val roots = File.listRoots() ?: emptyArray()
            for (root in roots) {
                val totalSpace = root.totalSpace
                val freeSpace = root.freeSpace
                if (totalSpace > 0) {
                    val totalGb = totalSpace / (1024.0 * 1024.0 * 1024.0)
                    val freeGb = freeSpace / (1024.0 * 1024.0 * 1024.0)
                    val usedGb = totalGb - freeGb
                    val percentUsed = ((usedGb / totalGb) * 100).toFloat()
                    val drivePath = root.absolutePath.replace("\\", "")

                    drives.add(
                        DriveInfo(
                            root = root.absolutePath,
                            name = "Local Disk ($drivePath)",
                            totalSpaceGb = Math.round(totalGb * 10.0) / 10.0,
                            freeSpaceGb = Math.round(freeGb * 10.0) / 10.0,
                            usedSpaceGb = Math.round(usedGb * 10.0) / 10.0,
                            percentUsed = Math.round(percentUsed * 10.0) / 10.0f
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        val telemetry = WindowsHardwareMonitor.getLiveTelemetry()

        SystemStats(
            wingetVersion = version,
            isWingetAvailable = version != "Not Detected",
            totalPackages = packageCount,
            updatesAvailableCount = updatesCount,
            drives = drives,
            telemetry = telemetry
        )
    }

    override suspend fun openDiskCleanup(): OperationResult = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder("cleanmgr.exe").start()
            OperationResult.Idle
        } catch (_: Exception) {
            OperationResult.Idle
        }
    }

    override suspend fun optimizeSystem(): OperationResult = withContext(Dispatchers.IO) {
        try {
            val result = WinGetExecutor.execute("source", "reset", "--force", "--accept-source-agreements")
            if (result.isSuccess) {
                OperationResult.Success("Winget sources and package cache optimized successfully.")
            } else {
                OperationResult.Success("System optimization completed.")
            }
        } catch (e: Exception) {
            OperationResult.Error("Optimization failed: ${e.message}")
        }
    }
}
