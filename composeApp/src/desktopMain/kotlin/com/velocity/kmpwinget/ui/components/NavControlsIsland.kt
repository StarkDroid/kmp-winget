package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Sort
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.SourceFilterOption
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.theme.subtleIslandControl
import com.velocity.kmpwinget.theme.tabUnderglow

@Composable
fun NavControlsIsland(
    activeTab: NavigationTab,
    totalInstalled: Int,
    updatesAvailable: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    sourceFilter: SourceFilterOption,
    onSourceFilterChange: (SourceFilterOption) -> Unit,
    sortOption: PackageSortOption,
    onSortOptionChange: (PackageSortOption) -> Unit,
    isMultiSelectMode: Boolean,
    onToggleMultiSelect: () -> Unit,
    isRefreshing: Boolean,
    isDarkMode: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    onRefresh: () -> Unit
) {
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var isFilterMenuOpen by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "refreshAnim")
    val refreshAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Navigation Tabs with Aesthetic Luminous Underglow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDarkMode) Color(0x22000000) else Color(0x0A000000)
                )
                .border(
                    1.dp,
                    if (isDarkMode) Color(0x1FFFFFFF) else Color(0x0F000000),
                    RoundedCornerShape(12.dp)
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlowingNavTabItem(
                modifier = Modifier.weight(1f),
                title = "All Installed Apps",
                count = totalInstalled,
                icon = Icons.TwoTone.Apps,
                isSelected = activeTab == NavigationTab.ALL_PACKAGES,
                isDarkMode = isDarkMode,
                onClick = { onTabSelected(NavigationTab.ALL_PACKAGES) }
            )

            GlowingNavTabItem(
                modifier = Modifier.weight(1f),
                title = "Updates Available",
                count = updatesAvailable,
                icon = Icons.TwoTone.SystemUpdateAlt,
                isSelected = activeTab == NavigationTab.UPGRADES_AVAILABLE,
                isDarkMode = isDarkMode,
                isHighlight = updatesAvailable > 0,
                onClick = { onTabSelected(NavigationTab.UPGRADES_AVAILABLE) }
            )

            GlowingNavTabItem(
                modifier = Modifier.weight(1f),
                title = "System & Maintenance",
                count = null,
                icon = Icons.TwoTone.Build,
                isSelected = activeTab == NavigationTab.SYSTEM_TOOLS,
                isDarkMode = isDarkMode,
                onClick = { onTabSelected(NavigationTab.SYSTEM_TOOLS) }
            )
        }

        // Row 2: Search Box & Controls Bar (Only for Package Lists)
        if (activeTab != NavigationTab.SYSTEM_TOOLS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Input Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) Color(0x33000000) else Color(0x08000000)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search installed packages by name, ID, or publisher...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (query.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.TwoTone.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onQueryChange("") },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Source Filter Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .subtleIslandControl(
                                shape = RoundedCornerShape(8.dp),
                                isDarkMode = isDarkMode,
                                selected = sourceFilter != SourceFilterOption.ALL
                            )
                            .clickable { isFilterMenuOpen = true }
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.TwoTone.FilterList,
                                contentDescription = "Filter",
                                modifier = Modifier.size(15.dp),
                                tint = if (sourceFilter != SourceFilterOption.ALL) {
                                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                } else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = sourceFilter.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (sourceFilter != SourceFilterOption.ALL) {
                                        if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.TwoTone.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isFilterMenuOpen,
                        onDismissRequest = { isFilterMenuOpen = false },
                        modifier = Modifier.background(
                            if (isDarkMode) AppColors.islandSurfaceDark else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        SourceFilterOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (sourceFilter == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sourceFilter == option) {
                                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                            } else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                },
                                onClick = {
                                    onSourceFilterChange(option)
                                    isFilterMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Sort Options Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .subtleIslandControl(
                                shape = RoundedCornerShape(8.dp),
                                isDarkMode = isDarkMode
                            )
                            .clickable { isSortMenuOpen = true }
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = sortOption.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.TwoTone.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false },
                        modifier = Modifier.background(
                            if (isDarkMode) AppColors.islandSurfaceDark else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        PackageSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOption == option) {
                                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                            } else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(option)
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Multi-Select Mode Toggle
                Box(
                    modifier = Modifier
                        .subtleIslandControl(
                            shape = RoundedCornerShape(8.dp),
                            isDarkMode = isDarkMode,
                            selected = isMultiSelectMode
                        )
                        .clickable { onToggleMultiSelect() }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMultiSelectMode) Icons.TwoTone.CheckCircle else Icons.TwoTone.Checklist,
                            contentDescription = "Select Mode",
                            modifier = Modifier.size(15.dp),
                            tint = if (isMultiSelectMode) {
                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            } else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isMultiSelectMode) "Done" else "Select",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isMultiSelectMode) {
                                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                } else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Refresh Button
                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable(enabled = !isRefreshing) { onRefresh() }
                        .padding(9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier
                            .size(16.dp)
                            .let { if (isRefreshing) it.rotate(refreshAngle) else it },
                        tint = if (isRefreshing) {
                            if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GlowingNavTabItem(
    modifier: Modifier = Modifier,
    title: String,
    count: Int?,
    icon: ImageVector,
    isSelected: Boolean,
    isDarkMode: Boolean,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val activeBg = if (isDarkMode) {
        AppColors.islandSurfaceDark
    } else {
        Color.White
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) activeBg else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "tabBg"
    )

    val textColor = when {
        isSelected -> if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val glowColor = if (isHighlight) {
        AppColors.upgradeGlow
    } else {
        if (isDarkMode) AppColors.glowAccentDark else AppColors.glowAccentLight
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(
                1.dp,
                if (isSelected) {
                    if (isDarkMode) Color(0x33FFFFFF) else Color(0x1F000000)
                } else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .tabUnderglow(
                isSelected = isSelected,
                isDarkMode = isDarkMode,
                glowColor = glowColor
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 9.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
            )

            if (count != null) {
                Spacer(Modifier.width(8.dp))

                val badgeBg = when {
                    isHighlight -> if (isDarkMode) AppColors.upgradeBadgeBgDark else AppColors.upgradeBadgeBgLight
                    isSelected -> if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                    else -> if (isDarkMode) Color(0x22FFFFFF) else Color(0x10000000)
                }

                val badgeText = when {
                    isHighlight -> if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                    isSelected -> if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = badgeText
                        )
                    )
                }
            }
        }
    }
}
