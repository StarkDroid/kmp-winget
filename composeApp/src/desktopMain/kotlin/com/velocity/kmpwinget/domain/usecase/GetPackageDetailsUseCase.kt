package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDetails
import com.velocity.kmpwinget.domain.repository.IPackageRepository

class GetPackageDetailsUseCase(
    private val packageRepository: IPackageRepository
) {
    suspend fun execute(pkg: Package): PackageDetails {
        return packageRepository.getPackageDetails(pkg)
    }
}
