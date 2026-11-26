package com.velocity.kmpwinget.data.datasource

import com.velocity.kmpwinget.platform.WindowsNativeBridge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * A driver update published by the Windows Update Agent (WUA).
 *
 * This is the only supported driver update source in the application. WinGet is an
 * application package manager and does not maintain a driver catalog, so it must never
 * be used to infer driver updates.
 */
data class WindowsUpdateDriver(
    val title: String,
    val version: String?,
    val date: String?
)

object WindowsUpdateDriverScanner {

    private const val SEARCH_TIMEOUT_SECONDS = 120L

    private val searchScript = """
        ${'$'}ErrorActionPreference = 'Stop'
        ${'$'}ProgressPreference = 'SilentlyContinue'
        [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
        ${'$'}session = New-Object -ComObject Microsoft.Update.Session
        ${'$'}searcher = ${'$'}session.CreateUpdateSearcher()
        ${'$'}result = ${'$'}searcher.Search("IsInstalled=0 and Type='Driver'")
        foreach (${'$'}update in ${'$'}result.Updates) {
            try {
                ${'$'}title = (${'$'}update.Title -replace '\s+', ' ').Replace([char]9, ' ')
                ${'$'}version = ''
                ${'$'}date = ''
                try { ${'$'}version = [string]${'$'}update.DriverVerString } catch { }
                try { ${'$'}date = ${'$'}update.DriverVerDate.ToString('yyyy-MM-dd') } catch { }
                Write-Output (${'$'}title + "`t" + ${'$'}version + "`t" + ${'$'}date)
            } catch { }
        }
    """.trimIndent()

    suspend fun searchDriverUpdates(): List<WindowsUpdateDriver> = withContext(Dispatchers.IO) {
        if (!WindowsNativeBridge.isWindows) return@withContext emptyList()

        var process: Process? = null
        try {
            // -EncodedCommand avoids Windows command-line quote mangling for multi-line scripts.
            val encodedCommand = Base64.getEncoder()
                .encodeToString(searchScript.toByteArray(Charsets.UTF_16LE))

            process = ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-WindowStyle", "Hidden",
                "-EncodedCommand", encodedCommand
            ).redirectError(ProcessBuilder.Redirect.DISCARD).start()

            val output = StringBuilder()
            val readerThread = Thread {
                try {
                    process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            output.appendLine(line)
                        }
                    }
                } catch (_: Throwable) {
                }
            }.apply {
                isDaemon = true
                start()
            }

            val finished = runInterruptible {
                process.waitFor(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
            if (!finished) {
                process.destroyForcibly()
                return@withContext emptyList()
            }
            readerThread.join(5_000)

            if (process.exitValue() == 0) parseSearchOutput(output.toString()) else emptyList()
        } catch (e: CancellationException) {
            process?.destroyForcibly()
            throw e
        } catch (_: Throwable) {
            process?.destroyForcibly()
            emptyList()
        }
    }

    fun parseSearchOutput(output: String): List<WindowsUpdateDriver> {
        return output.lines()
            .mapNotNull { line ->
                val parts = line.split('\t')
                if (parts.size < 2) return@mapNotNull null
                val title = parts[0].trim()
                if (title.isBlank()) return@mapNotNull null
                WindowsUpdateDriver(
                    title = title,
                    version = parts.getOrNull(1)?.trim()?.ifBlank { extractVersion(title) },
                    date = parts.getOrNull(2)?.trim()?.ifBlank { null }
                )
            }
            .distinctBy { it.title }
    }

    private fun extractVersion(title: String): String? {
        return Regex("\\d+(?:\\.\\d+){1,3}")
            .findAll(title)
            .lastOrNull()
            ?.value
    }
}
