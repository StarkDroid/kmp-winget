package com.velocity.kmpwinget.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.velocity.kmpwinget.repository.IPackageRepository
import com.velocity.kmpwinget.repository.PackageRepositoryImpl
import com.velocity.kmpwinget.viewmodel.MainViewModel

val appModule = module {
    
    // Repositories
    single<IPackageRepository> { PackageRepositoryImpl() }

    // ViewModels
    viewModel {
        MainViewModel(get())
    }
}