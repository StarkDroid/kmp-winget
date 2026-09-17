package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.data.datasource.Win32IconLoader
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.DeleteUnderglowButton
import com.velocity.kmpwinget.theme.IslandRoundedCheckbox
import com.velocity.kmpwinget.theme.UpdateUnderglowButton

@Composable
fun PackageTableRow(
    pkg: Package,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isDarkMode: Boolean,
    onSelect: () -> Unit,
    onInspect: () -> Unit,
    onUpgrade: () -> Unit,
    onUninstall: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var appIcon by remember(pkg.iconPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pkg.iconPath) {
        if (!pkg.iconPath.isNullOrBlank()) {
            appIcon = Win32IconLoader.loadIcon(pkg.iconPath)
        }
    }

    val initial = pkg.name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    val avatarGradient = getAvatarGradient(pkg.name)

    val rowBg = when {
        isSelected -> if (isDarkMode) AppColors.rowSelectedDark else AppColors.rowSelectedLight
        isHovered -> if (isDarkMode) AppColors.rowHoverDark else AppColors.rowHoverLight
        else -> Color.Transparent
    }

    val animatedRowBg by animateColorAsState(
        targetValue = rowBg,
        animationSpec = tween(durationMillis = 120),
        label = "rowBg"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedRowBg)
                .hoverable(interactionSource)
                .clickable {
                    if (isMultiSelectMode) {
                        onSelect()
                    } else {
                        onInspect()
                    }
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Multi-Select Checkbox
            if (isMultiSelectMode) {
                IslandRoundedCheckbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    isDarkMode = isDarkMode,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            // Real App Icon or Initial Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (appIcon != null) Modifier.background(Color.Transparent) else Modifier.background(avatarGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon!!,
                        contentDescription = pkg.name,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Main Details: Title on Top, Tags on Line 2
            Column(modifier = Modifier.weight(1f)) {
                // Line 1: Clean App Title
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )

                Spacer(Modifier.height(3.dp))

                // Line 2: Publisher Name & Tags
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val publisherName = when {
                        !pkg.publisher.isNullOrBlank() -> pkg.publisher
                        pkg.source == PackageSource.MSSTORE -> "Microsoft Store"
                        pkg.id.contains(".") -> pkg.id.substringBefore(".")
                        else -> null
                    }

                    if (!publisherName.isNullOrBlank()) {
                        Text(
                            text = publisherName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }

                    // Source Tag Badge
                    SourceBadge(source = pkg.source, isDarkMode = isDarkMode)

                    // Update Ready Indicator Pill
                    if (pkg.hasUpdate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isDarkMode) AppColors.upgradeBadgeBgDark else AppColors.upgradeBadgeBgLight
                                )
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "Update Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                                )
                            )
                        }
                    }

                    if (!pkg.estimatedSize.isNullOrBlank()) {
                        Text(
                            text = "• ${pkg.estimatedSize}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Version Information Column
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                if (pkg.hasUpdate) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pkg.version.ifEmpty { "Current" },
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
                            text = pkg.availableVersion ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                            )
                        )
                    }
                } else {
                    Text(
                        text = if (pkg.version.isNotEmpty()) "v${pkg.version}" else "Installed",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Row Action Buttons
            if (!isMultiSelectMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (pkg.hasUpdate) {
                        UpdateUnderglowButton(
                            text = "Update",
                            onClick = onUpgrade,
                            isDarkMode = isDarkMode
                        )
                    }

                    DeleteUnderglowButton(
                        onClick = onUninstall,
                        isDarkMode = isDarkMode
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
private fun SourceBadge(source: PackageSource, isDarkMode: Boolean) {
    val (bg, textColor) = when (source) {
        PackageSource.WINGET -> Pair(
            if (isDarkMode) AppColors.badgeWingetBgDark else AppColors.badgeWingetBgLight,
            if (isDarkMode) AppColors.badgeWingetDark else AppColors.badgeWingetLight
        )
        PackageSource.MSSTORE -> Pair(
            if (isDarkMode) AppColors.badgeStoreBgDark else AppColors.badgeStoreBgLight,
            if (isDarkMode) AppColors.badgeStoreDark else AppColors.badgeStoreLight
        )
        PackageSource.LOCAL, PackageSource.UNKNOWN -> Pair(
            if (isDarkMode) AppColors.badgeLocalBgDark else AppColors.badgeLocalBgLight,
            if (isDarkMode) AppColors.badgeLocalDark else AppColors.badgeLocalLight
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    ) {
        Text(
            text = source.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

private fun getAvatarGradient(name: String): Brush {
    val hash = kotlin.math.abs(name.hashCode())
    val palettes = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
        listOf(Color(0xFF10B981), Color(0xFF047857)),
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        listOf(Color(0xFFF97316), Color(0xFFC2410C)),
        listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
        listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
    )
    val chosen = palettes[hash % palettes.size]
    return Brush.linearGradient(chosen)
}
