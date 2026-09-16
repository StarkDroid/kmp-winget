package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.DriverClass
import com.velocity.kmpwinget.domain.model.DriverPackage
import com.velocity.kmpwinget.domain.repository.IDriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDriversUseCase(
    private val driverRepository: IDriverRepository
) {
    fun execute(
        searchQuery: String = "",
        driverClassFilter: DriverClass? = null,
        forceRefresh: Boolean = false
    ): Flow<List<DriverPackage>> {
        return driverRepository.getInstalledDrivers(forceRefresh).map { drivers ->
            drivers.filter { driver ->
                val matchesQuery = if (searchQuery.isBlank()) true else {
                    val q = searchQuery.trim().lowercase()
                    driver.displayName.lowercase().contains(q) ||
                            driver.providerName.lowercase().contains(q) ||
                            driver.originalName.lowercase().contains(q) ||
                            driver.publishedName.lowercase().contains(q)
                }

                val matchesClass = if (driverClassFilter == null) true else driver.driverClass == driverClassFilter

                matchesQuery && matchesClass
            }.sortedWith(
                compareByDescending<DriverPackage> { it.hasUpdate }
                    .thenBy { it.driverClass.ordinal }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }
}
