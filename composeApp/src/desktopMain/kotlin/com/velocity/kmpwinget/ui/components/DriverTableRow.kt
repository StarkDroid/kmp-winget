package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.VolumeUp
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun DriverTableRow(
    driver: DriverPackage,
    isDarkMode: Boolean,
    onUpdate: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rowBg = when {
        isHovered -> if (isDarkMode) AppColors.rowHoverDark else AppColors.rowHoverLight
        else -> Color.Transparent
    }

    val animatedRowBg by animateColorAsState(
        targetValue = rowBg,
        animationSpec = tween(durationMillis = 120),
        label = "driverRowBg"
    )

    val (categoryIcon, categoryGradient) = getDriverCategoryVisuals(driver.driverClass)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedRowBg)
                .hoverable(interactionSource)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Gradient Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = driver.driverClass.displayName,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            // Center: Title & Metadata Tags
            Column(modifier = Modifier.weight(1f)) {
                // Line 1: Driver Title
                Text(
                    text = driver.displayName.ifBlank { driver.originalName },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                // Line 2: Provider, Class Badge, Published Name (oemXX.inf), Signer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (driver.providerName.isNotBlank()) {
                        Text(
                            text = driver.providerName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }

                    // Driver Class Badge
                    DriverClassBadge(driverClass = driver.driverClass, isDarkMode = isDarkMode)

                    // Published Name Pill (e.g. oem12.inf)
                    if (driver.publishedName.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isDarkMode) Color(0x1AFFFFFF) else Color(0x0C000000)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = driver.publishedName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Signer Name
                    if (!driver.signerName.isNullOrBlank()) {
                        Text(
                            text = "• ${driver.signerName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Right: Version Information & Update Indicator
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                if (driver.hasUpdate) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = driver.driverVersion.ifBlank { "Current" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = driver.availableVersion ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                            )
                        )
                    }
                } else {
                    Text(
                        text = if (driver.driverVersion.isNotBlank()) "v${driver.driverVersion}" else "Installed",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                if (driver.driverDate.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = driver.driverDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // Action Button
            Box(
                modifier = Modifier
                    .then(
                        if (driver.hasUpdate) {
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                )
                                .clickable { onUpdate() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        } else {
                            Modifier
                                .subtleIslandControl(
                                    shape = RoundedCornerShape(6.dp),
                                    isDarkMode = isDarkMode
                                )
                                .clickable { onUpdate() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (driver.hasUpdate) Icons.TwoTone.Download else Icons.TwoTone.Refresh,
                        contentDescription = "Update Driver",
                        modifier = Modifier.size(14.dp),
                        tint = if (driver.hasUpdate) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (driver.hasUpdate) "Update Driver" else "Scan / Update",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (driver.hasUpdate) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = if (isDarkMode) AppColors.rowDividerDark else AppColors.rowDividerLight
        )
    }
}

@Composable
private fun DriverClassBadge(driverClass: DriverClass, isDarkMode: Boolean) {
    val (bg, textColor) = when (driverClass) {
        DriverClass.DISPLAY -> Pair(
            if (isDarkMode) Color(0xFF3B0764) else Color(0xFFF3E8FF),
            if (isDarkMode) Color(0xFFC084FC) else Color(0xFF9333EA)
        )
        DriverClass.NET -> Pair(
            if (isDarkMode) Color(0xFF064E3B) else Color(0xFFECFDF5),
            if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)
        )
        DriverClass.AUDIO -> Pair(
            if (isDarkMode) Color(0xFF4C0519) else Color(0xFFFFE4E6),
            if (isDarkMode) Color(0xFFFB7185) else Color(0xFFE11D48)
        )
        DriverClass.BLUETOOTH -> Pair(
            if (isDarkMode) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
            if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
        )
        DriverClass.SYSTEM -> Pair(
            if (isDarkMode) Color(0xFF451A03) else Color(0xFFFEF3C7),
            if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706)
        )
        DriverClass.PERIPHERALS -> Pair(
            if (isDarkMode) Color(0xFF164E63) else Color(0xFFCFFAFE),
            if (isDarkMode) Color(0xFF22D3EE) else Color(0xFF0891B2)
        )
        DriverClass.OTHER -> Pair(
            if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6),
            if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    ) {
        Text(
            text = driverClass.displayName.substringBefore(" / "),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

private fun getDriverCategoryVisuals(driverClass: DriverClass): Pair<ImageVector, Brush> {
    return when (driverClass) {
        DriverClass.DISPLAY -> Pair(
            Icons.TwoTone.Monitor,
            Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
        )
        DriverClass.NET -> Pair(
            Icons.TwoTone.Wifi,
            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        )
        DriverClass.AUDIO -> Pair(
            Icons.AutoMirrored.TwoTone.VolumeUp,
            Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
        )
        DriverClass.BLUETOOTH -> Pair(
            Icons.TwoTone.Bluetooth,
            Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        )
        DriverClass.SYSTEM -> Pair(
            Icons.TwoTone.Memory,
            Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFC2410C)))
        )
        DriverClass.PERIPHERALS -> Pair(
            Icons.TwoTone.Keyboard,
            Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0E7490)))
        )
        DriverClass.OTHER -> Pair(
            Icons.TwoTone.DeveloperBoard,
            Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF334155)))
        )
    }
}
