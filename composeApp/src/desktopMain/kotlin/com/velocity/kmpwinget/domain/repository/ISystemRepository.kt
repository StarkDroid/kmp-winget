package com.velocity.kmpwinget.domain.repository

import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.model.SystemStats

interface ISystemRepository {
    suspend fun getWingetVersion(): String
    suspend fun isWingetInstalled(): Boolean
    suspend fun getSystemStats(packageCount: Int, updatesCount: Int): SystemStats
    suspend fun openDiskCleanup(): OperationResult
    suspend fun optimizeSystem(): OperationResult
}
