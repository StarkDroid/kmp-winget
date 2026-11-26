package com.velocity.kmpwinget.data.datasource

import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.IPHlpAPI
import com.sun.jna.platform.win32.WinBase.FILETIME
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef.DWORDLONG
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.win32.StdCallLibrary
import com.velocity.kmpwinget.domain.model.LiveSystemTelemetry
import com.velocity.kmpwinget.platform.WindowsNativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.management.ManagementFactory
import java.net.NetworkInterface

object WindowsHardwareMonitor {

    private const val GPU_MEMORY_VALUE = "HardwareInformation.qwMemorySize"

    @Structure.FieldOrder(
        "dwLength",
        "dwMemoryLoad",
        "ullTotalPhys",
        "ullAvailPhys",
        "ullTotalPageFile",
        "ullAvailPageFile",
        "ullTotalVirtual",
        "ullAvailVirtual",
        "ullAvailExtendedVirtual"
    )
    open class MEMORYSTATUSEX : Structure() {
        @JvmField var dwLength: DWORD = DWORD(size().toLong())
        @JvmField var dwMemoryLoad: DWORD = DWORD(0)
        @JvmField var ullTotalPhys: DWORDLONG = DWORDLONG(0)
        @JvmField var ullAvailPhys: DWORDLONG = DWORDLONG(0)
        @JvmField var ullTotalPageFile: DWORDLONG = DWORDLONG(0)
        @JvmField var ullAvailPageFile: DWORDLONG = DWORDLONG(0)
        @JvmField var ullTotalVirtual: DWORDLONG = DWORDLONG(0)
        @JvmField var ullAvailVirtual: DWORDLONG = DWORDLONG(0)
        @JvmField var ullAvailExtendedVirtual: DWORDLONG = DWORDLONG(0)
    }

    private interface Kernel32Direct : StdCallLibrary {
        fun GlobalMemoryStatusEx(lpBuffer: MEMORYSTATUSEX): Boolean
        fun GetSystemTimes(lpIdleTime: FILETIME, lpKernelTime: FILETIME, lpUserTime: FILETIME): Boolean

        companion object {
            val INSTANCE: Kernel32Direct = Native.load("kernel32", Kernel32Direct::class.java)
        }
    }

    private var prevIdleTime: Long = 0L
    private var prevKernelTime: Long = 0L
    private var prevUserTime: Long = 0L

    private var previousNetworkTimestamp: Long = 0L
    private var previousReceivedBytes: Long = 0L
    private var previousTransmittedBytes: Long = 0L

    val cpuName: String by lazy {
        if (!WindowsNativeBridge.isWindows) return@lazy "Processor"
        try {
            Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
                "ProcessorNameString"
            ).trim()
        } catch (_: Throwable) {
            "CPU"
        }
    }

    val cpuBaseSpeedGhz: Double by lazy {
        if (!WindowsNativeBridge.isWindows) return@lazy 0.0
        try {
            val mhz = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
                "~MHz"
            )
            Math.round((mhz / 1000.0) * 10.0) / 10.0
        } catch (_: Throwable) {
            0.0
        }
    }

    val gpuName: String by lazy {
        if (!WindowsNativeBridge.isWindows) return@lazy "Dedicated GPU"
        try {
            val classKey = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}"
            if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, classKey)) {
                val subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, classKey)
                for (sub in subKeys) {
                    val subPath = "$classKey\\$sub"
                    if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, subPath, "DriverDesc")) {
                        val desc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, subPath, "DriverDesc").trim()
                        if (desc.isNotBlank() && !desc.contains("Basic", ignoreCase = true) && !desc.contains("Remote", ignoreCase = true)) {
                            return@lazy desc
                        }
                    }
                }
            }
            "Graphics Adapter"
        } catch (_: Throwable) {
            "GPU"
        }
    }

    val gpuVramGb: Double by lazy {
        if (!WindowsNativeBridge.isWindows) return@lazy 0.0
        try {
            val classKey = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}"
            for (sub in Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, classKey)) {
                val subPath = "$classKey\\$sub"
                if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, subPath, GPU_MEMORY_VALUE)) {
                    val bytes = Advapi32Util.registryGetLongValue(
                        WinReg.HKEY_LOCAL_MACHINE,
                        subPath,
                        GPU_MEMORY_VALUE
                    )
                    if (bytes > 0) {
                        return@lazy Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0
                    }
                }
            }
            0.0
        } catch (_: Throwable) {
            0.0
        }
    }

    /**
     * Reads real-time hardware telemetry (CPU %, RAM %, GPU, Network speeds, Uptime).
     */
    suspend fun getLiveTelemetry(): LiveSystemTelemetry = withContext(Dispatchers.IO) {
        var cpuPercent = 0f
        var totalRamGb = 16.0
        var freeRamGb = 8.0
        var usedRamGb = 8.0
        var ramPercent = 50f

        // 1. Native Win32 RAM via GlobalMemoryStatusEx
        try {
            val mem = MEMORYSTATUSEX()
            if (Kernel32Direct.INSTANCE.GlobalMemoryStatusEx(mem)) {
                val totalBytes = mem.ullTotalPhys.toLong()
                val freeBytes = mem.ullAvailPhys.toLong()
                ramPercent = mem.dwMemoryLoad.toInt().toFloat()

                totalRamGb = Math.round((totalBytes / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0
                freeRamGb = Math.round((freeBytes / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0
                usedRamGb = Math.round((totalRamGb - freeRamGb) * 10.0) / 10.0
            }
        } catch (_: Throwable) {}

        // 2. Native Win32 CPU % via GetSystemTimes
        try {
            val idleTime = FILETIME()
            val kernelTime = FILETIME()
            val userTime = FILETIME()

            if (Kernel32Direct.INSTANCE.GetSystemTimes(idleTime, kernelTime, userTime)) {
                val currentIdle = fileTimeToLong(idleTime)
                val currentKernel = fileTimeToLong(kernelTime)
                val currentUser = fileTimeToLong(userTime)

                if (prevKernelTime > 0L) {
                    val deltaIdle = currentIdle - prevIdleTime
                    val deltaKernel = currentKernel - prevKernelTime
                    val deltaUser = currentUser - prevUserTime
                    val total = deltaKernel + deltaUser

                    if (total > 0L) {
                        val load = (1.0 - (deltaIdle.toDouble() / total.toDouble())) * 100.0
                        cpuPercent = (Math.round(load * 10.0) / 10.0).toFloat().coerceIn(0f, 100f)
                    }
                }

                prevIdleTime = currentIdle
                prevKernelTime = currentKernel
                prevUserTime = currentUser
            }

            if (cpuPercent <= 0f) {
                // First sample (or GetSystemTimes unavailable): read the OS-wide CPU load.
                val osBean = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
                val osLoad = osBean?.cpuLoad ?: -1.0
                if (osLoad >= 0.0) {
                    cpuPercent = (osLoad * 100.0).toFloat().coerceIn(0f, 100f)
                }
            }
        } catch (_: Throwable) {
            cpuPercent = 0f
        }

        // 3. Network live speed measurement
        var downloadKbps = 0.0
        var uploadKbps = 0.0
        var primaryAdapterName = "Network Adapter"

        try {
            val now = System.currentTimeMillis()
            val counters = readNetworkCounters()
            if (counters != null) {
                primaryAdapterName = counters.adapterName
                val received = counters.receivedBytes
                val transmitted = counters.transmittedBytes

                if (previousNetworkTimestamp > 0 && now > previousNetworkTimestamp &&
                    received >= previousReceivedBytes && transmitted >= previousTransmittedBytes
                ) {
                    val elapsedSeconds = (now - previousNetworkTimestamp) / 1000.0
                    downloadKbps = roundOneDecimal(
                        (received - previousReceivedBytes) * 8.0 / 1000.0 / elapsedSeconds
                    )
                    uploadKbps = roundOneDecimal(
                        (transmitted - previousTransmittedBytes) * 8.0 / 1000.0 / elapsedSeconds
                    )
                }

                previousReceivedBytes = received
                previousTransmittedBytes = transmitted
                previousNetworkTimestamp = now
            }
        } catch (_: Throwable) {}

        // 4. System Uptime calculation
        val uptimeStr = try {
            val uptimeMs = ManagementFactory.getRuntimeMXBean().uptime
            val seconds = uptimeMs / 1000
            val minutes = (seconds / 60) % 60
            val hours = (seconds / 3600) % 24
            val days = seconds / 86400

            when {
                days > 0 -> "$days d $hours hrs $minutes min"
                hours > 0 -> "$hours hrs $minutes min"
                else -> "$minutes min ${seconds % 60} sec"
            }
        } catch (_: Throwable) {
            "Active"
        }

        LiveSystemTelemetry(
            cpuName = cpuName,
            cpuUsagePercent = cpuPercent,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuSpeedGhz = cpuBaseSpeedGhz,
            cpuTempEstimate = (36 + (cpuPercent * 0.4)).toInt().coerceIn(36, 90),

            ramTotalGb = totalRamGb,
            ramUsedGb = usedRamGb,
            ramFreeGb = freeRamGb,
            ramUsagePercent = ramPercent.coerceIn(1f, 100f),

            gpuName = gpuName,
            gpuUsagePercent = 0f,
            gpuUsageAvailable = false,
            vramTotalGb = gpuVramGb,

            downloadSpeedKbps = downloadKbps,
            uploadSpeedKbps = uploadKbps,
            networkAdapterName = primaryAdapterName,

            systemUptime = uptimeStr
        )
    }

    private data class NetworkCounters(
        val adapterName: String,
        val receivedBytes: Long,
        val transmittedBytes: Long
    )

    private fun readNetworkCounters(): NetworkCounters? {
        var adapterName = "Network Adapter"
        var receivedBytes = 0L
        var transmittedBytes = 0L
        var found = false

        val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: return null
        for (ni in interfaces) {
            if (ni.isLoopback || !ni.isUp || ni.isVirtual) continue
            if (!found) adapterName = ni.displayName.substringBefore("(").trim()

            try {
                val row = IPHlpAPI.MIB_IFROW()
                row.dwIndex = ni.index
                if (IPHlpAPI.INSTANCE.GetIfEntry(row) == 0) {
                    receivedBytes += row.dwInOctets.toLong() and 0xFFFFFFFFL
                    transmittedBytes += row.dwOutOctets.toLong() and 0xFFFFFFFFL
                    found = true
                }
            } catch (_: Throwable) {
            }
        }

        return if (found) NetworkCounters(adapterName, receivedBytes, transmittedBytes) else null
    }

    private fun roundOneDecimal(value: Double): Double = Math.round(value * 10.0) / 10.0

    private fun fileTimeToLong(ft: FILETIME): Long {
        return (ft.dwHighDateTime.toLong() shl 32) or (ft.dwLowDateTime.toLong() and 0xFFFFFFFFL)
    }
}
