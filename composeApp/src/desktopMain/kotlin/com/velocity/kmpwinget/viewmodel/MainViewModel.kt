package com.velocity.kmpwinget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.velocity.kmpwinget.model.domain.OperationResult
import com.velocity.kmpwinget.model.domain.Package
import com.velocity.kmpwinget.repository.IPackageRepository
import com.velocity.kmpwinget.utils.PowerShellCommand
import kotlinx.coroutines.delay

class MainViewModel(private val packageRepository: IPackageRepository) : ViewModel() {
    private val _packages = MutableStateFlow<List<Package>>(emptyList())
    val packages: StateFlow<List<Package>> = _packages.asStateFlow()

    private val _isLoading = MutableStateFlow<OperationResult?>(null)
    val isLoading: StateFlow<OperationResult?> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showUpgradesOnly = MutableStateFlow(false)
    val showUpgradesOnly: StateFlow<Boolean> = _showUpgradesOnly.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedPackageIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackageIds: StateFlow<Set<String>> = _selectedPackageIds.asStateFlow()


    init {
        loadPackages()
    }

    fun loadPackages() {
        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            try {
                packageRepository.listInstalledPackages(_showUpgradesOnly.value).collect { packages ->
                    val selectedIds = _selectedPackageIds.value
                    _packages.value = packages.map { pkg ->
                        pkg.copy(isSelected = selectedIds.contains(pkg.id))
                    }
                    _isLoading.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = OperationResult.Error("Failed to load packages: ${e.message}")
            }
        }
    }

    fun searchPackages(query: String) {
        _searchQuery.value = query
    }

    fun toggleUpgradesOnly(showOnly: Boolean) {
        _showUpgradesOnly.value = showOnly
        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            try {
                packageRepository.listInstalledPackages().collect { allPackages ->
                    val selectedIds = _selectedPackageIds.value
                    val filteredPackages = if (showOnly) {
                        allPackages.filter { it.hasUpdate }
                    } else {
                        allPackages
                    }
                    _packages.value = filteredPackages.map { pkg ->
                        pkg.copy(isSelected = selectedIds.contains(pkg.id))
                    }
                    _isLoading.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = OperationResult.Error("Failed to filter packages: ${e.message}")
            }
        }
    }

    fun upgradePackage(packageId: String) {
        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            packageRepository.upgradePackage(packageId).collect { result ->
                _isLoading.value = result
                if (result is OperationResult.Success) {
                    delay(1500)
                    loadPackages()
                }
            }
        }
    }

    fun uninstallPackage(packageId: String) {
        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            packageRepository.uninstallPackage(packageId).collect { result ->
                _isLoading.value = result
                if (result is OperationResult.Success) {
                    delay(1500)
                    loadPackages()
                }
            }
        }
    }

    fun cleanDisk() {
        viewModelScope.launch {
            PowerShellCommand.executeCommand("cleanmgr")
        }
    }

    fun clearOperationResult() {
        _isLoading.value = null
    }

    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            clearSelections()
        }
    }

    fun togglePackageSelection(packageId: String) {
        val currentSelection = _selectedPackageIds.value.toMutableSet()
        if (currentSelection.contains(packageId)) {
            currentSelection.remove(packageId)
        } else {
            currentSelection.add(packageId)
        }
        _selectedPackageIds.value = currentSelection

        _packages.value = _packages.value.map { pkg ->
            if (pkg.id == packageId) {
                pkg.copy(isSelected = currentSelection.contains(pkg.id))
            } else {
                pkg
            }
        }
    }

    fun clearSelections() {
        _selectedPackageIds.value = emptySet()
        _packages.value = _packages.value.map { pkg -> pkg.copy(isSelected = false) }
    }

    fun isPackageSelected(packageId: String): Boolean {
        return _selectedPackageIds.value.contains(packageId)
    }

    fun upgradeSelectedPackages() {
        val packagesToUpgrade = _selectedPackageIds.value
        if (packagesToUpgrade.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            var successCount = 0
            var errorCount = 0

            for (id in packagesToUpgrade) {
                try {
                    packageRepository.upgradePackage(id).collect { result ->
                        if (result is OperationResult.Success) {
                            successCount++
                        } else if (result is OperationResult.Error) {
                            errorCount++
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                }
            }

            val resultMessage = if (errorCount == 0) {
                OperationResult.Success("Successfully upgraded $successCount packages")
            } else {
                OperationResult.Error("Upgraded $successCount packages, failed to upgrade $errorCount packages")
            }

            _isLoading.value = resultMessage
            delay(1500)
            clearSelections()
            loadPackages()
            toggleMultiSelectMode()
        }
    }

    fun uninstallSelectedPackages() {
        val packagesToUninstall = _selectedPackageIds.value
        if (packagesToUninstall.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            var successCount = 0
            var errorCount = 0

            for (id in packagesToUninstall) {
                try {
                    packageRepository.uninstallPackage(id).collect { result ->
                        if (result is OperationResult.Success) {
                            successCount++
                        } else if (result is OperationResult.Error) {
                            errorCount++
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                }
            }

            val resultMessage = if (errorCount == 0) {
                OperationResult.Success("Successfully uninstalled $successCount packages")
            } else {
                OperationResult.Error("Uninstalled $successCount packages, failed to uninstall $errorCount packages")
            }

            _isLoading.value = resultMessage
            delay(1500)
            clearSelections()
            loadPackages()
            toggleMultiSelectMode()
        }
    }

}