package com.velocity.kmpwinget.data.datasource

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.platform.WindowsNativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WindowsRegistryScanner {

    private data class UninstallRoot(
        val hKey: WinReg.HKEY,
        val path: String,
        val idPrefix: String
    )

    private val UNINSTALL_ROOTS = listOf(
        UninstallRoot(
            WinReg.HKEY_LOCAL_MACHINE,
            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
            "ARP\\Machine\\X64"
        ),
        UninstallRoot(
            WinReg.HKEY_LOCAL_MACHINE,
            "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
            "ARP\\Machine\\X86"
        ),
        UninstallRoot(
            WinReg.HKEY_CURRENT_USER,
            "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
            "ARP\\User"
        )
    )

    suspend fun scanInstalledApps(): List<Package> = withContext(Dispatchers.IO) {
        if (!WindowsNativeBridge.isWindows) return@withContext emptyList()

        val results = mutableMapOf<String, Package>()

        for (root in UNINSTALL_ROOTS) {
            val hKey = root.hKey
            val path = root.path
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

                        val displayIcon = if (Advapi32Util.registryValueExists(hKey, subPath, "DisplayIcon")) {
                            Advapi32Util.registryGetStringValue(hKey, subPath, "DisplayIcon").trim()
                        } else null

                        val sizeKb = if (Advapi32Util.registryValueExists(hKey, subPath, "EstimatedSize")) {
                            try {
                                val size = Advapi32Util.registryGetIntValue(hKey, subPath, "EstimatedSize")
                                if (size > 1024) "${size / 1024} MB" else "$size KB"
                            } catch (_: Throwable) {
                                null
                            }
                        } else null

                        val id = "${root.idPrefix}\\$subKey"
                        val nameKey = name.lowercase()

                        // Deduplicate by normalized display name, preferring an entry with a version.
                        if (!results.containsKey(nameKey) || (results[nameKey]?.version.isNullOrBlank() && version.isNotBlank())) {
                            results[nameKey] = Package(
                                id = id,
                                name = name,
                                version = version,
                                publisher = publisher,
                                installDate = installDate,
                                estimatedSize = sizeKb,
                                iconPath = displayIcon,
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
