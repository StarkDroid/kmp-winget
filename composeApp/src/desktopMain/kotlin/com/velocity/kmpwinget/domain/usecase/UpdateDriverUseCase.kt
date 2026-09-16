package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.domain.repository.IDriverRepository
import kotlinx.coroutines.flow.Flow

class UpdateDriverUseCase(
    private val driverRepository: IDriverRepository
) {
    suspend fun execute(driver: DriverPackage): Flow<OperationResult> {
        return driverRepository.updateDriver(driver)
    }

    suspend fun scanPnpDevices(): OperationResult {
        return driverRepository.scanPnpDevices()
    }
}
