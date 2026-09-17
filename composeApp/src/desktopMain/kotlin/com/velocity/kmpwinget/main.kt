package com.velocity.kmpwinget

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.velocity.kmpwinget.data.datasource.WindowsNativeBridge
import com.velocity.kmpwinget.di.appModule
import com.velocity.kmpwinget.domain.model.FavoritesManager
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.ui.MainScreen
import com.velocity.kmpwinget.ui.components.FloatingNotchWindow
import com.velocity.kmpwinget.viewmodel.MainViewModel
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.kmp_winget
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        val viewModel = koinInject<MainViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val isNotchEnabled by FavoritesManager.isNotchEnabled

        // Floating Quick-Launch Notch Overlay
        if (isNotchEnabled) {
            FloatingNotchWindow(
                allPackages = uiState.allPackages,
                onDismiss = { FavoritesManager.setNotchEnabled(false) }
            )
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
            LaunchedEffect(window) {
                window.minimumSize = Dimension(720, 560)
            }

            val isDarkMode = ThemeState.isDarkMode.value
            LaunchedEffect(window, isDarkMode) {
                WindowsNativeBridge.applyTheme(window, isDarkMode)
            }

            MainScreen(viewModel = viewModel)
        }
    }
}
