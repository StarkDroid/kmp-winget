package com.velocity.kmpwinget.domain.model

object PackageDeduplicator {

    /**
     * Deduplicates packages by merging duplicate WinGet and local/ARP representations
     * of the same application. WinGet/MSStore entries or entries with updates take precedence.
     */
    fun deduplicate(packages: List<Package>): List<Package> {
        if (packages.isEmpty()) return emptyList()

        val deduplicatedMap = mutableMapOf<String, Package>()

        for (pkg in packages) {
            val normName = normalizeName(pkg.name)
            val wingetId = (pkg.matchedWingetId ?: pkg.id).takeIf {
                !it.startsWith("ARP\\", ignoreCase = true) && !it.startsWith("MSIX\\", ignoreCase = true)
            }?.lowercase()

            val key = if (!wingetId.isNullOrBlank()) "id:$wingetId" else "name:$normName"
            val existing = deduplicatedMap[key] ?: findByNameKey(deduplicatedMap, normName)

            if (existing == null) {
                deduplicatedMap[key] = pkg
            } else {
                val superior = mergePackages(existing, pkg)
                val existingKey = deduplicatedMap.entries.firstOrNull { it.value.id == existing.id }?.key ?: key
                deduplicatedMap[existingKey] = superior
            }
        }

        return deduplicatedMap.values.toList()
    }

    private fun findByNameKey(map: Map<String, Package>, normName: String): Package? {
        if (normName.length < 3) return null
        return map.values.firstOrNull { normalizeName(it.name) == normName }
    }

    private fun mergePackages(first: Package, second: Package): Package {
        val resolvedSource = when {
            first.source == PackageSource.WINGET || second.source == PackageSource.WINGET -> PackageSource.WINGET
            first.source == PackageSource.MSSTORE || second.source == PackageSource.MSSTORE -> PackageSource.MSSTORE
            else -> PackageSource.LOCAL
        }

        val primary = if (!first.isLocal) first else if (!second.isLocal) second else first

        val bestAvailable = when {
            !first.availableVersion.isNullOrBlank() -> first.availableVersion
            !second.availableVersion.isNullOrBlank() -> second.availableVersion
            else -> null
        }

        val bestMatchedId = first.matchedWingetId ?: second.matchedWingetId
        val bestIconPath = first.iconPath ?: second.iconPath
        val bestPublisher = first.publisher ?: second.publisher
        val bestSize = first.estimatedSize ?: second.estimatedSize

        return primary.copy(
            source = resolvedSource,
            availableVersion = bestAvailable,
            matchedWingetId = bestMatchedId,
            iconPath = bestIconPath,
            publisher = bestPublisher,
            estimatedSize = bestSize
        )
    }

    fun normalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("\\s*\\(.*?\\)"), "") // remove (x64), (64-bit), (user) etc
            .replace(Regex("\\s*v?\\d+(\\.\\d+)*.*$"), "") // remove trailing version numbers
            .replace(Regex("\\s*\\d+-bit", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*version\\s*\\d+.*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { name.trim().lowercase() }
    }
}
