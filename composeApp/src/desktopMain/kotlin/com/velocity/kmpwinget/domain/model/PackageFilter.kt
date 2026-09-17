package com.velocity.kmpwinget.domain.model

enum class NavigationTab(val title: String) {
    ALL_PACKAGES("All Apps"),
    UPGRADES_AVAILABLE("Updates Available"),
    DRIVERS("Device Drivers"),
    SYSTEM_TOOLS("System & Maintenance")
}

enum class PackageSortOption(val displayName: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    UPDATES_FIRST("Updates Available First"),
    SOURCE("Source (Winget / MSStore)")
}

enum class SourceFilterOption(val displayName: String) {
    ALL("All Sources"),
    WINGET_ONLY("WinGet"),
    MSSTORE_ONLY("Microsoft Store"),
    LOCAL_ONLY("Local / Win32")
}
