package com.velocity.kmpwinget.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Opens Windows Update, the supported installation surface for driver updates.
 *
 * The application deliberately does not attempt to install driver packages silently:
 * driver installation requires elevation and an OEM/Windows Update compatibility check.
 */
object WindowsUpdateLauncher {

    suspend fun openDriverUpdates(): Boolean = withContext(Dispatchers.IO) {
        if (!WindowsNativeBridge.isWindows) return@withContext false

        val scanTriggered = runCatching {
            ProcessBuilder("UsoClient.exe", "StartScan")
                .redirectErrorStream(true)
                .start()
        }.isSuccess

        val settingsOpened = runCatching {
            ProcessBuilder("explorer.exe", "ms-settings:windowsupdate")
                .start()
        }.isSuccess

        scanTriggered || settingsOpened
    }
}
