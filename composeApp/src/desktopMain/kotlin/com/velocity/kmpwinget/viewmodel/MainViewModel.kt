package com.velocity.kmpwinget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.domain.model.SourceFilterOption
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import com.velocity.kmpwinget.domain.usecase.BatchOperationUseCase
import com.velocity.kmpwinget.domain.usecase.GetPackagesUseCase
import com.velocity.kmpwinget.domain.usecase.SystemToolsUseCase
import com.velocity.kmpwinget.domain.usecase.UninstallPackageUseCase
import com.velocity.kmpwinget.domain.usecase.UpgradePackageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val getPackagesUseCase: GetPackagesUseCase,
    private val upgradePackageUseCase: UpgradePackageUseCase,
    private val uninstallPackageUseCase: UninstallPackageUseCase,
    private val batchOperationUseCase: BatchOperationUseCase,
    private val systemToolsUseCase: SystemToolsUseCase,
    private val packageRepository: IPackageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var packagesJob: Job? = null
    private var upgradesJob: Job? = null
    private var localResolutionJob: Job? = null

    // Authoritative in-memory cache of package ID to available upgrade version
    private val knownUpgradeMap = mutableMapOf<String, String>()

    init {
        loadData(forceRefresh = false)
    }

    fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.ChangeTab -> {
                _uiState.update { it.copy(activeTab = intent.tab) }
                updateDisplayedPackages()
            }
            is MainUiIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
                updateDisplayedPackages()
            }
            is MainUiIntent.ChangeSortOption -> {
                _uiState.update { it.copy(sortOption = intent.sort) }
                updateDisplayedPackages()
            }
            is MainUiIntent.ChangeSourceFilter -> {
                _uiState.update { it.copy(sourceFilter = intent.filter) }
                updateDisplayedPackages()
            }
            is MainUiIntent.ToggleMultiSelectMode -> {
                _uiState.update {
                    val newMode = !it.isMultiSelectMode
                    it.copy(
                        isMultiSelectMode = newMode,
                        selectedPackageIds = if (newMode) it.selectedPackageIds else emptySet()
                    )
                }
            }
            is MainUiIntent.TogglePackageSelection -> {
                _uiState.update { state ->
                    val set = state.selectedPackageIds.toMutableSet()
                    if (set.contains(intent.packageId)) {
                        set.remove(intent.packageId)
                    } else {
                        set.add(intent.packageId)
                    }
                    state.copy(selectedPackageIds = set)
                }
            }
            is MainUiIntent.SelectAllDisplayed -> {
                _uiState.update { state ->
                    val allIds = state.displayedPackages.map { it.id }.toSet()
                    val newSelection = if (state.isAllSelected) emptySet() else allIds
                    state.copy(selectedPackageIds = newSelection)
                }
            }
            is MainUiIntent.ClearSelection -> {
                _uiState.update { it.copy(selectedPackageIds = emptySet()) }
            }
            is MainUiIntent.Refresh -> {
                loadData(forceRefresh = intent.force)
            }
            is MainUiIntent.RequestUpgrade -> {
                upgradeSingle(intent.pkg)
            }
            is MainUiIntent.RequestUninstall -> {
                _uiState.update { it.copy(packageToConfirmUninstall = intent.pkg) }
            }
            is MainUiIntent.ConfirmUninstall -> {
                _uiState.update { it.copy(packageToConfirmUninstall = null) }
                uninstallSingle(intent.pkg)
            }
            is MainUiIntent.DismissUninstallConfirm -> {
                _uiState.update { it.copy(packageToConfirmUninstall = null) }
            }
            is MainUiIntent.RequestBatchUpgrade -> {
                upgradeBatch()
            }
            is MainUiIntent.RequestBatchUninstall -> {
                _uiState.update { it.copy(batchUninstallConfirm = true) }
            }
            is MainUiIntent.ConfirmBatchUninstall -> {
                _uiState.update { it.copy(batchUninstallConfirm = false) }
                uninstallBatch()
            }
            is MainUiIntent.DismissBatchUninstallConfirm -> {
                _uiState.update { it.copy(batchUninstallConfirm = false) }
            }
            is MainUiIntent.ClearOperationResult -> {
                _uiState.update { it.copy(operationResult = OperationResult.Idle) }
            }
            is MainUiIntent.LaunchDiskCleanup -> {
                viewModelScope.launch {
                    val result = systemToolsUseCase.openDiskCleanup()
                    _uiState.update { it.copy(operationResult = result) }
                }
            }
            is MainUiIntent.OptimizeSystem -> {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            operationResult = OperationResult.Loading(
                                title = "Optimizing System",
                                message = "Refreshing winget sources and package caches..."
                            )
                        )
                    }
                    val result = systemToolsUseCase.optimizeSystem()
                    _uiState.update { it.copy(operationResult = result) }
                    loadData(forceRefresh = true)
                }
            }
        }
    }

    private fun loadData(forceRefresh: Boolean) {
        if (forceRefresh) {
            knownUpgradeMap.clear()
        }

        _uiState.update { it.copy(isRefreshing = true) }

        // 1. Launch WinGet upgrade scan
        upgradesJob?.cancel()
        upgradesJob = viewModelScope.launch {
            try {
                getPackagesUseCase.execute(
                    showUpgradesOnly = true,
                    forceRefresh = forceRefresh
                ).collect { upgrades ->
                    upgrades.forEach { upg ->
                        if (!upg.availableVersion.isNullOrBlank()) {
                            knownUpgradeMap[upg.id] = upg.availableVersion
                        }
                    }

                    _uiState.update { state ->
                        val annotatedPackages = state.allPackages.map { pkg ->
                            if (knownUpgradeMap.containsKey(pkg.id)) {
                                pkg.copy(availableVersion = knownUpgradeMap[pkg.id])
                            } else {
                                pkg
                            }
                        }

                        val activeUpgrades = if (annotatedPackages.any { it.hasUpdate }) {
                            annotatedPackages.filter { it.hasUpdate }
                        } else {
                            upgrades
                        }

                        state.copy(
                            allPackages = annotatedPackages.ifEmpty { upgrades },
                            upgradablePackages = activeUpgrades
                        )
                    }
                    updateDisplayedPackages()
                    refreshSystemStats()

                    // Start background resolution for non-winget/local applications
                    resolveLocalAppUpdates()
                }
            } catch (_: Exception) {}
        }

        // 2. Launch full installed packages scan
        packagesJob?.cancel()
        packagesJob = viewModelScope.launch {
            try {
                getPackagesUseCase.execute(
                    showUpgradesOnly = false,
                    forceRefresh = forceRefresh
                ).collect { packages ->
                    _uiState.update { state ->
                        val annotated = packages.map { pkg ->
                            if (knownUpgradeMap.containsKey(pkg.id)) {
                                pkg.copy(availableVersion = knownUpgradeMap[pkg.id])
                            } else {
                                pkg
                            }
                        }

                        state.copy(
                            allPackages = annotated,
                            upgradablePackages = annotated.filter { it.hasUpdate }.ifEmpty { state.upgradablePackages },
                            isRefreshing = false
                        )
                    }
                    updateDisplayedPackages()
                    refreshSystemStats()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        operationResult = OperationResult.Error("Failed to load packages: ${e.message}")
                    )
                }
            }
        }
    }

    private fun resolveLocalAppUpdates() {
        localResolutionJob?.cancel()
        localResolutionJob = viewModelScope.launch {
            val localApps = _uiState.value.allPackages.filter { it.isLocal && !it.hasUpdate }
            if (localApps.isNotEmpty()) {
                val resolvedApps = packageRepository.resolveUpdatesForLocalPackages(localApps)
                val resolvedMap = resolvedApps.filter { it.hasUpdate }.associateBy { it.id }

                if (resolvedMap.isNotEmpty()) {
                    resolvedMap.forEach { (id, pkg) ->
                        if (!pkg.availableVersion.isNullOrBlank()) {
                            knownUpgradeMap[id] = pkg.availableVersion
                        }
                    }

                    _uiState.update { state ->
                        val updatedList = state.allPackages.map { pkg ->
                            resolvedMap[pkg.id] ?: pkg
                        }
                        state.copy(
                            allPackages = updatedList,
                            upgradablePackages = updatedList.filter { it.hasUpdate }
                        )
                    }
                    updateDisplayedPackages()
                    refreshSystemStats()
                }
            }
        }
    }

    private fun updateDisplayedPackages() {
        _uiState.update { state ->
            val sourceList = when (state.activeTab) {
                NavigationTab.ALL_PACKAGES -> state.allPackages
                NavigationTab.UPGRADES_AVAILABLE -> state.allPackages.filter { it.hasUpdate }.ifEmpty { state.upgradablePackages }
                NavigationTab.SYSTEM_TOOLS -> emptyList()
            }

            val query = state.searchQuery.trim().lowercase()
            val filtered = sourceList.filter { pkg ->
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    pkg.name.lowercase().contains(query) ||
                            pkg.id.lowercase().contains(query) ||
                            (pkg.publisher?.lowercase()?.contains(query) == true)
                }

                val matchesSource = when (state.sourceFilter) {
                    SourceFilterOption.ALL -> true
                    SourceFilterOption.WINGET_ONLY -> pkg.source == PackageSource.WINGET
                    SourceFilterOption.MSSTORE_ONLY -> pkg.source == PackageSource.MSSTORE
                    SourceFilterOption.LOCAL_ONLY -> pkg.source == PackageSource.LOCAL
                }

                matchesQuery && matchesSource
            }

            val sorted = when (state.sortOption) {
                PackageSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
                PackageSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
                PackageSortOption.UPDATES_FIRST -> filtered.sortedWith(
                    compareByDescending<Package> { it.hasUpdate }
                        .thenBy { it.name.lowercase() }
                )
                PackageSortOption.SOURCE -> filtered.sortedWith(
                    compareBy<Package> { it.source.name }
                        .thenBy { it.name.lowercase() }
                )
            }

            state.copy(displayedPackages = sorted)
        }
    }

    private fun refreshSystemStats() {
        viewModelScope.launch {
            val count = _uiState.value.totalInstalledCount
            val updates = _uiState.value.updatesCount
            val stats = systemToolsUseCase.getSystemStats(count, updates)
            _uiState.update { it.copy(systemStats = stats) }
        }
    }

    private fun upgradeSingle(pkg: Package) {
        viewModelScope.launch {
            upgradePackageUseCase.execute(pkg).collect { result ->
                _uiState.update { it.copy(operationResult = result) }
                if (result is OperationResult.Success) {
                    loadData(forceRefresh = true)
                }
            }
        }
    }

    private fun uninstallSingle(pkg: Package) {
        viewModelScope.launch {
            uninstallPackageUseCase.execute(pkg.id, pkg.name).collect { result ->
                _uiState.update { it.copy(operationResult = result) }
                if (result is OperationResult.Success) {
                    loadData(forceRefresh = true)
                }
            }
        }
    }

    private fun upgradeBatch() {
        val selectedIds = _uiState.value.selectedPackageIds
        val packagesToUpgrade = _uiState.value.allPackages.filter { selectedIds.contains(it.id) }
        if (packagesToUpgrade.isEmpty()) return

        viewModelScope.launch {
            batchOperationUseCase.upgradeMultiple(packagesToUpgrade).collect { result ->
                _uiState.update { it.copy(operationResult = result) }
                if (result is OperationResult.Success) {
                    _uiState.update { it.copy(selectedPackageIds = emptySet(), isMultiSelectMode = false) }
                    loadData(forceRefresh = true)
                }
            }
        }
    }

    private fun uninstallBatch() {
        val selectedIds = _uiState.value.selectedPackageIds
        val packagesToUninstall = _uiState.value.allPackages.filter { selectedIds.contains(it.id) }
        if (packagesToUninstall.isEmpty()) return

        viewModelScope.launch {
            batchOperationUseCase.uninstallMultiple(packagesToUninstall).collect { result ->
                _uiState.update { it.copy(operationResult = result) }
                if (result is OperationResult.Success) {
                    _uiState.update { it.copy(selectedPackageIds = emptySet(), isMultiSelectMode = false) }
                    loadData(forceRefresh = true)
                }
            }
        }
    }
}
