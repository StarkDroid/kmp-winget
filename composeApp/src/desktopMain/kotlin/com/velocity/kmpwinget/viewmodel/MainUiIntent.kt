package com.velocity.kmpwinget.viewmodel

import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.SourceFilterOption

sealed interface MainUiIntent {
    data class ChangeTab(val tab: NavigationTab) : MainUiIntent
    data class UpdateSearchQuery(val query: String) : MainUiIntent
    data class ChangeSortOption(val sort: PackageSortOption) : MainUiIntent
    data class ChangeSourceFilter(val filter: SourceFilterOption) : MainUiIntent
    data object ToggleMultiSelectMode : MainUiIntent
    data class TogglePackageSelection(val packageId: String) : MainUiIntent
    data object SelectAllDisplayed : MainUiIntent
    data object ClearSelection : MainUiIntent
    data class Refresh(val force: Boolean = true) : MainUiIntent
    data class RequestUpgrade(val pkg: Package) : MainUiIntent
    data class RequestUninstall(val pkg: Package) : MainUiIntent
    data class ConfirmUninstall(val pkg: Package) : MainUiIntent
    data object DismissUninstallConfirm : MainUiIntent
    data object RequestBatchUpgrade : MainUiIntent
    data object RequestBatchUninstall : MainUiIntent
    data object ConfirmBatchUninstall : MainUiIntent
    data object DismissBatchUninstallConfirm : MainUiIntent
    data object ClearOperationResult : MainUiIntent
    data object LaunchDiskCleanup : MainUiIntent
    data object OptimizeSystem : MainUiIntent
    data class EnqueueBackgroundUpdates(val packages: List<Package>) : MainUiIntent
    data object ToggleQueueExpanded : MainUiIntent
    data object DismissQueue : MainUiIntent
    data class RequestDriverUpdate(val driver: DriverPackage) : MainUiIntent
    data object RescanPnpDevices : MainUiIntent
    data class FilterDriversByClass(val driverClass: DriverClass?) : MainUiIntent
}
