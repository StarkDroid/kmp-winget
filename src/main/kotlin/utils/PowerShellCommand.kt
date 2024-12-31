package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Package

private fun executeCommand(command: String): String {
    println("Executing $command")
    val process = ProcessBuilder("powershell.exe", "-Command", command)
        .redirectErrorStream(true)
        .start()

    return process.inputStream.bufferedReader().use { it.readText().trim() }.also {
        process.waitFor()
    }
}

private fun listInstalledPackages(): List<Package> {
    val output = executeCommand("winget list --disable-interactivity")
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

fun refreshPackages(
    scope: CoroutineScope,
    onPackagesLoaded: (List<model.Package>) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    scope.launch {
        try {
            setLoading(true)
            val packages = withContext(Dispatchers.IO) {
                listInstalledPackages()
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
        } catch (e: Exception) {
            println("Error loading packages: ${e.message}")
            onPackagesLoaded(emptyList())
        } finally {
            setLoading(false)
        }
    }
}