package com.velocity.kmpwinget.viewmodel

import com.velocity.kmpwinget.domain.model.BackgroundQueueState
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.SourceFilterOption
import com.velocity.kmpwinget.domain.model.SystemStats

data class MainUiState(
    val allPackages: List<Package> = emptyList(),
    val upgradablePackages: List<Package> = emptyList(),
    val displayedPackages: List<Package> = emptyList(),
    val selectedPackageIds: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val activeTab: NavigationTab = NavigationTab.ALL_PACKAGES,
    val searchQuery: String = "",
    val sortOption: PackageSortOption = PackageSortOption.NAME_ASC,
    val sourceFilter: SourceFilterOption = SourceFilterOption.ALL,
    val isRefreshing: Boolean = false,
    val operationResult: OperationResult = OperationResult.Idle,
    val backgroundQueue: BackgroundQueueState = BackgroundQueueState(),
    val systemStats: SystemStats = SystemStats(),
    val packageToConfirmUninstall: Package? = null,
    val batchUninstallConfirm: Boolean = false
) {
    val totalInstalledCount: Int
        get() = allPackages.size

    val updatesCount: Int
        get() = upgradablePackages.size

    val selectedCount: Int
        get() = selectedPackageIds.size

    val isAllSelected: Boolean
        get() = displayedPackages.isNotEmpty() && displayedPackages.all { selectedPackageIds.contains(it.id) }
}
