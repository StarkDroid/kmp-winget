package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDetails
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.domain.model.VersionComparator

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
                    (line.contains("Version", ignoreCase = true) || line.contains("Source", ignoreCase = true))
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
                    val sub = line.substring(start, end).trim()
                    return sub.ifBlank { null }
                }

                val name = getColValue("Name") ?: ""
                val id = getColValue("Id") ?: ""
                val rawVersion = getColValue("Version") ?: ""
                val rawAvailable = getColValue("Available")
                val rawSource = getColValue("Source")

                if (name.isNotEmpty() && id.isNotEmpty()) {
                    val cleanedVersion = sanitizeVersion(rawVersion)
                    val cleanedAvailable = sanitizeVersion(rawAvailable)
                    val resolvedSource = PackageSource.fromIdAndSource(id, rawSource)

                    packages.add(
                        Package(
                            id = id,
                            name = name,
                            version = cleanedVersion,
                            availableVersion = cleanedAvailable,
                            source = resolvedSource,
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

    /**
     * Parses the output of `winget show` to extract exact package details (full untruncated ID and version).
     */
    fun parseShowOutput(output: String): PackageDetails? {
        if (output.isBlank()) return null
        val lines = output.lines()

        // Single package matched: "Found <Name> [<Id>]"
        val foundLine = lines.firstOrNull { it.startsWith("Found ", ignoreCase = true) }
        if (foundLine != null) {
            val name = foundLine.removePrefix("Found ").substringBefore("[").trim()
            val id = foundLine.substringAfter("[").substringBefore("]").trim()
            val versionLine = lines.firstOrNull { it.startsWith("Version:", ignoreCase = true) }
            val version = versionLine?.substringAfter(":")?.trim() ?: ""
            val publisherLine = lines.firstOrNull { it.startsWith("Publisher:", ignoreCase = true) }
            val publisher = publisherLine?.substringAfter(":")?.trim()

            if (id.isNotEmpty() && version.isNotEmpty()) {
                return PackageDetails(
                    id = id,
                    name = name,
                    version = sanitizeVersion(version),
                    publisher = publisher
                )
            }
        }

        // Multiple packages list table from winget show
        val table = parseListOutput(output)
        val first = table.firstOrNull()
        if (first != null && first.id.isNotEmpty()) {
            return PackageDetails(
                id = first.id,
                name = first.name,
                version = first.version,
                publisher = first.publisher
            )
        }

        return null
    }

    /**
     * Parses the full output of `winget show` to extract all metadata fields.
     */
    fun parseFullPackageDetails(output: String, pkg: Package): PackageDetails {
        if (output.isBlank()) {
            return PackageDetails(
                id = pkg.targetUpgradeId.ifBlank { pkg.id },
                name = pkg.name,
                version = pkg.availableVersion ?: pkg.version,
                installedVersion = pkg.version.ifBlank { null },
                availableVersion = pkg.availableVersion,
                hasUpdate = pkg.hasUpdate,
                publisher = pkg.publisher,
                description = pkg.description,
                iconPath = pkg.iconPath,
                source = pkg.source
            )
        }

        var id: String = pkg.targetUpgradeId.ifBlank { pkg.id }
        var name: String = pkg.name
        var parsedVersion: String? = null
        var publisher: String? = pkg.publisher
        var publisherUrl: String? = null
        var publisherSupportUrl: String? = null
        var author: String? = null
        var moniker: String? = null
        var description: String? = pkg.description
        var homepage: String? = null
        var license: String? = null
        var licenseUrl: String? = null
        var copyright: String? = null
        var releaseDate: String? = null
        var releaseNotes: String? = null
        var releaseNotesUrl: String? = null
        var installerType: String? = null
        var installerUrl: String? = null
        var installerSha256: String? = null
        var architecture: String? = null
        val tags = mutableListOf<String>()

        val lines = output.lines()
        var currentMultiLineKey: String? = null
        val multiLineBuffer = mutableListOf<String>()

        fun flushMultiLine() {
            val text = multiLineBuffer.joinToString("\n").trim()
            if (text.isNotEmpty()) {
                when (currentMultiLineKey) {
                    "description" -> description = text
                    "releasenotes" -> releaseNotes = text
                    "tags" -> {
                        val parsedTags = multiLineBuffer.map { it.trim() }.filter { it.isNotEmpty() }
                        tags.addAll(parsedTags)
                    }
                }
            }
            multiLineBuffer.clear()
            currentMultiLineKey = null
        }

        for (rawLine in lines) {
            val line = rawLine
            val trimLine = line.trim()

            if (trimLine.isEmpty()) {
                if (currentMultiLineKey != null) {
                    multiLineBuffer.add("")
                }
                continue
            }

            // Check header "Found <Name> [<Id>]"
            if (trimLine.startsWith("Found ", ignoreCase = true) && trimLine.contains("[") && trimLine.contains("]")) {
                flushMultiLine()
                val parsedName = trimLine.removePrefix("Found ").substringBefore("[").trim()
                val parsedId = trimLine.substringAfter("[").substringBefore("]").trim()
                if (parsedName.isNotEmpty()) name = parsedName
                if (parsedId.isNotEmpty()) id = parsedId
                continue
            }

            // Check known key-value fields
            when {
                trimLine.startsWith("Publisher Support Url:", ignoreCase = true) || trimLine.startsWith("Publisher Support URL:", ignoreCase = true) -> {
                    flushMultiLine()
                    publisherSupportUrl = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Publisher Url:", ignoreCase = true) || trimLine.startsWith("Publisher URL:", ignoreCase = true) -> {
                    flushMultiLine()
                    publisherUrl = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Publisher:", ignoreCase = true) -> {
                    flushMultiLine()
                    publisher = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Version:", ignoreCase = true) -> {
                    flushMultiLine()
                    parsedVersion = sanitizeVersion(trimLine.substringAfter(":").trim()).ifBlank { null }
                }
                trimLine.startsWith("Author:", ignoreCase = true) -> {
                    flushMultiLine()
                    author = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Moniker:", ignoreCase = true) -> {
                    flushMultiLine()
                    moniker = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Homepage:", ignoreCase = true) -> {
                    flushMultiLine()
                    homepage = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("License Url:", ignoreCase = true) || trimLine.startsWith("License URL:", ignoreCase = true) -> {
                    flushMultiLine()
                    licenseUrl = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("License:", ignoreCase = true) -> {
                    flushMultiLine()
                    license = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Copyright:", ignoreCase = true) -> {
                    flushMultiLine()
                    copyright = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Release Notes Url:", ignoreCase = true) || trimLine.startsWith("Release Notes URL:", ignoreCase = true) -> {
                    flushMultiLine()
                    releaseNotesUrl = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Release Notes:", ignoreCase = true) -> {
                    flushMultiLine()
                    val inline = trimLine.substringAfter(":").trim()
                    currentMultiLineKey = "releasenotes"
                    if (inline.isNotEmpty()) multiLineBuffer.add(inline)
                }
                trimLine.startsWith("Description:", ignoreCase = true) -> {
                    flushMultiLine()
                    val inline = trimLine.substringAfter(":").trim()
                    currentMultiLineKey = "description"
                    if (inline.isNotEmpty()) multiLineBuffer.add(inline)
                }
                trimLine.startsWith("Tags:", ignoreCase = true) -> {
                    flushMultiLine()
                    val inline = trimLine.substringAfter(":").trim()
                    currentMultiLineKey = "tags"
                    if (inline.isNotEmpty()) multiLineBuffer.add(inline)
                }
                trimLine.startsWith("Installer Type:", ignoreCase = true) -> {
                    flushMultiLine()
                    installerType = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Installer Url:", ignoreCase = true) || trimLine.startsWith("Installer URL:", ignoreCase = true) -> {
                    flushMultiLine()
                    installerUrl = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Installer SHA256:", ignoreCase = true) ||
                trimLine.startsWith("Installer SHA-256:", ignoreCase = true) ||
                trimLine.startsWith("SHA256:", ignoreCase = true) ||
                trimLine.startsWith("SHA-256:", ignoreCase = true) -> {
                    flushMultiLine()
                    installerSha256 = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Release Date:", ignoreCase = true) -> {
                    flushMultiLine()
                    releaseDate = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Installer Architecture:", ignoreCase = true) ||
                trimLine.startsWith("Architecture:", ignoreCase = true) -> {
                    flushMultiLine()
                    architecture = trimLine.substringAfter(":").trim().ifBlank { null }
                }
                trimLine.startsWith("Documentation:", ignoreCase = true) ||
                trimLine.startsWith("Installer:", ignoreCase = true) ||
                trimLine.startsWith("Privacy Url:", ignoreCase = true) ||
                trimLine.startsWith("Copyright Url:", ignoreCase = true) ||
                trimLine.startsWith("Purchase Url:", ignoreCase = true) ||
                trimLine.startsWith("Agreements:", ignoreCase = true) ||
                trimLine.startsWith("Offline Distribution Supported:", ignoreCase = true) ||
                trimLine.startsWith("Installer Scope:", ignoreCase = true) ||
                trimLine.startsWith("Installer Locale:", ignoreCase = true) -> {
                    flushMultiLine()
                }
                else -> {
                    if (currentMultiLineKey != null) {
                        multiLineBuffer.add(trimLine)
                    }
                }
            }
        }
        flushMultiLine()

        // Infer architecture if not explicitly provided
        if (architecture.isNullOrBlank()) {
            val urlOrId = (installerUrl ?: "") + " " + id
            when {
                urlOrId.contains("x64", ignoreCase = true) || urlOrId.contains("64-bit", ignoreCase = true) -> architecture = "x64"
                urlOrId.contains("arm64", ignoreCase = true) -> architecture = "arm64"
                urlOrId.contains("x86", ignoreCase = true) || urlOrId.contains("32-bit", ignoreCase = true) -> architecture = "x86"
            }
        }

        val installedVer = pkg.version.ifBlank { null }
        val finalAvailVer = pkg.availableVersion ?: if (!parsedVersion.isNullOrBlank() && parsedVersion != installedVer) parsedVersion else null
        val hasUpdate = pkg.hasUpdate || (finalAvailVer != null && installedVer != null && VersionComparator.isNewer(installedVer, finalAvailVer))

        return PackageDetails(
            id = id,
            name = name,
            version = parsedVersion ?: pkg.version,
            installedVersion = installedVer,
            availableVersion = finalAvailVer,
            hasUpdate = hasUpdate,
            publisher = publisher ?: pkg.publisher,
            publisherUrl = publisherUrl,
            publisherSupportUrl = publisherSupportUrl,
            author = author,
            moniker = moniker,
            description = description ?: pkg.description,
            homepage = homepage,
            license = license,
            licenseUrl = licenseUrl,
            copyright = copyright,
            releaseDate = releaseDate,
            releaseNotes = releaseNotes,
            releaseNotesUrl = releaseNotesUrl,
            installerType = installerType,
            installerUrl = installerUrl,
            installerSha256 = installerSha256,
            architecture = architecture,
            tags = tags.distinct(),
            iconPath = pkg.iconPath,
            source = pkg.source
        )
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
                source = PackageSource.fromIdAndSource(id, source),
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
