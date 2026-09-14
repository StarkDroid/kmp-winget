package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.SystemStats
import com.velocity.kmpwinget.domain.repository.ISystemRepository

class SystemToolsUseCase(
    private val systemRepository: ISystemRepository
) {
    suspend fun getSystemStats(totalPackages: Int, updatesCount: Int): SystemStats {
        return systemRepository.getSystemStats(totalPackages, updatesCount)
    }

    suspend fun openDiskCleanup(): OperationResult {
        return systemRepository.openDiskCleanup()
    }

    suspend fun optimizeSystem(): OperationResult {
        return systemRepository.optimizeSystem()
    }
}
