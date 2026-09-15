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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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

    private var scanJob: Job? = null
    private var queueWorkerJob: Job? = null
    private var telemetryJob: Job? = null

    // Authoritative in-memory cache of package ID to available upgrade version
    private val knownUpgradeMap = mutableMapOf<String, String>()

    init {
        refreshSystemStats()
        loadData(forceRefresh = false)
    }

    fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.ChangeTab -> {
                _uiState.update { it.copy(activeTab = intent.tab) }
                updateDisplayedPackages()

                if (intent.tab == NavigationTab.SYSTEM_TOOLS) {
                    startTelemetryPolling()
                } else {
                    telemetryJob?.cancel()
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
                    // Silently spawn cleanmgr without opening operation dialog
                    systemToolsUseCase.openDiskCleanup()
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

    private fun startTelemetryPolling() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                refreshSystemStats()
                delay(2000)
            }
        }
    }

    private fun loadData(forceRefresh: Boolean) {
        if (forceRefresh) {
            knownUpgradeMap.clear()
        }

        _uiState.update {
            it.copy(
                isRefreshing = true,
                isCheckingUpdates = true
            )
        }

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                coroutineScope {
                    // 1. Fetch raw installed packages flow
                    val installedDeferred = async {
                        getPackagesUseCase.execute(
                            showUpgradesOnly = false,
                            forceRefresh = forceRefresh
                        ).firstOrNull() ?: emptyList()
                    }

                    // 2. Fetch authoritative WinGet upgrades
                    val wingetUpgradesDeferred = async {
                        getPackagesUseCase.execute(
                            showUpgradesOnly = true,
                            forceRefresh = forceRefresh
                        ).firstOrNull() ?: emptyList()
                    }

                    val rawInstalled = installedDeferred.await()
                    val wingetUpgrades = wingetUpgradesDeferred.await()

                    // Populate initial upgrade map from WinGet
                    wingetUpgrades.forEach { upg ->
                        if (!upg.availableVersion.isNullOrBlank()) {
                            knownUpgradeMap[upg.id] = upg.availableVersion
                        }
                    }

                    // 3. Resolve local/non-winget applications in background concurrently
                    val localApps = rawInstalled.filter { it.isLocal && !knownUpgradeMap.containsKey(it.id) }
                    val resolvedLocalApps = if (localApps.isNotEmpty()) {
                        packageRepository.resolveUpdatesForLocalPackages(localApps)
                    } else emptyList()

                    resolvedLocalApps.filter { it.hasUpdate }.forEach { pkg ->
                        if (!pkg.availableVersion.isNullOrBlank()) {
                            knownUpgradeMap[pkg.id] = pkg.availableVersion
                        }
                    }

                    // 4. Merge all packages and upgrades into a single authoritative set
                    val annotatedAll = rawInstalled.map { pkg ->
                        val updateVer = knownUpgradeMap[pkg.id]
                        if (!updateVer.isNullOrBlank()) {
                            val matchedId = resolvedLocalApps.firstOrNull { it.id == pkg.id }?.matchedWingetId ?: pkg.matchedWingetId
                            pkg.copy(availableVersion = updateVer, matchedWingetId = matchedId)
                        } else {
                            pkg
                        }
                    }

                    val deduplicatedAll = PackageDeduplicator.deduplicate(annotatedAll)
                    val deduplicatedUpgrades = PackageDeduplicator.deduplicate(
                        deduplicatedAll.filter { it.hasUpdate }.ifEmpty { wingetUpgrades }
                    )

                    // 5. Atomically update UI state only after all updates are 100% loaded
                    _uiState.update { state ->
                        state.copy(
                            allPackages = deduplicatedAll,
                            upgradablePackages = deduplicatedUpgrades,
                            isRefreshing = false,
                            isCheckingUpdates = false
                        )
                    }

                    updateDisplayedPackages()
                    refreshSystemStats()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isCheckingUpdates = false,
                        operationResult = OperationResult.Error("Failed to load packages: ${e.message}")
                    )
                }
            }
        }
    }

    private fun updateDisplayedPackages() {
        _uiState.update { state ->
            val sourceList = when (state.activeTab) {
                NavigationTab.ALL_PACKAGES -> state.allPackages
                NavigationTab.UPGRADES_AVAILABLE -> state.upgradablePackages
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
