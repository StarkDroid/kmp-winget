package com.velocity.kmpwinget.domain.model

data class DriveInfo(
    val root: String,
    val name: String,
    val totalSpaceGb: Double,
    val freeSpaceGb: Double,
    val usedSpaceGb: Double,
    val percentUsed: Float
)

data class SystemStats(
    val wingetVersion: String = "Detecting...",
    val isWingetAvailable: Boolean = true,
    val totalPackages: Int = 0,
    val updatesAvailableCount: Int = 0,
    val drives: List<DriveInfo> = emptyList(),
    val windowsVersion: String = "Windows 11"
)
