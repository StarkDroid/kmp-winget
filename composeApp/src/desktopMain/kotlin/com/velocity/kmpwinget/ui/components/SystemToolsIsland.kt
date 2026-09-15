package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.DriveInfo
import com.velocity.kmpwinget.domain.model.LiveSystemTelemetry
import com.velocity.kmpwinget.domain.model.SystemStats
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun SystemToolsIsland(
    stats: SystemStats,
    isDarkMode: Boolean,
    onLaunchDiskCleanup: () -> Unit,
    onOptimizeWinget: () -> Unit
) {
    val telemetry = stats.telemetry

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section 1: System Storage Overview (Grid Layout)
        item {
            Text(
                text = "System Storage Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (stats.drives.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleIslandControl(shape = RoundedCornerShape(10.dp), isDarkMode = isDarkMode)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scanning storage drives...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                // Responsive Grid: 2 columns
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    stats.drives.chunked(2).forEach { rowDrives ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowDrives.forEach { drive ->
                                Box(modifier = Modifier.weight(1f)) {
                                    StorageDriveCard(drive = drive, isDarkMode = isDarkMode)
                                }
                            }
                            if (rowDrives.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Real-Time Hardware Telemetry & System Information
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Live System Telemetry & Hardware",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: CPU & RAM Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CPU Telemetry Card
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryCard(
                            title = "Processor (CPU)",
                            name = telemetry.cpuName,
                            primaryStat = "${telemetry.cpuUsagePercent}%",
                            secondaryStat = "${telemetry.cpuCores} Cores • ${if (telemetry.cpuSpeedGhz > 0) "${telemetry.cpuSpeedGhz} GHz" else "Active"}",
                            icon = Icons.TwoTone.Memory,
                            progress = (telemetry.cpuUsagePercent / 100f).coerceIn(0.02f, 1f),
                            badgeText = telemetry.cpuTempEstimate?.let { "~${it}°C" },
                            isDarkMode = isDarkMode,
                            accentColor = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                    }

                    // Memory (RAM) Telemetry Card
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryCard(
                            title = "Memory (RAM)",
                            name = "${telemetry.ramUsedGb} GB used of ${telemetry.ramTotalGb} GB",
                            primaryStat = "${telemetry.ramUsagePercent.toInt()}%",
                            secondaryStat = "${telemetry.ramFreeGb} GB available",
                            icon = Icons.TwoTone.Layers,
                            progress = (telemetry.ramUsagePercent / 100f).coerceIn(0.02f, 1f),
                            badgeText = "${telemetry.ramTotalGb.toInt()} GB Total",
                            isDarkMode = isDarkMode,
                            accentColor = Color(0xFF8B5CF6)
                        )
                    }
                }

                // Row 2: GPU & Live Internet Speed Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // GPU Card
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryCard(
                            title = "Graphics (GPU)",
                            name = telemetry.gpuName,
                            primaryStat = "${telemetry.gpuUsagePercent.toInt()}%",
                            secondaryStat = "Display Controller",
                            icon = Icons.TwoTone.VideogameAsset,
                            progress = (telemetry.gpuUsagePercent / 100f).coerceIn(0.02f, 1f),
                            badgeText = telemetry.gpuTempEstimate?.let { "~${it}°C" },
                            isDarkMode = isDarkMode,
                            accentColor = Color(0xFF10B981)
                        )
                    }

                    // Live Network Speed Card
                    Box(modifier = Modifier.weight(1f)) {
                        NetworkSpeedCard(telemetry = telemetry, isDarkMode = isDarkMode)
                    }
                }

                // Row 3: System Uptime Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Uptime",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = telemetry.systemUptime.ifBlank { "Active" },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "WinGet ${stats.wingetVersion}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Windows & Package Maintenance Tools
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Maintenance Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ActionToolCard(
                        title = "Windows Disk Cleanup",
                        subtitle = "Remove temp files and update caches.",
                        icon = Icons.TwoTone.CleaningServices,
                        buttonText = "Launch",
                        isDarkMode = isDarkMode,
                        onClick = onLaunchDiskCleanup
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ActionToolCard(
                        title = "Optimize WinGet Sources",
                        subtitle = "Reset repository and package caches.",
                        icon = Icons.TwoTone.Cached,
                        buttonText = "Optimize",
                        isDarkMode = isDarkMode,
                        onClick = onOptimizeWinget
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageDriveCard(drive: DriveInfo, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = drive.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (drive.percentUsed > 85f) {
                                if (isDarkMode) AppColors.dangerBadgeBgDark else AppColors.dangerBadgeBgLight
                            } else {
                                if (isDarkMode) Color(0x22FFFFFF) else Color(0x0F000000)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${drive.percentUsed.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (drive.percentUsed > 85f) {
                                if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            val progressColor = when {
                drive.percentUsed > 90f -> if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                drive.percentUsed > 75f -> Color(0xFFF59E0B)
                else -> if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
            }

            LinearProgressIndicator(
                progress = { (drive.percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${drive.freeSpaceGb} GB free of ${drive.totalSpaceGb} GB",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun TelemetryCard(
    title: String,
    name: String,
    primaryStat: String,
    secondaryStat: String,
    icon: ImageVector,
    progress: Float,
    badgeText: String?,
    isDarkMode: Boolean,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accentColor
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = if (isDarkMode) 0.22f else 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentColor
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = primaryStat,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = secondaryStat,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NetworkSpeedCard(
    telemetry: LiveSystemTelemetry,
    isDarkMode: Boolean
) {
    val accentColor = Color(0xFF0284C7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accentColor
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Live Network Traffic",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = if (isDarkMode) 0.22f else 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Real-time",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = accentColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.TwoTone.ArrowDownward,
                        contentDescription = "Download",
                        modifier = Modifier.size(16.dp),
                        tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = formatSpeed(telemetry.downloadSpeedKbps),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Upload Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.TwoTone.ArrowUpward,
                        contentDescription = "Upload",
                        modifier = Modifier.size(16.dp),
                        tint = accentColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = formatSpeed(telemetry.uploadSpeedKbps),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Upload",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = telemetry.networkAdapterName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

private fun formatSpeed(kbps: Double): String {
    return if (kbps >= 1024.0) {
        val mbps = Math.round((kbps / 1024.0) * 10.0) / 10.0
        "$mbps MB/s"
    } else {
        "${kbps.toInt()} KB/s"
    }
}

@Composable
private fun ActionToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                    )
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
