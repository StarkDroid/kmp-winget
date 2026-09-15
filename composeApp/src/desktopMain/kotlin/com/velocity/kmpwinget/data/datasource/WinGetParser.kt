package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource

object WinGetParser {

    private data class ColumnPos(val name: String, val start: Int)

    /**
     * Parses the output of `winget list` or `winget search` using dynamic column boundaries.
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

        // Detect all column starts dynamically
        val columns = mutableListOf<ColumnPos>()
        columns.add(ColumnPos("Name", 0))

        findColumnStart(headerLine, "Id")?.let { columns.add(ColumnPos("Id", it)) }
            ?: findColumnStart(headerLine, "ID")?.let { columns.add(ColumnPos("Id", it)) }

        findColumnStart(headerLine, "Version")?.let { columns.add(ColumnPos("Version", it)) }
        findColumnStart(headerLine, "Available")?.let { columns.add(ColumnPos("Available", it)) }
        findColumnStart(headerLine, "Match")?.let { columns.add(ColumnPos("Match", it)) }
        findColumnStart(headerLine, "Source")?.let { columns.add(ColumnPos("Source", it)) }

        val sortedColumns = columns.sortedBy { it.start }
        val packages = mutableListOf<Package>()

        for (i in (separatorIndex + 1) until lines.size) {
            val line = lines[i]
            if (line.isBlank() || line.startsWith("-") || line.contains("upgrades available", ignoreCase = true)) {
                continue
            }

            try {
                val len = line.length
                fun getColValue(colName: String): String? {
                    val idx = sortedColumns.indexOfFirst { it.name.equals(colName, ignoreCase = true) }
                    if (idx == -1) return null
                    val start = sortedColumns[idx].start
                    if (start >= len) return null
                    val end = if (idx + 1 < sortedColumns.size) {
                        minOf(len, sortedColumns[idx + 1].start)
                    } else {
                        len
                    }
                    if (start >= end) return null
                    return line.substring(start, end).trim()
                }

                val name = getColValue("Name") ?: ""
                val id = getColValue("Id") ?: ""
                val rawVersion = getColValue("Version") ?: ""
                val rawAvailable = getColValue("Available")
                val rawSource = getColValue("Source")

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
                } else {
                    parseSingleLineFallback(line)?.let { packages.add(it) }
                }
            } catch (_: Exception) {
                parseSingleLineFallback(line)?.let { packages.add(it) }
            }
        }

        return packages
    }

    private fun findColumnStart(header: String, columnName: String): Int? {
        val idx = header.indexOf(columnName, ignoreCase = true)
        return if (idx >= 0) idx else null
    }

    private fun parseFallback(lines: List<String>): List<Package> {
        return lines.mapNotNull { parseSingleLineFallback(it) }
    }

    private fun parseSingleLineFallback(line: String): Package? {
        if (line.isBlank() || line.startsWith("-") || (line.contains("Name") && line.contains("Id"))) {
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
            .trim()
        return cleaned
    }
}
