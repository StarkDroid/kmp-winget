package com.velocity.kmpwinget.domain.model

import java.util.UUID

enum class PackageSource(val displayName: String) {
    WINGET("winget"),
    MSSTORE("msstore"),
    LOCAL("local"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(str: String?): PackageSource {
            if (str.isNullOrBlank()) return LOCAL
            val lower = str.trim().lowercase()
            return when {
                lower.contains("winget") -> WINGET
                lower.contains("msstore") -> MSSTORE
                lower.contains("local") || lower.contains("arp") -> LOCAL
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Domain entity representing an installed or upgradable software package.
 */
data class Package(
    val id: String,
    val name: String,
    val version: String,
    val availableVersion: String? = null,
    val source: PackageSource = PackageSource.WINGET,
    val rawSource: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val installDate: String? = null,
    val estimatedSize: String? = null,
    val isPinned: Boolean = false,
    val matchedWingetId: String? = null,
    val uniqueId: String = UUID.randomUUID().toString()
) {
    val hasUpdate: Boolean
        get() = !availableVersion.isNullOrBlank() &&
                availableVersion.trim() != version.trim() &&
                !availableVersion.contains("Unknown", ignoreCase = true) &&
                VersionComparator.isNewer(version, availableVersion)

    val cleanId: String
        get() = id.removePrefix("ARP\\Machine\\X64\\")
            .removePrefix("ARP\\Machine\\X86\\")
            .removePrefix("ARP\\User\\")

    val targetUpgradeId: String
        get() = matchedWingetId ?: id

    val isLocal: Boolean
        get() = source == PackageSource.LOCAL || id.startsWith("ARP\\")
}
