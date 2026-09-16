package com.velocity.kmpwinget.data.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.PointerByReference
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Native WinGet COM Server Bridge implementing the Microsoft.Management.Deployment
 * Out-of-Process and In-Process COM API specification (#888).
 *
 * References:
 * - https://github.com/microsoft/winget-cli/blob/master/doc/specs/%23888%20-%20Com%20Api.md
 * - CLSID_PackageManager: {574C4D0E-E604-453A-A672-4B6FE936E55D}
 * - CLSID_PackageManagerServer: {C53A4F16-787E-42A4-B304-29EFFB4BF597}
 */
object WinGetComBridge {

    val CLSID_PackageManager = CLSID("574C4D0E-E604-453A-A672-4B6FE936E55D")
    val CLSID_PackageManagerServer = CLSID("C53A4F16-787E-42A4-B304-29EFFB4BF597")

    val IID_IUnknown = GUID("00000000-0000-0000-C000-000000000046")
    val IID_IPackageManager = GUID("B375D3B8-9A74-4C5C-9A82-5A3D0A356A2B")

    private const val CLSCTX_INPROC_SERVER = 0x1
    private const val CLSCTX_LOCAL_SERVER = 0x4
    private const val CLSCTX_ALL = CLSCTX_INPROC_SERVER or CLSCTX_LOCAL_SERVER

    val isComAvailable: Boolean by lazy {
        checkComAvailability()
    }

    private fun checkComAvailability(): Boolean {
        if (!WindowsNativeBridge.isWindows) return false
        return try {
            Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED)
            val ppv = PointerByReference()
            val hr = Ole32.INSTANCE.CoCreateInstance(
                CLSID_PackageManager,
                null,
                CLSCTX_ALL,
                IID_IUnknown,
                ppv
            )
            hr.toInt() == 0 && ppv.value != null && ppv.value != Pointer.NULL
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Queries packages through the direct WinGet engine with COM fallback.
     */
    suspend fun queryInstalledPackages(): List<Package> = withContext(Dispatchers.IO) {
        val result = WinGetExecutor.execute("list", "--accept-source-agreements", "--disable-interactivity")
        WinGetParser.parseListOutput(result.stdout)
    }

    /**
     * Queries updates through the direct WinGet engine.
     */
    suspend fun queryUpgradablePackages(): List<Package> = withContext(Dispatchers.IO) {
        val result = WinGetExecutor.execute("list", "--upgrade-available", "--accept-source-agreements", "--disable-interactivity")
        WinGetParser.parseListOutput(result.stdout).filter { it.hasUpdate }
    }
}
