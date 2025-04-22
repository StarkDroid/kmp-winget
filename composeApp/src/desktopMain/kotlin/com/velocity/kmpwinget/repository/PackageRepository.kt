package com.velocity.kmpwinget.repository

import com.velocity.kmpwinget.model.domain.OperationResult
import com.velocity.kmpwinget.model.domain.Package
import com.velocity.kmpwinget.model.domain.cleanVersions
import com.velocity.kmpwinget.utils.PowerShellCommand
import com.velocity.kmpwinget.utils.executeCleanupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Interface for package management operations
 */
interface IPackageRepository {
    suspend fun listInstalledPackages(showUpgradesOnly: Boolean = false): Flow<List<Package>>
    suspend fun upgradePackage(packageId: String): Flow<OperationResult>
    suspend fun uninstallPackage(packageId: String): Flow<OperationResult>
    suspend fun getPackageDetails(packageId: String): Flow<Package>
    suspend fun cleanDisk(): Boolean
}

class PackageRepositoryImpl : IPackageRepository {
    override suspend fun listInstalledPackages(showUpgradesOnly: Boolean): Flow<List<Package>> = flow {
        val packages = PowerShellCommand.listInstalledPackages(showUpgradesOnly)
        .map { it.cleanVersions() }
        emit(packages)
    }

    override suspend fun upgradePackage(packageId: String): Flow<OperationResult> = flow {
        val result = PowerShellCommand.upgradePackage(packageId)
        if (result) {
            emit(OperationResult.Success("Package $packageId upgraded successfully"))
        } else {
            emit(OperationResult.Error("Failed to upgrade package $packageId"))
        }
    }

    override suspend fun uninstallPackage(packageId: String): Flow<OperationResult> = flow {
        val result = PowerShellCommand.uninstallPackage(packageId)
        if (result) {
            emit(OperationResult.Success("Package $packageId uninstalled successfully"))
        } else {
            emit(OperationResult.Error("Failed to uninstall package $packageId"))
        }
    }

    override suspend fun getPackageDetails(packageId: String): Flow<Package> = flow {
        val allPackages = PowerShellCommand.listInstalledPackages(false)
        val packageDetails = allPackages.find { it.id == packageId }
        if (packageDetails != null) {
            emit(packageDetails)
        } else {
            throw Exception("Package not found")
        }
    }

    override suspend fun cleanDisk(): Boolean {
        return executeCleanupManager() is OperationResult.Success
    }
}