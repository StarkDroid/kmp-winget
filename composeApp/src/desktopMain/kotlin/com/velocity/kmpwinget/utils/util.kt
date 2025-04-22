package com.velocity.kmpwinget.utils

import com.velocity.kmpwinget.model.domain.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Safely executes disk cleanup utility
 */
suspend fun executeCleanupManager(): OperationResult {
    return withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("powershell.exe", "-Command", "cleanmgr /sagerun:1")
                .redirectErrorStream(true)
                .start()

            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroy()
                return@withContext OperationResult.Error("Disk Cleanup Manager timed out")
            }

            if (process.exitValue() == 0) {
                OperationResult.Success("Disk Cleanup Manager completed successfully")
            } else {
                val output = process.inputStream.bufferedReader().use { it.readText().trim() }
                OperationResult.Error("Failed to run Disk Cleanup Manager: $output")
            }

        } catch (e: Exception) {
            OperationResult.Error("Error launching Disk Cleanup Manager: ${e.message}")
        }
    }
}

/**
 * Checks if winget is installed
 */
suspend fun isWingetAvailable(): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("powershell.exe", "-Command", "winget --version")
                .redirectErrorStream(true)
                .start()

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroy()
                return@withContext false
            }

            val exitCode = process.exitValue()

            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            println("Winget check output: $output")

            exitCode == 0
        } catch (e: Exception) {
            println("Error checking winget availability: ${e.message}")
            false
        }
    }
}

