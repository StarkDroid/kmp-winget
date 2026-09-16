package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.domain.model.DriverPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object WindowsDriverScanner {

    suspend fun scanDrivers(): List<DriverPackage> = withContext(Dispatchers.IO) {
        if (!WindowsNativeBridge.isWindows) return@withContext emptyList()

        try {
            val process = ProcessBuilder("pnputil.exe", "/enum-drivers")
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
            val lines = reader.readLines()
            process.waitFor()

            parsePnpUtilOutput(lines)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun parsePnpUtilOutput(lines: List<String>): List<DriverPackage> {
        val drivers = mutableListOf<DriverPackage>()
        var currentPublishedName = ""
        var currentOriginalName = ""
        var currentProviderName = ""
        var currentClassName = ""
        var currentDriverDate = ""
        var currentDriverVersion = ""
        var currentSignerName: String? = null

        fun flushBlock() {
            if (currentPublishedName.isNotEmpty() && currentOriginalName.isNotEmpty()) {
                drivers.add(
                    DriverPackage(
                        publishedName = currentPublishedName,
                        originalName = currentOriginalName,
                        providerName = currentProviderName.ifBlank { "System" },
                        className = currentClassName.ifBlank { "Component" },
                        driverDate = currentDriverDate,
                        driverVersion = currentDriverVersion,
                        signerName = currentSignerName
                    )
                )
            }
            currentPublishedName = ""
            currentOriginalName = ""
            currentProviderName = ""
            currentClassName = ""
            currentDriverDate = ""
            currentDriverVersion = ""
            currentSignerName = null
        }

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("---") || line.startsWith("Microsoft PnP Utility")) {
                if (currentPublishedName.isNotEmpty()) {
                    flushBlock()
                }
                continue
            }

            val key = line.substringBefore(":").trim().lowercase()
            val value = line.substringAfter(":").trim()

            when {
                key.contains("published name") -> currentPublishedName = value
                key.contains("original name") -> currentOriginalName = value
                key.contains("provider name") -> currentProviderName = value
                key.contains("class name") -> currentClassName = value
                key.contains("driver version") -> {
                    // e.g. "08/20/2024 2.2.0.134"
                    if (value.contains(" ")) {
                        currentDriverDate = value.substringBefore(" ").trim()
                        currentDriverVersion = value.substringAfter(" ").trim()
                    } else {
                        currentDriverVersion = value
                    }
                }
                key.contains("signer name") -> currentSignerName = value
            }
        }

        if (currentPublishedName.isNotEmpty()) {
            flushBlock()
        }

        return drivers
    }
}
