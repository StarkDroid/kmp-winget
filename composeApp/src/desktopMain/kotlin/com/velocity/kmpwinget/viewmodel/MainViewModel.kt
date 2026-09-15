package com.velocity.kmpwinget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velocity.kmpwinget.domain.model.BackgroundQueueState
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDeduplicator
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.domain.model.SourceFilterOption
import com.velocity.kmpwinget.domain.model.TaskStatus
import com.velocity.kmpwinget.domain.model.UpdateTask
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
    private var queueWorkerJob: Job? = null

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
                if (intent.tab == NavigationTab.UPGRADES_AVAILABLE) {
                    resolveLocalAppUpdates()
                }
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
                val selectedIds = _uiState.value.selectedPackageIds
                val packagesToUpgrade = _uiState.value.allPackages.filter { selectedIds.contains(it.id) }
                if (packagesToUpgrade.isNotEmpty()) {
                    _uiState.update { it.copy(selectedPackageIds = emptySet(), isMultiSelectMode = false) }
                    enqueueBackgroundUpdates(packagesToUpgrade)
                }
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
            is MainUiIntent.EnqueueBackgroundUpdates -> {
                enqueueBackgroundUpdates(intent.packages)
            }
            is MainUiIntent.ToggleQueueExpanded -> {
                _uiState.update {
                    it.copy(
                        backgroundQueue = it.backgroundQueue.copy(isExpanded = !it.backgroundQueue.isExpanded)
                    )
                }
            }
            is MainUiIntent.DismissQueue -> {
                _uiState.update {
                    it.copy(backgroundQueue = BackgroundQueueState())
                }
            }
        }
    }

    private fun enqueueBackgroundUpdates(packages: List<Package>) {
        val newTasks = packages.map { UpdateTask(pkg = it) }
        _uiState.update { state ->
            val combinedTasks = state.backgroundQueue.tasks + newTasks
            state.copy(
                backgroundQueue = state.backgroundQueue.copy(
                    tasks = combinedTasks,
                    isRunning = true
                )
            )
        }
        processBackgroundQueue()
    }

    private fun processBackgroundQueue() {
        if (queueWorkerJob?.isActive == true) return

        queueWorkerJob = viewModelScope.launch {
            while (true) {
                val nextTask = _uiState.value.backgroundQueue.tasks.firstOrNull { it.status == TaskStatus.QUEUED }
                if (nextTask == null) {
                    _uiState.update {
                        it.copy(
                            backgroundQueue = it.backgroundQueue.copy(isRunning = false)
                        )
                    }
                    loadData(forceRefresh = true)
                    break
                }

                // Update task to IN_PROGRESS
                _uiState.update { state ->
                    val updated = state.backgroundQueue.tasks.map {
                        if (it.pkg.uniqueId == nextTask.pkg.uniqueId) {
                            it.copy(status = TaskStatus.IN_PROGRESS, message = "Downloading & Installing...")
                        } else it
                    }
                    state.copy(backgroundQueue = state.backgroundQueue.copy(tasks = updated, isRunning = true))
                }

                var isTaskSuccess = false
                val logsBuilder = StringBuilder()

                try {
                    packageRepository.upgradePackage(nextTask.pkg.id, nextTask.pkg.name, nextTask.pkg.matchedWingetId).collect { result ->
                        when (result) {
                            is OperationResult.Loading -> {
                                logsBuilder.append(result.logOutput)
                                _uiState.update { state ->
                                    val updated = state.backgroundQueue.tasks.map {
                                        if (it.pkg.uniqueId == nextTask.pkg.uniqueId) {
                                            it.copy(
                                                progress = result.currentProgress ?: 0.5f,
                                                message = result.message,
                                                logs = logsBuilder.toString()
                                            )
                                        } else it
                                    }
                                    state.copy(backgroundQueue = state.backgroundQueue.copy(tasks = updated))
                                }
                            }
                            is OperationResult.Success -> {
                                isTaskSuccess = true
                            }
                            is OperationResult.Error -> {
                                isTaskSuccess = false
                            }
                            else -> {}
                        }
                    }
                } catch (_: Exception) {
                    isTaskSuccess = false
                }

                // Mark task as COMPLETED or FAILED
                _uiState.update { state ->
                    val updated = state.backgroundQueue.tasks.map {
                        if (it.pkg.uniqueId == nextTask.pkg.uniqueId) {
                            it.copy(
                                status = if (isTaskSuccess) TaskStatus.COMPLETED else TaskStatus.FAILED,
                                message = if (isTaskSuccess) "Updated successfully" else "Failed to update",
                                progress = 1f
                            )
                        } else it
                    }
                    state.copy(backgroundQueue = state.backgroundQueue.copy(tasks = updated))
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

                        val deduplicatedAll = PackageDeduplicator.deduplicate(annotatedPackages.ifEmpty { upgrades })
                        val activeUpgrades = PackageDeduplicator.deduplicate(
                            if (deduplicatedAll.any { it.hasUpdate }) {
                                deduplicatedAll.filter { it.hasUpdate }
                            } else {
                                upgrades
                            }
                        )

                        state.copy(
                            allPackages = deduplicatedAll,
                            upgradablePackages = activeUpgrades
                        )
                    }
                    updateDisplayedPackages()
                    refreshSystemStats()
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

                        val deduplicatedAll = PackageDeduplicator.deduplicate(annotated)
                        val activeUpgrades = PackageDeduplicator.deduplicate(
                            deduplicatedAll.filter { it.hasUpdate }.ifEmpty { state.upgradablePackages }
                        )

                        state.copy(
                            allPackages = deduplicatedAll,
                            upgradablePackages = activeUpgrades,
                            isRefreshing = false
                        )
                    }
                    updateDisplayedPackages()
                    refreshSystemStats()

                    // As soon as all packages are loaded, resolve local/non-winget apps
                    resolveLocalAppUpdates()
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
                        val deduplicatedAll = PackageDeduplicator.deduplicate(updatedList)
                        state.copy(
                            allPackages = deduplicatedAll,
                            upgradablePackages = deduplicatedAll.filter { it.hasUpdate }
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

            state.copy(displayedPackages = PackageDeduplicator.deduplicate(sorted))
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
