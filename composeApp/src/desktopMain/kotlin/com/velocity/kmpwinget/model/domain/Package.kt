package com.velocity.kmpwinget.model.domain

import java.util.UUID

/**
 * Represents a software package managed by winget
 */
data class Package(
    val id: String,
    val name: String,
    val version: String,
    val availableVersion: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val installDate: String? = null,
    val size: String? = null,
    val isSelected: Boolean = false,
    val uniqueId: String = UUID.randomUUID().toString()
) {
    val hasUpdate: Boolean get() = !availableVersion.isNullOrEmpty() && availableVersion != version
}

fun Package.cleanVersions(): Package {
    return this.copy(
        version = version.takeIf { it != "Unknown" && it != "winget" } ?: "",
        availableVersion = availableVersion
            ?.replace(Regex("\\s*winget\\b", RegexOption.IGNORE_CASE), "")
            ?.replace(Regex("\\s*msstore\\b", RegexOption.IGNORE_CASE), "")
            ?.trim()
            ?: ""
    )
}