package com.velocity.kmpwinget.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.velocity.kmpwinget.model.domain.OperationResult
import com.velocity.kmpwinget.model.domain.Package
import com.velocity.kmpwinget.utils.executeCleanupManager

/**
 * Interface for package management operations
 */
interface IPackageRepository {
    suspend fun listInstalledPackages(): Flow<List<Package>>
    suspend fun searchPackages(query: String): Flow<List<Package>>
    suspend fun upgradePackage(packageId: String): Flow<OperationResult>
    suspend fun uninstallPackage(packageId: String): Flow<OperationResult>
    suspend fun getPackageDetails(packageId: String): Flow<Package>
    suspend fun cleanDisk(): Boolean
}

class PackageRepositoryImpl : IPackageRepository {
    override suspend fun listInstalledPackages(): Flow<List<Package>> = flow {
        emit(com.velocity.kmpwinget.utils.listInstalledPackages(false))
    }

    override suspend fun searchPackages(query: String): Flow<List<Package>> = flow {
        emit(com.velocity.kmpwinget.utils.listInstalledPackages(false).filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true)
        })
    }

    override suspend fun upgradePackage(packageId: String): Flow<OperationResult> = flow {
        val result = com.velocity.kmpwinget.utils.upgradePackage(packageId)
        if (result) {
            emit(OperationResult.Success("Package $packageId upgraded successfully"))
        } else {
            emit(OperationResult.Error("Failed to upgrade package $packageId"))
        }
    }

    override suspend fun uninstallPackage(packageId: String): Flow<OperationResult> = flow {
        val result = com.velocity.kmpwinget.utils.uninstallPackage(packageId)
        if (result) {
            emit(OperationResult.Success("Package $packageId uninstalled successfully"))
        } else {
            emit(OperationResult.Error("Failed to uninstall package $packageId"))
        }
    }

    override suspend fun getPackageDetails(packageId: String): Flow<Package> = flow {
        val allPackages = com.velocity.kmpwinget.utils.listInstalledPackages(false)
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