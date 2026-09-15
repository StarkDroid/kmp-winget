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
        get() {
            if (availableVersion.isNullOrBlank()) return false
            val avail = availableVersion.trim()
            val cur = version.trim()
            if (avail.isEmpty() || avail.equals(cur, ignoreCase = true)) return false
            if (avail.contains("Unknown", ignoreCase = true)) return false
            if (cur.isEmpty() || cur.contains("Unknown", ignoreCase = true) || cur.startsWith("<")) return true
            return VersionComparator.isNewer(cur, avail)
        }

    val cleanId: String
        get() = id.removePrefix("ARP\\Machine\\X64\\")
            .removePrefix("ARP\\Machine\\X86\\")
            .removePrefix("ARP\\User\\")

    val targetUpgradeId: String
        get() = matchedWingetId ?: id

    val isLocal: Boolean
        get() = source == PackageSource.LOCAL || id.startsWith("ARP\\")
}
