package com.velocity.kmpwinget.domain.model

import com.velocity.kmpwinget.domain.service.VersionComparator

enum class DriverClass(val displayName: String) {
    DISPLAY("Display / GPU"),
    NETWORK("Network / Wi-Fi"),
    AUDIO("Audio / Sound"),
    BLUETOOTH("Bluetooth"),
    SYSTEM("System / Chipset"),
    PERIPHERALS("Peripherals"),
    OTHER("Hardware Component");

    companion object {
        fun fromClassName(className: String?): DriverClass {
            if (className.isNullOrBlank()) return OTHER
            val lower = className.trim().lowercase()
            return when {
                lower.contains("display") || lower.contains("graphics") -> DISPLAY
                lower.contains("net") || lower.contains("network") || lower.contains("wlan") || lower.contains("lan") -> NETWORK
                lower.contains("audio") || lower.contains("media") || lower.contains("sound") -> AUDIO
                lower.contains("bluetooth") || lower.contains("bt") -> BLUETOOTH
                lower.contains("system") || lower.contains("processor") || lower.contains("chipset") -> SYSTEM
                lower.contains("hid") || lower.contains("keyboard") || lower.contains("mouse") || lower.contains("usb") -> PERIPHERALS
                else -> OTHER
            }
        }

        /**
         * Resolves a driver class from the title of a Windows Update driver package,
         * e.g. "Intel Corporation - Display - 31.0.101.5333".
         */
        fun fromUpdateTitle(title: String): DriverClass {
            val lower = title.lowercase()
            return when {
                lower.contains("display") || lower.contains("graphics") || lower.contains("video") -> DISPLAY
                lower.contains("network") || lower.contains("wi-fi") || lower.contains("wifi") ||
                    lower.contains("ethernet") || lower.contains("wlan") -> NETWORK
                lower.contains("audio") || lower.contains("media") || lower.contains("sound") -> AUDIO
                lower.contains("bluetooth") -> BLUETOOTH
                lower.contains("system") || lower.contains("chipset") || lower.contains("processor") -> SYSTEM
                lower.contains("hid") || lower.contains("keyboard") || lower.contains("mouse") ||
                    lower.contains("usb") || lower.contains("input") -> PERIPHERALS
                else -> OTHER
            }
        }
    }
}

/**
 * A driver package installed in the Windows Driver Store.
 *
 * [availableVersion] and [availableUpdateTitle] are populated exclusively from the
 * Windows Update Agent (WUA) driver search, never from a heuristic WinGet lookup.
 */
data class DriverPackage(
    val publishedName: String,
    val originalName: String,
    val providerName: String,
    val className: String,
    val driverClass: DriverClass = DriverClass.fromClassName(className),
    val driverDate: String,
    val driverVersion: String,
    val signerName: String? = null,
    val availableVersion: String? = null,
    val availableUpdateTitle: String? = null
) {
    val displayName: String
        get() = "$providerName ${driverClass.displayName} ($originalName)"

    val hasUpdate: Boolean
        get() = !availableVersion.isNullOrBlank() &&
                availableVersion.trim() != driverVersion.trim() &&
                VersionComparator.isNewer(driverVersion, availableVersion)
}
