package com.velocity.kmpwinget.domain.repository

import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.OperationResult
import kotlinx.coroutines.flow.Flow

interface IDriverRepository {
    fun getInstalledDrivers(forceRefresh: Boolean = false): Flow<List<DriverPackage>>
    suspend fun resolveDriverUpdates(drivers: List<DriverPackage>): List<DriverPackage>
    suspend fun updateDriver(driver: DriverPackage): Flow<OperationResult>
    suspend fun scanPnpDevices(): OperationResult
}
