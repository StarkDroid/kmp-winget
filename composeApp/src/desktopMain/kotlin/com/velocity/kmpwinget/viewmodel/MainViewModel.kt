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

    init {
        loadPackages()
    }

    fun loadPackages() {
        viewModelScope.launch {
            _isLoading.value = OperationResult.Loading
            try {
                packageRepository.listInstalledPackages(_showUpgradesOnly.value).collect { packages ->
                    _packages.value = packages
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
                    _packages.value = if (showOnly) {
                        allPackages.filter { it.hasUpdate }
                    } else {
                        allPackages
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
}