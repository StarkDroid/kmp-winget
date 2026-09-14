package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cached
import androidx.compose.material.icons.twotone.CleaningServices
import androidx.compose.material.icons.twotone.Storage
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Storage Overview
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
                        text = "Scanning disk drives...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    stats.drives.forEach { drive ->
                        DriveCard(drive = drive, isDarkMode = isDarkMode)
                    }
                }
            }
        }

        // Section 2: Maintenance Actions
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Windows & Package Maintenance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionToolCard(
                    title = "Launch Windows Disk Cleanup",
                    subtitle = "Remove temporary installation files, Windows update caches, and system junk.",
                    icon = Icons.TwoTone.CleaningServices,
                    buttonText = "Open Tool",
                    isDarkMode = isDarkMode,
                    onClick = onLaunchDiskCleanup
                )

                ActionToolCard(
                    title = "Optimize WinGet Sources & Cache",
                    subtitle = "Reset repository sources, rebuild local package indexes, and resolve download stalls.",
                    icon = Icons.TwoTone.Cached,
                    buttonText = "Optimize",
                    isDarkMode = isDarkMode,
                    onClick = onOptimizeWinget
                )
            }
        }

        // Section 3: System Diagnostics
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Environment Diagnostics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleIslandControl(shape = RoundedCornerShape(10.dp), isDarkMode = isDarkMode)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DiagnosticRow(
                        label = "Operating System",
                        value = stats.windowsVersion
                    )
                    DiagnosticRow(
                        label = "WinGet CLI Version",
                        value = stats.wingetVersion
                    )
                    DiagnosticRow(
                        label = "Managed Applications",
                        value = "${stats.totalPackages} total installed (${stats.updatesAvailableCount} updates pending)"
                    )
                    DiagnosticRow(
                        label = "Architecture",
                        value = "Clean MVI • Kotlin Multiplatform Desktop"
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveCard(drive: DriveInfo, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .subtleIslandControl(shape = RoundedCornerShape(10.dp), isDarkMode = isDarkMode)
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

                Spacer(Modifier.width(10.dp))

                Text(
                    text = drive.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${drive.freeSpaceGb} GB free of ${drive.totalSpaceGb} GB",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
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

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${drive.percentUsed}% used",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
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
            .subtleIslandControl(shape = RoundedCornerShape(10.dp), isDarkMode = isDarkMode)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                    )
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
