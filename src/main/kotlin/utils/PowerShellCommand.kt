package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Package
import model.PerformAction

fun executeCommand(command: String): String {
    println("Executing $command")
    val process = ProcessBuilder("powershell.exe", "-Command", command)
        .redirectErrorStream(true)
        .start()

    return process.inputStream.bufferedReader().use { it.readText().trim() }.also {
        process.waitFor()
    }
}

private fun listInstalledPackages(showUpgradesOnly: Boolean): List<Package> {
    val command = buildString {
        append("winget list --disable-interactivity")
        if (showUpgradesOnly) {
            append(" --upgrade-available")
        }
    }
    val output = executeCommand(command)
    println("Raw Output: $output")
    return parseWingetList(output)
}

private fun parseWingetList(output: String): List<Package> {
    return output.lines()
        .drop(2)
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val parts = line.split(Regex("\\s{2,}"))

            if (parts.size >= 3) {
                val name = parts[0].trim()
                val idAndVersion = parts[1].trim()
                val version = parts[2].trim()
                var availableVersion: String? = null

                if (parts.size > 3) {
                    val potentialAvailable = parts[3].trim()
                    availableVersion = potentialAvailable
                }

                if (idAndVersion.last().isDigit() && version.isEmpty()) {
                    val lastSpaceIndex = idAndVersion.indexOfLast { it.isWhitespace() }
                    val extractedVersion = idAndVersion.substring(lastSpaceIndex + 1).trim()
                    val extractedId = idAndVersion.substring(0, lastSpaceIndex).trim()

                    Package(
                        name = name,
                        id = extractedId,
                        version = extractedVersion,
                        availableVersion = availableVersion
                    )
                } else {
                    Package(
                        name = name,
                        id = idAndVersion,
                        version = version,
                        availableVersion = availableVersion
                    )
                }
            } else {
                null
            }
        }
}

fun upgradePackage(packageName: String): Boolean {
    try {
        val escapedPackageName = packageName.replace("\"", "\\\"")
        val command = "winget upgrade -q \"$escapedPackageName\" --accept-source-agreements --accept-package-agreements"
        val output = executeCommand(command)
        println("Upgrade Output for $packageName: $output")

        return output.contains("Successfully installed", ignoreCase = true) ||
                output.contains("No applicable update found", ignoreCase = true)
    } catch (e: Exception) {
        println("Error upgrading package $packageName: ${e.message}")
        return false
    }
}

fun performAction(
    scope: CoroutineScope,
    onPackagesLoaded: (List<Package>) -> Unit,
    setLoading: (Boolean) -> Unit,
    action: PerformAction = PerformAction.RefreshList,
    onActionComplete: ((Boolean) -> Unit)? = null,
    showUpgradesOnly: Boolean = false
) {
    scope.launch {
        try {
            setLoading(true)
            when(action) {
                is PerformAction.RefreshList -> {
                    val packages = withContext(Dispatchers.IO) {
                        listInstalledPackages(showUpgradesOnly)
                    }.map { pkg ->
                        pkg.copy(
                            version = pkg.version.takeIf { it != "Unknown" && it != "winget" } ?: "",
                            availableVersion = pkg.availableVersion
                                ?.replace(Regex("\\s*winget\\b", RegexOption.IGNORE_CASE), "")
                                ?.trim()
                                ?: ""
                        )
                    }
                    onPackagesLoaded(packages)
                }

                is PerformAction.UpgradePackage -> {
                    val upgradeResult = withContext(Dispatchers.IO) {
                        upgradePackage(action.packageName)
                    }
                    onActionComplete?.invoke(upgradeResult)
                }
            }
        } catch (e: Exception) {
            println("Error loading packages: ${e.message}")
            when (action) {
                is PerformAction.RefreshList -> onPackagesLoaded(emptyList())
                else -> onActionComplete?.invoke(false)
            }
        } finally {
            setLoading(false)
        }
    }
}