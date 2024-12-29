package utils

import model.Package

class PowerShellCommand {
    private fun executeCommand(command: String): String {
        println("Executing $command")
        val process = ProcessBuilder("powershell.exe", "-Command", command)
            .redirectErrorStream(true)
            .start()

        return process.inputStream.bufferedReader().use { it.readText().trim() }.also {
            process.waitFor()
        }
    }

    fun listInstalledPackages(): List<Package> {
        val output = executeCommand("winget list --disable-interactivity")
        println("Raw Output: $output")
        return parseWingetList(output)
    }

    private fun parseWingetList(output: String): List<Package> {
        return output.lines()
            .drop(8)
            .filter {
                it.isNotBlank() &&
                !it.contains("Loading", ignoreCase = true) &&
                        !it.contains("Retrieving", ignoreCase = true)
            }
            .mapNotNull { line ->
                val parts = line.split(Regex("\\s{2,}"))

                if (parts.size >= 3) {
                    val name = parts[0].trim()
                    val idAndVersion = parts[1].trim()
                    val version = parts[2].trim()

                    if (idAndVersion.last().isDigit() && version.isEmpty()) {
                        val lastSpaceIndex = idAndVersion.indexOfLast { it.isWhitespace() }
                        val extractedVersion = idAndVersion.substring(lastSpaceIndex + 1).trim()
                        val extractedId = idAndVersion.substring(0, lastSpaceIndex).trim()

                        Package(
                            name = name,
                            id = extractedId,
                            version = extractedVersion
                        )
                    } else {
                        Package(
                            name = name,
                            id = idAndVersion,
                            version = version
                        )
                    }
                } else {
                    null
                }
            }
    }

}