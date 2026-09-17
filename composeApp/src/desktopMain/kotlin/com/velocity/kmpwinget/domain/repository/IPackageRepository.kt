package com.velocity.kmpwinget.domain.repository

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDetails
import kotlinx.coroutines.flow.Flow

interface IPackageRepository {
    fun getInstalledPackages(forceRefresh: Boolean = false): Flow<List<Package>>
    fun getUpgradablePackages(forceRefresh: Boolean = false): Flow<List<Package>>
    suspend fun resolveUpdatesForLocalPackages(packages: List<Package>): List<Package>
    suspend fun upgradePackage(packageId: String, packageName: String, matchedWingetId: String? = null): Flow<OperationResult>
    suspend fun uninstallPackage(packageId: String, packageName: String): Flow<OperationResult>
    suspend fun upgradeMultiplePackages(packages: List<Package>): Flow<OperationResult>
    suspend fun uninstallMultiplePackages(packages: List<Package>): Flow<OperationResult>
    suspend fun searchWingetStore(query: String): List<Package>
    suspend fun getPackageDetails(pkg: Package): PackageDetails
}
