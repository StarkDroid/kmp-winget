package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.twotone.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.twotone.Launch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
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
import com.velocity.kmpwinget.data.datasource.AppLauncher
import com.velocity.kmpwinget.data.datasource.Win32IconLoader
import com.velocity.kmpwinget.domain.model.FavoritesManager
import com.velocity.kmpwinget.domain.model.NotchPosition
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.theme.subtleIslandControl
import kotlinx.coroutines.launch

@Composable
fun SettingsIsland(
    allPackages: List<Package>,
    isDarkMode: Boolean
) {
    val isNotchEnabled by FavoritesManager.isNotchEnabled
    val currentPosition by FavoritesManager.notchPosition
    val favoriteIds by FavoritesManager.favoriteIds

    val favoritePackages = remember(favoriteIds, allPackages) {
        favoriteIds.mapNotNull { id -> allPackages.find { it.id == id } }
    }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Settings & Quick Launch",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Configure floating quick-launch notch overlay, monitor docking alignment, and manage pinned apps.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Card 1: Floating Notch Master Toggle
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isDarkMode) Color(0x228B5CF6) else Color(0x147C3AED)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.DynamicFeed,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF7C3AED)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Floating Quick-Launch Notch Overlay",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isNotchEnabled) {
                                                if (isDarkMode) AppColors.upgradeBadgeBgDark else AppColors.upgradeBadgeBgLight
                                            } else {
                                                if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isNotchEnabled) "Active" else "Disabled",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNotchEnabled) {
                                                if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                                            } else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "Display a minimalist floating notch at the top of your screen that expands with macOS dock magnification when hovered.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isNotchEnabled,
                        onCheckedChange = { FavoritesManager.setNotchEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)
                        )
                    )
                }
            }
        }

        // Card 2: Notch Screen Position Selector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isDarkMode) Color(0x2238BDF8) else Color(0x140284C7)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.DesktopWindows,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Notch Screen Position",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Choose where the floating notch capsule docks on your primary monitor display.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Position Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NotchPosition.entries.forEach { position ->
                            val isSelected = currentPosition == position
                            val chipBg = when {
                                isSelected -> if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                                else -> if (isDarkMode) Color(0x10FFFFFF) else Color(0x08000000)
                            }
                            val chipBorder = when {
                                isSelected -> if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                else -> if (isDarkMode) Color(0x1AFFFFFF) else Color(0x10000000)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                                    .clickable { FavoritesManager.setPosition(position) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (position) {
                                            NotchPosition.TOP_LEFT -> Icons.AutoMirrored.TwoTone.AlignHorizontalLeft
                                            NotchPosition.TOP_CENTER -> Icons.TwoTone.AlignHorizontalCenter
                                            NotchPosition.TOP_RIGHT -> Icons.AutoMirrored.TwoTone.AlignHorizontalRight
                                        },
                                        contentDescription = position.displayName,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) {
                                            if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                        } else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = position.displayName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                            } else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Pinned Favorite Apps Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pinned Favorite Applications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${favoritePackages.size} Pinned",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Applications pinned to the floating quick-launch notch overlay. You can star/unstar apps directly in the package list or remove them below.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        // Pinned Items or Empty State
        if (favoritePackages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .subtleIslandControl(shape = RoundedCornerShape(12.dp), isDarkMode = isDarkMode)
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkMode) Color(0x22FFD54F) else Color(0x1FFFCA28)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFFFFB800)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "No applications pinned yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Click the star (⭐) icon next to any installed application in the package list to pin it to your quick-launch notch.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        } else {
            items(
                items = favoritePackages,
                key = { it.uniqueId }
            ) { pkg ->
                PinnedAppRow(
                    pkg = pkg,
                    isDarkMode = isDarkMode,
                    onLaunch = {
                        coroutineScope.launch {
                            AppLauncher.launchPackage(pkg)
                        }
                    },
                    onUnpin = {
                        FavoritesManager.toggleFavorite(pkg.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun PinnedAppRow(
    pkg: Package,
    isDarkMode: Boolean,
    onLaunch: () -> Unit,
    onUnpin: () -> Unit
) {
    var appIcon by remember(pkg.iconPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pkg.iconPath) {
        if (!pkg.iconPath.isNullOrBlank()) {
            appIcon = Win32IconLoader.loadIcon(pkg.iconPath)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isHovered -> if (isDarkMode) AppColors.rowHoverDark else AppColors.rowHoverLight
        else -> if (isDarkMode) Color(0x0FFFFFFF) else Color(0x06000000)
    }

    val animatedBg by animateColorAsState(
        targetValue = bg,
        animationSpec = tween(120),
        label = "pinnedRowBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(
                1.dp,
                if (isDarkMode) Color(0x18FFFFFF) else Color(0x0E000000),
                RoundedCornerShape(10.dp)
            )
            .hoverable(interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon or Initial
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (appIcon != null) Modifier.background(Color.Transparent)
                        else Modifier.background(getSettingsAvatarGradient(pkg.name))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon!!,
                        contentDescription = pkg.name,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    val initial = pkg.name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
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

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (pkg.version.isNotEmpty()) "v${pkg.version}" else "Installed",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    SettingsSourceBadge(source = pkg.source, isDarkMode = isDarkMode)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Actions: Quick Launch + Unpin Star
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Launch Button
                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable { onLaunch() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.Launch,
                            contentDescription = "Launch",
                            modifier = Modifier.size(13.dp),
                            tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Launch",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        )
                    }
                }

                // Unpin Button
                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable { onUnpin() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Unpin",
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFFFFB800)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Unpin",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSourceBadge(source: PackageSource, isDarkMode: Boolean) {
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
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = source.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

private fun getSettingsAvatarGradient(name: String): Brush {
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
