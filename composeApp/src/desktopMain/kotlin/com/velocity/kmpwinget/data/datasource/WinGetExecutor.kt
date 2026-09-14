package com.velocity.kmpwinget.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

data class CommandExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val isSuccess: Boolean
)

object WinGetExecutor {

    private val wingetExecutable: String by lazy {
        findWinGetPath()
    }

    private fun findWinGetPath(): String {
        // 1. Test standard command in PATH
        try {
            val process = ProcessBuilder("winget", "--version")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            if (process.exitValue() == 0 && output.trim().isNotEmpty()) {
                return "winget"
            }
        } catch (_: Throwable) {}

        // 2. Check LocalAppData WindowsApps
        val localAppData = System.getenv("LOCALAPPDATA")
        if (localAppData != null) {
            val candidate = File(localAppData, "Microsoft\\WindowsApps\\winget.exe")
            if (candidate.exists() && candidate.canExecute()) {
                return candidate.absolutePath
            }
        }

        // 3. Fallback to standard winget
        return "winget"
    }

    /**
     * Executes a winget command and returns the full output.
     */
    suspend fun execute(vararg args: String): CommandExecutionResult = withContext(Dispatchers.IO) {
        val commandList = mutableListOf(wingetExecutable).apply {
            addAll(args)
        }

        try {
            val processBuilder = ProcessBuilder(commandList)
            processBuilder.environment()["PYTHONIOENCODING"] = "utf-8"
            val process = processBuilder.start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream, StandardCharsets.UTF_8))

            val stdoutThread = Thread {
                try {
                    var line: String?
                    while (stdoutReader.readLine().also { line = it } != null) {
                        stdoutBuilder.appendLine(line)
                    }
                } catch (_: Throwable) {}
            }

            val stderrThread = Thread {
                try {
                    var line: String?
                    while (stderrReader.readLine().also { line = it } != null) {
                        stderrBuilder.appendLine(line)
                    }
                } catch (_: Throwable) {}
            }

            stdoutThread.start()
            stderrThread.start()

            val exitCode = process.waitFor()
            stdoutThread.join(5000)
            stderrThread.join(5000)

            val stdout = stdoutBuilder.toString().trim()
            val stderr = stderrBuilder.toString().trim()

            CommandExecutionResult(
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr,
                isSuccess = exitCode == 0 || (exitCode == -1978335189) // WinGet "No applicable update found" code
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Failed to execute winget",
                isSuccess = false
            )
        }
    }

    /**
     * Streams command output line-by-line for live terminal progress feedback.
     */
    fun streamExecution(vararg args: String): Flow<String> = flow {
        val commandList = mutableListOf(wingetExecutable).apply {
            addAll(args)
        }

        val process = ProcessBuilder(commandList)
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line != null && line!!.isNotBlank()) {
                    emit(line!!)
                }
            }
        } finally {
            reader.close()
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)
}
