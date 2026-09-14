package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource

object WinGetParser {

    /**
     * Parses the output of `winget list` or `winget search`.
     */
    fun parseListOutput(output: String): List<Package> {
        if (output.isBlank()) return emptyList()

        val lines = output.lines()
        val headerIndex = lines.indexOfFirst { line ->
            line.contains("Name", ignoreCase = true) &&
                    (line.contains("Id", ignoreCase = true) || line.contains("ID")) &&
                    line.contains("Version", ignoreCase = true)
        }

        if (headerIndex == -1 || headerIndex + 1 >= lines.size) {
            return parseFallback(lines)
        }

        val headerLine = lines[headerIndex]
        val separatorIndex = headerIndex + 1

        val idStart = headerLine.indexOf("Id", ignoreCase = true).takeIf { it >= 0 }
            ?: headerLine.indexOf("ID").takeIf { it >= 0 } ?: 30
        val versionStart = headerLine.indexOf("Version", ignoreCase = true).takeIf { it >= 0 } ?: (idStart + 25)
        val availableStart = headerLine.indexOf("Available", ignoreCase = true)
        val sourceStart = headerLine.indexOf("Source", ignoreCase = true)

        val packages = mutableListOf<Package>()

        for (i in (separatorIndex + 1) until lines.size) {
            val line = lines[i]
            if (line.isBlank() || line.startsWith("-") || line.contains("upgrades available", ignoreCase = true)) {
                continue
            }

            try {
                val len = line.length
                val name = if (len > 0) line.substring(0, minOf(len, idStart)).trim() else ""
                val id = if (len > idStart) line.substring(idStart, minOf(len, versionStart)).trim() else ""

                val rawVersion = when {
                    availableStart > 0 && len > versionStart ->
                        line.substring(versionStart, minOf(len, availableStart)).trim()
                    sourceStart > 0 && len > versionStart ->
                        line.substring(versionStart, minOf(len, sourceStart)).trim()
                    len > versionStart ->
                        line.substring(versionStart).trim()
                    else -> ""
                }

                val rawAvailable = if (availableStart > 0 && len > availableStart) {
                    if (sourceStart > 0 && len > sourceStart) {
                        line.substring(availableStart, minOf(len, sourceStart)).trim()
                    } else {
                        line.substring(availableStart).trim()
                    }
                } else null

                val rawSource = if (sourceStart > 0 && len > sourceStart) {
                    line.substring(sourceStart).trim()
                } else null

                if (name.isNotEmpty() && id.isNotEmpty()) {
                    val cleanedVersion = sanitizeVersion(rawVersion)
                    val cleanedAvailable = sanitizeVersion(rawAvailable)

                    packages.add(
                        Package(
                            id = id,
                            name = name,
                            version = cleanedVersion,
                            availableVersion = cleanedAvailable,
                            source = PackageSource.fromString(rawSource),
                            rawSource = rawSource
                        )
                    )
                }
            } catch (_: Exception) {
                // Try fallback for this single line
                parseSingleLineFallback(line)?.let { packages.add(it) }
            }
        }

        return packages
    }

    private fun parseFallback(lines: List<String>): List<Package> {
        return lines.mapNotNull { parseSingleLineFallback(it) }
    }

    private fun parseSingleLineFallback(line: String): Package? {
        if (line.isBlank() || line.startsWith("-") || line.contains("Name") && line.contains("Id")) {
            return null
        }

        val parts = line.split(Regex("\\s{2,}"))
        if (parts.size >= 3) {
            val name = parts[0].trim()
            val id = parts[1].trim()
            val version = parts[2].trim()
            val available = if (parts.size > 3) parts[3].trim() else null
            val source = if (parts.size > 4) parts[4].trim() else null

            return Package(
                id = id,
                name = name,
                version = sanitizeVersion(version),
                availableVersion = sanitizeVersion(available),
                source = PackageSource.fromString(source),
                rawSource = source
            )
        }
        return null
    }

    private fun sanitizeVersion(version: String?): String {
        if (version.isNullOrBlank()) return ""
        var cleaned = version.trim()
        if (cleaned.equals("Unknown", ignoreCase = true) || cleaned.equals("winget", ignoreCase = true)) {
            return ""
        }
        cleaned = cleaned.replace(Regex("\\s*winget\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*msstore\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^<\\s*"), "")
            .trim()
        return cleaned
    }
}
