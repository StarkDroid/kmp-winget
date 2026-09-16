package com.velocity.kmpwinget.domain.model

import java.util.UUID

enum class DriverClass(val displayName: String) {
    DISPLAY("Display / GPU"),
    NET("Network / Wi-Fi"),
    AUDIO("Audio / Sound"),
    BLUETOOTH("Bluetooth"),
    SYSTEM("System / Chipset"),
    PERIPHERALS("Peripherals"),
    OTHER("Hardware Component");

    companion object {
        fun fromString(str: String?): DriverClass {
            if (str.isNullOrBlank()) return OTHER
            val lower = str.trim().lowercase()
            return when {
                lower.contains("display") || lower.contains("graphics") -> DISPLAY
                lower.contains("net") || lower.contains("network") || lower.contains("wlan") || lower.contains("lan") -> NET
                lower.contains("audio") || lower.contains("media") || lower.contains("sound") -> AUDIO
                lower.contains("bluetooth") || lower.contains("bt") -> BLUETOOTH
                lower.contains("system") || lower.contains("processor") || lower.contains("chipset") -> SYSTEM
                lower.contains("hid") || lower.contains("keyboard") || lower.contains("mouse") || lower.contains("usb") -> PERIPHERALS
                else -> OTHER
            }
        }
    }
}

data class DriverPackage(
    val publishedName: String,
    val originalName: String,
    val providerName: String,
    val className: String,
    val driverClass: DriverClass = DriverClass.fromString(className),
    val driverDate: String,
    val driverVersion: String,
    val signerName: String? = null,
    val availableVersion: String? = null,
    val matchedPackageId: String? = null,
    val friendlyName: String? = null,
    val uniqueId: String = UUID.randomUUID().toString()
) {
    val displayName: String
        get() = friendlyName ?: "$providerName ${driverClass.displayName} ($originalName)"

    val hasUpdate: Boolean
        get() = !availableVersion.isNullOrBlank() &&
                availableVersion.trim() != driverVersion.trim() &&
                VersionComparator.isNewer(driverVersion, availableVersion)
}
