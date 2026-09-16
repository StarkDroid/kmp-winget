package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.domain.model.Package
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AppLauncher {

    suspend fun launchPackage(pkg: Package): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. If we have a direct executable icon/install path
            if (!pkg.iconPath.isNullOrBlank()) {
                val cleanPath = pkg.iconPath.trim().removeSurrounding("\"").substringBefore(",").trim()
                val file = File(cleanPath)
                if (file.exists() && (file.extension.equals("exe", ignoreCase = true) || file.extension.equals("lnk", ignoreCase = true))) {
                    ProcessBuilder(file.absolutePath).start()
                    return@withContext true
                }
            }

            // 2. Launch via Windows Start URI / explorer
            val cleanName = pkg.name.substringBefore(" (").trim()
            ProcessBuilder("cmd.exe", "/c", "start", "\"\"", "\"$cleanName\"").start()
            true
        } catch (_: Throwable) {
            false
        }
    }
}
