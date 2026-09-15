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

            // Determine lookup key: prefer wingetId if available, otherwise normalized name
            val key = if (!wingetId.isNullOrBlank()) "id:$wingetId" else "name:$normName"

            val existing = deduplicatedMap[key] ?: findByNameKey(deduplicatedMap, normName)

            if (existing == null) {
                deduplicatedMap[key] = pkg
            } else {
                // Merge entries: pick the superior one (WinGet source or one with updates)
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
        // Priority 1: Has active update
        if (first.hasUpdate && !second.hasUpdate) return first.copy(
            publisher = first.publisher ?: second.publisher,
            estimatedSize = first.estimatedSize ?: second.estimatedSize
        )
        if (second.hasUpdate && !first.hasUpdate) return second.copy(
            publisher = second.publisher ?: first.publisher,
            estimatedSize = second.estimatedSize ?: first.estimatedSize
        )

        // Priority 2: WinGet or MSStore managed over local ARP
        if (!first.isLocal && second.isLocal) {
            return first.copy(
                availableVersion = first.availableVersion ?: second.availableVersion,
                matchedWingetId = first.matchedWingetId ?: second.matchedWingetId,
                publisher = first.publisher ?: second.publisher,
                estimatedSize = first.estimatedSize ?: second.estimatedSize
            )
        }
        if (!second.isLocal && first.isLocal) {
            return second.copy(
                availableVersion = second.availableVersion ?: first.availableVersion,
                matchedWingetId = second.matchedWingetId ?: first.matchedWingetId,
                publisher = second.publisher ?: first.publisher,
                estimatedSize = second.estimatedSize ?: first.estimatedSize
            )
        }

        // Priority 3: Newer installed version
        if (VersionComparator.isNewer(first.version, second.version)) {
            return second
        }

        return first
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
