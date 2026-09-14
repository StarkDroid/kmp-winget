package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import kotlinx.coroutines.flow.Flow

class UpgradePackageUseCase(
    private val packageRepository: IPackageRepository
) {
    suspend fun execute(pkg: Package): Flow<OperationResult> {
        return packageRepository.upgradePackage(pkg.id, pkg.name, pkg.matchedWingetId)
    }

    suspend fun execute(packageId: String, packageName: String, matchedWingetId: String? = null): Flow<OperationResult> {
        return packageRepository.upgradePackage(packageId, packageName, matchedWingetId)
    }
}
