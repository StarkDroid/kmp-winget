package com.velocity.kmpwinget.data.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.ptr.IntByReference
import java.awt.Window

object WindowsNativeBridge {

    private interface Dwmapi : Library {
        fun DwmSetWindowAttribute(hwnd: HWND, dwAttribute: Int, pvAttribute: Pointer, cbAttribute: Int): Int

        companion object {
            val INSTANCE: Dwmapi = Native.load("dwmapi", Dwmapi::class.java)
        }
    }

    private const val DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
    private const val DWMWA_CAPTION_COLOR = 35
    private const val DWMWA_TEXT_COLOR = 36

    val isWindows: Boolean
        get() = System.getProperty("os.name", "").contains("Windows", ignoreCase = true)

    val windowsBuildNumber: Int by lazy {
        if (!isWindows) return@lazy 0
        try {
            val versionStr = System.getProperty("os.version", "0")
            val buildFromProp = versionStr.substringAfterLast('.').toIntOrNull()
            if (buildFromProp != null && buildFromProp > 1000) {
                return@lazy buildFromProp
            }
            val build = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
                "CurrentBuild"
            )
            build.toIntOrNull() ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    val isWindows11OrGreater: Boolean
        get() = windowsBuildNumber >= 22000

    /**
     * Checks if the Windows system is currently in Dark Mode.
     */
    fun isSystemInDarkMode(): Boolean {
        if (!isWindows) return false
        return try {
            val lightTheme = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER,
                "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "AppsUseLightTheme"
            )
            lightTheme == 0
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Gets HWND pointer for an AWT Window.
     */
    fun getHWND(window: Window): HWND? {
        if (!isWindows || !window.isDisplayable) return null
        return try {
            val pointer = Native.getWindowPointer(window)
            if (pointer != null && Pointer.nativeValue(pointer) != 0L) {
                HWND(pointer)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Applies Windows title bar theme matching the active dark/light mode.
     */
    fun applyTheme(window: Window, isDarkMode: Boolean): Boolean {
        if (!isWindows) return false
        val hwnd = getHWND(window) ?: return false

        try {
            // 1. Set dark mode attribute on titlebar
            setDarkModeTitleBar(hwnd, isDarkMode)

            // 2. Set title bar caption color to match the canvas background
            if (isWindows11OrGreater) {
                // COLORREF format in Win32: 0x00BBGGRR
                val colorRef = if (isDarkMode) {
                    (0x0F shl 16) or (0x0C shl 8) or 0x0C // #0C0C0F
                } else {
                    (0xF8 shl 16) or (0xF3 shl 8) or 0xF1 // #F1F3F8
                }
                val captionPtr = IntByReference(colorRef).pointer
                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, captionPtr, 4)

                val textRef = if (isDarkMode) 0x00FFFFFF else 0x00111827
                val textPtr = IntByReference(textRef).pointer
                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, textPtr, 4)
            }
            return true
        } catch (_: Throwable) {
            return false
        }
    }

    fun setDarkModeTitleBar(hwnd: HWND, isDarkMode: Boolean): Boolean {
        if (!isWindows) return false
        return try {
            val value = if (isDarkMode) 1 else 0
            val ptr = IntByReference(value).pointer
            val attribute = if (windowsBuildNumber >= 18985) {
                DWMWA_USE_IMMERSIVE_DARK_MODE
            } else {
                DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1
            }
            val hr = Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, attribute, ptr, 4)
            hr == 0
        } catch (_: Throwable) {
            false
        }
    }
}
