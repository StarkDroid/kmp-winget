package com.velocity.kmpwinget

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.velocity.kmpwinget.data.datasource.WindowsNativeBridge
import com.velocity.kmpwinget.di.appModule
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.ui.MainScreen
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.kmp_winget
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() = application {
    startKoin {
        modules(appModule)
    }

    val icon = painterResource(Res.drawable.kmp_winget)
    val state = rememberWindowState(
        size = DpSize(960.dp, 800.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "WinGet Package Manager",
        icon = icon,
        resizable = true,
        state = state
    ) {
        // Enforce minimum window dimension
        LaunchedEffect(window) {
            window.minimumSize = Dimension(720, 560)
        }

        // Synchronize native Windows title bar with Dark/Light mode
        val isDarkMode = ThemeState.isDarkMode.value
        LaunchedEffect(window, isDarkMode) {
            WindowsNativeBridge.applyTheme(window, isDarkMode)
        }

        MainScreen()
    }
}
