package com.velocity.kmpwinget.domain.model

import com.velocity.kmpwinget.domain.service.VersionComparator

enum class PackageSource(val displayName: String) {
    WINGET("WinGet"),
    MSSTORE("Microsoft Store"),
    LOCAL("Local"),
    UNKNOWN("Unknown");

    companion object {
        fun fromIdAndSource(id: String?, sourceStr: String?): PackageSource {
            val src = sourceStr?.trim()?.lowercase() ?: ""
            if (src.contains("winget")) return WINGET
            if (src.contains("msstore")) return MSSTORE

            val cleanId = id?.trim() ?: ""
            if (cleanId.startsWith("ARP\\", ignoreCase = true)) return LOCAL
            if (cleanId.startsWith("MSIX\\", ignoreCase = true)) return MSSTORE
            if (cleanId.contains(".")) return WINGET

            return LOCAL
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
    val publisher: String? = null,
    val description: String? = null,
    val installDate: String? = null,
    val estimatedSize: String? = null,
    val matchedWingetId: String? = null,
    val iconPath: String? = null
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

    val targetUpgradeId: String
        get() = matchedWingetId ?: id

    val isLocal: Boolean
        get() = source == PackageSource.LOCAL || id.startsWith("ARP\\")
}
