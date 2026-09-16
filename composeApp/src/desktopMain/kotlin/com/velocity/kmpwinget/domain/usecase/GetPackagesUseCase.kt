package com.velocity.kmpwinget.domain.usecase

import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDeduplicator
import com.velocity.kmpwinget.domain.model.PackageSortOption
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.domain.model.SourceFilterOption
import com.velocity.kmpwinget.domain.repository.IPackageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPackagesUseCase(
    private val packageRepository: IPackageRepository
) {
    fun execute(
        showUpgradesOnly: Boolean = false,
        forceRefresh: Boolean = false,
        searchQuery: String = "",
        sortOption: PackageSortOption = PackageSortOption.NAME_ASC,
        sourceFilter: SourceFilterOption = SourceFilterOption.ALL
    ): Flow<List<Package>> {
        val baseFlow = if (showUpgradesOnly) {
            packageRepository.getUpgradablePackages(forceRefresh)
        } else {
            packageRepository.getInstalledPackages(forceRefresh)
        }

        return baseFlow.map { packages ->
            val deduplicated = PackageDeduplicator.deduplicate(packages)
            filterAndSort(deduplicated, searchQuery, sortOption, sourceFilter, showUpgradesOnly)
        }
    }

    private fun filterAndSort(
        packages: List<Package>,
        query: String,
        sortOption: PackageSortOption,
        sourceFilter: SourceFilterOption,
        showUpgradesOnly: Boolean
    ): List<Package> {
        val filtered = packages.filter { pkg ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                pkg.name.lowercase().contains(q) ||
                        pkg.id.lowercase().contains(q) ||
                        (pkg.publisher?.lowercase()?.contains(q) == true)
            }

            val matchesUpgrades = if (showUpgradesOnly) pkg.hasUpdate else true

            val matchesSource = when (sourceFilter) {
                SourceFilterOption.ALL -> true
                SourceFilterOption.WINGET_ONLY -> pkg.source == PackageSource.WINGET
                SourceFilterOption.MSSTORE_ONLY -> pkg.source == PackageSource.MSSTORE
                SourceFilterOption.LOCAL_ONLY -> pkg.source == PackageSource.LOCAL
            }

            matchesQuery && matchesUpgrades && matchesSource
        }

        return when (sortOption) {
            PackageSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            PackageSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            PackageSortOption.UPDATES_FIRST -> filtered.sortedWith(
                compareByDescending<Package> { it.hasUpdate }
                    .thenBy { it.name.lowercase() }
            )
            PackageSortOption.SOURCE -> filtered.sortedWith(
                compareBy<Package> { it.source.name }
                    .thenBy { it.name.lowercase() }
            )
        }
    }
}
