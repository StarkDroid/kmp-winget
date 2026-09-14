package com.velocity.kmpwinget.data.datasource

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WindowsRegistryScanner {

    private val UNINSTALL_KEYS = listOf(
        Pair(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"),
        Pair(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"),
        Pair(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall")
    )

    suspend fun scanInstalledApps(): List<Package> = withContext(Dispatchers.IO) {
        if (!WindowsNativeBridge.isWindows) return@withContext emptyList()

        val results = mutableMapOf<String, Package>()

        for ((hKey, path) in UNINSTALL_KEYS) {
            try {
                if (!Advapi32Util.registryKeyExists(hKey, path)) continue

                val subKeys = Advapi32Util.registryGetKeys(hKey, path)
                for (subKey in subKeys) {
                    try {
                        val subPath = "$path\\$subKey"

                        // Skip system components or Windows updates if flag is set
                        if (Advapi32Util.registryValueExists(hKey, subPath, "SystemComponent")) {
                            val sysComp = Advapi32Util.registryGetIntValue(hKey, subPath, "SystemComponent")
                            if (sysComp == 1) continue
                        }

                        if (Advapi32Util.registryValueExists(hKey, subPath, "ParentKeyName")) {
                            continue
                        }

                        val name = if (Advapi32Util.registryValueExists(hKey, subPath, "DisplayName")) {
                            Advapi32Util.registryGetStringValue(hKey, subPath, "DisplayName").trim()
                        } else ""

                        if (name.isBlank()) continue

                        val version = if (Advapi32Util.registryValueExists(hKey, subPath, "DisplayVersion")) {
                            Advapi32Util.registryGetStringValue(hKey, subPath, "DisplayVersion").trim()
                        } else ""

                        val publisher = if (Advapi32Util.registryValueExists(hKey, subPath, "Publisher")) {
                            Advapi32Util.registryGetStringValue(hKey, subPath, "Publisher").trim()
                        } else null

                        val installDate = if (Advapi32Util.registryValueExists(hKey, subPath, "InstallDate")) {
                            Advapi32Util.registryGetStringValue(hKey, subPath, "InstallDate").trim()
                        } else null

                        val sizeKb = if (Advapi32Util.registryValueExists(hKey, subPath, "EstimatedSize")) {
                            try {
                                val size = Advapi32Util.registryGetIntValue(hKey, subPath, "EstimatedSize")
                                if (size > 1024) "${size / 1024} MB" else "$size KB"
                            } catch (_: Throwable) {
                                null
                            }
                        } else null

                        val id = "ARP\\Machine\\X64\\$subKey"

                        // Deduplicate by name
                        if (!results.containsKey(name) || (results[name]?.version.isNullOrBlank() && version.isNotBlank())) {
                            results[name] = Package(
                                id = id,
                                name = name,
                                version = version,
                                publisher = publisher,
                                installDate = installDate,
                                estimatedSize = sizeKb,
                                source = PackageSource.LOCAL
                            )
                        }
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {}
        }

        results.values.toList().sortedBy { it.name.lowercase() }
    }
}
