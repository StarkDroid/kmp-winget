package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import kotlinx.coroutines.flow.Flow

class BatchOperationUseCase(
    private val packageRepository: IPackageRepository
) {
    suspend fun upgradeMultiple(packages: List<Package>): Flow<OperationResult> {
        return packageRepository.upgradeMultiplePackages(packages)
    }

    suspend fun uninstallMultiple(packages: List<Package>): Flow<OperationResult> {
        return packageRepository.uninstallMultiplePackages(packages)
    }
}
