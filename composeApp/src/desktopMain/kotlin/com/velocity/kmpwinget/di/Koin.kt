package com.velocity.kmpwinget.di

import com.velocity.kmpwinget.data.repository.DriverRepositoryImpl
import com.velocity.kmpwinget.data.repository.PackageRepositoryImpl
import com.velocity.kmpwinget.data.repository.SystemRepositoryImpl
import com.velocity.kmpwinget.domain.repository.IDriverRepository
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import com.velocity.kmpwinget.domain.repository.ISystemRepository
import com.velocity.kmpwinget.domain.usecase.BatchOperationUseCase
import com.velocity.kmpwinget.domain.usecase.GetDriversUseCase
import com.velocity.kmpwinget.domain.usecase.GetPackagesUseCase
import com.velocity.kmpwinget.domain.usecase.SystemToolsUseCase
import com.velocity.kmpwinget.domain.usecase.UninstallPackageUseCase
import com.velocity.kmpwinget.domain.usecase.UpdateDriverUseCase
import com.velocity.kmpwinget.domain.usecase.UpgradePackageUseCase
import com.velocity.kmpwinget.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<IPackageRepository> { PackageRepositoryImpl() }
    single<ISystemRepository> { SystemRepositoryImpl() }
    single<IDriverRepository> { DriverRepositoryImpl() }

    // UseCases
    single { GetPackagesUseCase(get()) }
    single { UpgradePackageUseCase(get()) }
    single { UninstallPackageUseCase(get()) }
    single { BatchOperationUseCase(get()) }
    single { SystemToolsUseCase(get()) }
    single { GetDriversUseCase(get()) }
    single { UpdateDriverUseCase(get()) }

    // ViewModel
    viewModel {
        MainViewModel(
            getPackagesUseCase = get(),
            upgradePackageUseCase = get(),
            uninstallPackageUseCase = get(),
            batchOperationUseCase = get(),
            systemToolsUseCase = get(),
            packageRepository = get(),
            getDriversUseCase = get(),
            updateDriverUseCase = get()
        )
    }
}
