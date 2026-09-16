package com.velocity.kmpwinget.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.AppTheme
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.ui.components.*
import com.velocity.kmpwinget.viewmodel.MainUiIntent
import com.velocity.kmpwinget.viewmodel.MainViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen() {
    val viewModel = koinInject<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    val isDarkMode = ThemeState.isDarkMode.value
    val listState = rememberLazyListState()

    AppTheme(isDarkTheme = isDarkMode) {
        // Operation Progress & Live Logs Modal Dialog (Used for single foreground actions)
        if (uiState.operationResult !is OperationResult.Idle) {
            OperationDialog(
                result = uiState.operationResult,
                isDarkMode = isDarkMode,
                onDismiss = { viewModel.onIntent(MainUiIntent.ClearOperationResult) }
            )
        }

        // Single Package Uninstall Confirmation Dialog
        if (uiState.packageToConfirmUninstall != null) {
            ConfirmUninstallDialog(
                pkg = uiState.packageToConfirmUninstall,
                isBatch = false,
                isDarkMode = isDarkMode,
                onConfirm = {
                    uiState.packageToConfirmUninstall?.let {
                        viewModel.onIntent(MainUiIntent.ConfirmUninstall(it))
                    }
                },
                onDismiss = { viewModel.onIntent(MainUiIntent.DismissUninstallConfirm) }
            )
        }

        // Driver Delete Confirmation Dialog
        if (uiState.driverToConfirmDelete != null) {
            ConfirmDriverDeleteDialog(
                driver = uiState.driverToConfirmDelete,
                isDarkMode = isDarkMode,
                onConfirm = {
                    uiState.driverToConfirmDelete?.let {
                        viewModel.onIntent(MainUiIntent.ConfirmDriverDelete(it))
                    }
                },
                onDismiss = { viewModel.onIntent(MainUiIntent.DismissDriverDeleteConfirm) }
            )
        }

        // Batch Uninstall Confirmation Dialog
        if (uiState.batchUninstallConfirm && uiState.selectedCount > 0) {
            ConfirmUninstallDialog(
                pkg = null,
                isBatch = true,
                batchCount = uiState.selectedCount,
                isDarkMode = isDarkMode,
                onConfirm = { viewModel.onIntent(MainUiIntent.ConfirmBatchUninstall) },
                onDismiss = { viewModel.onIntent(MainUiIntent.DismissBatchUninstallConfirm) }
            )
        }

        // Main Background Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Island 1: App Header Island
                AppHeaderIsland(
                    isDarkMode = isDarkMode,
                    wingetVersion = uiState.systemStats.wingetVersion
                )

                // Island 2: Navigation & Controls Island (Tabs with Luminous Underglow + Search/Filters)
                NavControlsIsland(
                    activeTab = uiState.activeTab,
                    totalInstalled = uiState.totalInstalledCount,
                    updatesAvailable = uiState.updatesCount,
                    totalDrivers = uiState.totalDriversCount,
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onIntent(MainUiIntent.UpdateSearchQuery(it)) },
                    sourceFilter = uiState.sourceFilter,
                    onSourceFilterChange = { viewModel.onIntent(MainUiIntent.ChangeSourceFilter(it)) },
                    sortOption = uiState.sortOption,
                    onSortOptionChange = { viewModel.onIntent(MainUiIntent.ChangeSortOption(it)) },
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    onToggleMultiSelect = { viewModel.onIntent(MainUiIntent.ToggleMultiSelectMode) },
                    isRefreshing = uiState.isRefreshing,
                    isDarkMode = isDarkMode,
                    onTabSelected = { viewModel.onIntent(MainUiIntent.ChangeTab(it)) },
                    onRefresh = { viewModel.onIntent(MainUiIntent.Refresh(force = true)) }
                )

                // Island 3: Main Data Content Island (Table Rows, Device Drivers, or System Tools)
                AnimatedContent(
                    targetState = uiState.activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "mainIslandContent",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { targetTab ->
                    when (targetTab) {
                        NavigationTab.SYSTEM_TOOLS -> {
                            SystemToolsIsland(
                                stats = uiState.systemStats,
                                isDarkMode = isDarkMode,
                                onLaunchDiskCleanup = { viewModel.onIntent(MainUiIntent.LaunchDiskCleanup) },
                                onOptimizeWinget = { viewModel.onIntent(MainUiIntent.OptimizeSystem) }
                            )
                        }
                        NavigationTab.DRIVERS -> {
                            DriversIsland(
                                drivers = uiState.drivers,
                                displayedDrivers = uiState.displayedDrivers,
                                selectedClass = uiState.driverClassFilter,
                                searchQuery = uiState.searchQuery,
                                isScanning = uiState.isScanningDrivers,
                                isRefreshing = uiState.isRefreshing,
                                isDarkMode = isDarkMode,
                                onSearchQueryChange = { viewModel.onIntent(MainUiIntent.UpdateSearchQuery(it)) },
                                onClassFilterChange = { viewModel.onIntent(MainUiIntent.FilterDriversByClass(it)) },
                                onScanDevices = { viewModel.onIntent(MainUiIntent.RescanPnpDevices) },
                                onRefresh = { viewModel.onIntent(MainUiIntent.Refresh(force = true)) },
                                onUpdateDriver = { viewModel.onIntent(MainUiIntent.RequestDriverUpdate(it)) },
                                onDeleteDriver = { viewModel.onIntent(MainUiIntent.RequestDriverDelete(it)) }
                            )
                        }
                        NavigationTab.ALL_PACKAGES, NavigationTab.UPGRADES_AVAILABLE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Table Column Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Package",
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
                                                    text = "${uiState.displayedPackages.size}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (targetTab == NavigationTab.UPGRADES_AVAILABLE) "Available Version" else "Installed Version",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(end = if (uiState.isMultiSelectMode) 12.dp else 80.dp)
                                        )
                                    }

                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
                                    )

                                    // Table Content & Loading Indicators
                                    if (targetTab == NavigationTab.UPGRADES_AVAILABLE && uiState.isCheckingUpdates) {
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
                                                    text = "Checking for available updates...",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "Scanning WinGet catalog and local applications",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    } else if (uiState.displayedPackages.isEmpty() && uiState.isRefreshing) {
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
                                                    text = "Loading installed applications...",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "Discovering packages and software metadata",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    } else if (uiState.displayedPackages.isEmpty()) {
                                        EmptyStateView(
                                            activeTab = targetTab,
                                            searchQuery = uiState.searchQuery,
                                            isDarkMode = isDarkMode,
                                            onClearSearch = { viewModel.onIntent(MainUiIntent.UpdateSearchQuery("")) },
                                            onRefresh = { viewModel.onIntent(MainUiIntent.Refresh(force = true)) }
                                        )
                                    } else {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            LazyColumn(
                                                state = listState,
                                                modifier = Modifier.fillMaxSize().padding(end = 6.dp),
                                                contentPadding = PaddingValues(bottom = if (uiState.isMultiSelectMode) 76.dp else 8.dp)
                                            ) {
                                                items(
                                                    items = uiState.displayedPackages,
                                                    key = { pkg -> pkg.uniqueId }
                                                ) { pkg ->
                                                    PackageTableRow(
                                                        pkg = pkg,
                                                        isSelected = uiState.selectedPackageIds.contains(pkg.id),
                                                        isMultiSelectMode = uiState.isMultiSelectMode,
                                                        isDarkMode = isDarkMode,
                                                        onSelect = {
                                                            viewModel.onIntent(
                                                                MainUiIntent.TogglePackageSelection(pkg.id)
                                                            )
                                                        },
                                                        onUpgrade = {
                                                            viewModel.onIntent(MainUiIntent.RequestUpgrade(pkg))
                                                        },
                                                        onUninstall = {
                                                            viewModel.onIntent(MainUiIntent.RequestUninstall(pkg))
                                                        }
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

                                // Selection Bar at the bottom of the Island
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                ) {
                                    SelectionBarIsland(
                                        isVisible = uiState.isMultiSelectMode,
                                        selectedCount = uiState.selectedCount,
                                        isAllSelected = uiState.isAllSelected,
                                        isDarkMode = isDarkMode,
                                        onSelectAllToggle = { viewModel.onIntent(MainUiIntent.SelectAllDisplayed) },
                                        onBatchUpgrade = { viewModel.onIntent(MainUiIntent.RequestBatchUpgrade) },
                                        onBatchUninstall = { viewModel.onIntent(MainUiIntent.RequestBatchUninstall) },
                                        onCancel = { viewModel.onIntent(MainUiIntent.ToggleMultiSelectMode) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Background Queue Task Island (Non-blocking background updater)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
            ) {
                BackgroundQueueIsland(
                    queueState = uiState.backgroundQueue,
                    isDarkMode = isDarkMode,
                    onToggleExpand = { viewModel.onIntent(MainUiIntent.ToggleQueueExpanded) },
                    onDismiss = { viewModel.onIntent(MainUiIntent.DismissQueue) }
                )
            }
        }
    }
}
