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
        val output = executeCommand("winget list")
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
                    Package(
                        id = parts[0].trim(),
                        name = parts[1].trim(),
                        version = parts[2].trim()
                    )
                } else null
            }
    }

}