package com.velocity.kmpwinget.utils

import com.velocity.kmpwinget.model.domain.Package
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PowerShellCommand {
    /**
     *
     * Primary command that takes winget as an input and targets the
     * required command
     *
     * */
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        println("Executing $command")
        val process = ProcessBuilder("powershell.exe", "-Command", command)
            .redirectErrorStream(true)
            .start()

        val result = process.inputStream.bufferedReader().use { it.readText().trim() }
        process.waitFor()
        result
    }

    /**
     *
     * Runs the "winget list" command, to fetch the entire list of apps installed.
     * Also appends "--upgrade-available" command when the upgrade toggle is clicked in the UI
     * Needs winget installed in powershell CLI
     *
     * */
    suspend fun listInstalledPackages(showUpgradesOnly: Boolean): List<Package> = withContext(Dispatchers.IO) {
        val command = buildString {
            append("winget list --disable-interactivity")
            if (showUpgradesOnly) {
                append(" --upgrade-available")
            }
        }
        val output = executeCommand(command)
        println("Raw Output: $output")
        parseWingetList(output)
    }

    /**
     *
     * The console output sanitation takes place here and is
     * put into their respective columns as a list of packages
     *
     * */
    fun parseWingetList(output: String): List<Package> {
        return output.lines()
            .drop(2)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
            try {
                val parts = line.split(Regex("\\s{2,}"))
                
                if (parts.size >= 3) {
                    val name = parts[0].trim()
                    val idAndVersion = parts[1].trim()
                    val version = parts[2].trim()
                    var availableVersion: String? = null
                    
                    if (parts.size > 3) {
                        availableVersion = parts[3].trim()
                    }
                    
                    if (idAndVersion.last().isDigit() && version.isEmpty()) {
                        val lastSpaceIndex = idAndVersion.indexOfLast { it.isWhitespace() }
                        val extractedVersion = idAndVersion.substring(lastSpaceIndex + 1).trim()
                        val extractedId = idAndVersion.substring(0, lastSpaceIndex).trim()
                        
                        Package(
                            name = name,
                            id = extractedId.take(100),
                            version = extractedVersion,
                            availableVersion = availableVersion
                        )
                    } else {
                        Package(
                            name = name,
                            id = idAndVersion.take(100),
                            version = version,
                            availableVersion = availableVersion
                        )
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                println("Error parsing package line: $line - ${e.message}")
                null
            }
        }
}

    /**
     *
     * Function to run an individual package upgrade with the base command
     * winget upgrade -q "Package Name"
     *
     * */
    suspend fun upgradePackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val escapedPackageName = packageName.replace("\"", "\\\"") // Escape quotes
            val command =
                "winget upgrade -q \"$escapedPackageName\" --accept-source-agreements --accept-package-agreements"
            val output = executeCommand(command)
            println("Upgrade Output for $packageName: $output")

            output.contains("Successfully installed", ignoreCase = true) ||
                    output.contains("No applicable update found", ignoreCase = true)
        } catch (e: Exception) {
            println("Error upgrading package $packageName: ${e.message}")
            false
        }
    }

    /**
     *
     * Function to uninstall an individual package with the base command
     * winget uninstall -q "Package Name"
     *
     * */
    suspend fun uninstallPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanedPackageName = packageName
                .replace(Regex("\\s*\\d+(\\.\\d+)?\\s*"), " ")
                .replace(Regex("\\s*\\.\\s*"), " ")
                .trim()

            // Escape quotes for safety
            val escapedPackageName = cleanedPackageName.replace("\"", "\\\"")
            val command = "winget uninstall -q \"$escapedPackageName\" --accept-source-agreements"
            val output = executeCommand(command)
            println("Uninstall Output for $cleanedPackageName: $output")

            output.contains("Successfully uninstalled", ignoreCase = true) ||
                    output.contains("No installed package found", ignoreCase = true)
        } catch (e: Exception) {
            println("Error uninstalling package $packageName: ${e.message}")
            false
        }
    }
}