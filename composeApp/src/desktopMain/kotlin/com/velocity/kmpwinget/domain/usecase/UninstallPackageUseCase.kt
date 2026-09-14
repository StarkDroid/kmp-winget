package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import kotlinx.coroutines.flow.Flow

class UninstallPackageUseCase(
    private val packageRepository: IPackageRepository
) {
    suspend fun execute(packageId: String, packageName: String): Flow<OperationResult> {
        return packageRepository.uninstallPackage(packageId, packageName)
    }
}
