package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.theme.subtleIslandControl

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriversIsland(
    drivers: List<DriverPackage>,
    displayedDrivers: List<DriverPackage>,
    selectedClass: DriverClass?,
    searchQuery: String,
    isScanning: Boolean,
    isRefreshing: Boolean,
    isDarkMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClassFilterChange: (DriverClass?) -> Unit,
    onScanDevices: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateDriver: (DriverPackage) -> Unit,
    onDeleteDriver: (DriverPackage) -> Unit
) {
    val listState = rememberLazyListState()
    var isFilterMenuOpen by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "driverRefreshAnim")
    val refreshAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driverRefreshRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Section 1: Island Controls Bar (Search, Filter Chips, Scan, Refresh)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Driver Search Box
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
                                contentDescription = "Search Drivers",
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.width(8.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search drivers by name, provider, class, or INF file...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                            fontSize = 13.sp
                                        )
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
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

                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.TwoTone.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onSearchQueryChange("") },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Driver Class Filter Dropdown Menu
                    Box {
                        Box(
                            modifier = Modifier
                                .subtleIslandControl(
                                    shape = RoundedCornerShape(8.dp),
                                    isDarkMode = isDarkMode,
                                    selected = selectedClass != null
                                )
                                .clickable { isFilterMenuOpen = true }
                                .padding(horizontal = 10.dp, vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.TwoTone.FilterList,
                                    contentDescription = "Class Filter",
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selectedClass != null) {
                                        if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = selectedClass?.displayName ?: "All Classes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedClass != null) {
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
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "All Classes",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedClass == null) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedClass == null) {
                                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                            } else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                },
                                onClick = {
                                    onClassFilterChange(null)
                                    isFilterMenuOpen = false
                                }
                            )

                            DriverClass.entries.forEach { driverClass ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = driverClass.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (selectedClass == driverClass) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedClass == driverClass) {
                                                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                                } else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    },
                                    onClick = {
                                        onClassFilterChange(driverClass)
                                        isFilterMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Scan Plug & Play Devices Button
                    Box(
                        modifier = Modifier
                            .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                            .clickable(enabled = !isScanning) { onScanDevices() }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.TwoTone.Sensors,
                                contentDescription = "Scan Devices",
                                modifier = Modifier
                                    .size(15.dp)
                                    .let { if (isScanning) it.rotate(refreshAngle) else it },
                                tint = if (isScanning) {
                                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                } else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isScanning) "Scanning..." else "Scan Devices",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
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
                            contentDescription = "Refresh Drivers",
                            modifier = Modifier
                                .size(16.dp)
                                .let { if (isRefreshing) it.rotate(refreshAngle) else it },
                            tint = if (isRefreshing) {
                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            } else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Quick Filter Chips (All, GPU, Network, Audio, Bluetooth, Chipset, Peripherals)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DriverChipItem(
                        label = "All (${drivers.size})",
                        isSelected = selectedClass == null,
                        isDarkMode = isDarkMode,
                        onClick = { onClassFilterChange(null) }
                    )

                    DriverClass.entries.forEach { driverClass ->
                        val count = drivers.count { it.driverClass == driverClass }
                        val label = when (driverClass) {
                            DriverClass.DISPLAY -> "GPU / Display"
                            DriverClass.NET -> "Network"
                            DriverClass.AUDIO -> "Audio"
                            DriverClass.BLUETOOTH -> "Bluetooth"
                            DriverClass.SYSTEM -> "Chipset"
                            DriverClass.PERIPHERALS -> "Peripherals"
                            DriverClass.OTHER -> "Other"
                        }

                        DriverChipItem(
                            label = "$label ($count)",
                            isSelected = selectedClass == driverClass,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onClassFilterChange(if (selectedClass == driverClass) null else driverClass)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
            )

            // Section 2: Table Column Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Device Driver",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${displayedDrivers.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        )
                    }
                }

                Text(
                    text = "Driver Version & Date",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(end = 160.dp)
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
            )

            // Section 3: Driver Rows List or Empty / Loading State
            if (displayedDrivers.isEmpty() && isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Enumerating device drivers...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Scanning OEM INF repository and hardware state",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else if (displayedDrivers.isEmpty()) {
                EmptyStateView(
                    activeTab = NavigationTab.DRIVERS,
                    searchQuery = searchQuery,
                    isDarkMode = isDarkMode,
                    onClearSearch = { onSearchQueryChange("") },
                    onRefresh = onRefresh
                )
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(end = 6.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(
                            items = displayedDrivers,
                            key = { driver -> driver.uniqueId }
                        ) { driver ->
                            DriverTableRow(
                                driver = driver,
                                isDarkMode = isDarkMode,
                                onUpdate = { onUpdateDriver(driver) },
                                onDelete = { onDeleteDriver(driver) }
                            )
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                        adapter = rememberScrollbarAdapter(scrollState = listState),
                        style = ScrollbarStyle(
                            shape = RoundedCornerShape(4.dp),
                            minimalHeight = 40.dp,
                            thickness = 6.dp,
                            unhoverColor = if (isDarkMode) Color(0x26FFFFFF) else Color(0x1F000000),
                            hoverColor = if (isDarkMode) AppColors.primaryDark.copy(alpha = 0.8f) else AppColors.primaryLight.copy(alpha = 0.8f),
                            hoverDurationMillis = 150
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverChipItem(
    label: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .subtleIslandControl(
                shape = RoundedCornerShape(20.dp),
                isDarkMode = isDarkMode,
                selected = isSelected
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isSelected) {
                    if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                } else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
