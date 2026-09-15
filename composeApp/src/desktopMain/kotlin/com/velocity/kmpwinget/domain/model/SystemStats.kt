package com.velocity.kmpwinget.domain.model

data class DriveInfo(
    val root: String,
    val name: String,
    val totalSpaceGb: Double,
    val freeSpaceGb: Double,
    val usedSpaceGb: Double,
    val percentUsed: Float
)

data class LiveSystemTelemetry(
    val cpuName: String = "CPU",
    val cpuUsagePercent: Float = 0f,
    val cpuCores: Int = 8,
    val cpuSpeedGhz: Double = 0.0,
    val cpuTempEstimate: Int? = 42,

    val ramTotalGb: Double = 16.0,
    val ramUsedGb: Double = 8.0,
    val ramFreeGb: Double = 8.0,
    val ramUsagePercent: Float = 50f,

    val gpuName: String = "Dedicated GPU",
    val gpuUsagePercent: Float = 0f,
    val gpuTempEstimate: Int? = 45,
    val vramTotalGb: Double = 8.0,

    val downloadSpeedKbps: Double = 0.0,
    val uploadSpeedKbps: Double = 0.0,
    val networkAdapterName: String = "Ethernet / Wi-Fi",

    val osName: String = "Windows 11",
    val osBuild: String = "",
    val systemUptime: String = ""
)

data class SystemStats(
    val wingetVersion: String = "Detecting...",
    val isWingetAvailable: Boolean = true,
    val totalPackages: Int = 0,
    val updatesAvailableCount: Int = 0,
    val drives: List<DriveInfo> = emptyList(),
    val windowsVersion: String = "Windows 11",
    val telemetry: LiveSystemTelemetry = LiveSystemTelemetry()
)
