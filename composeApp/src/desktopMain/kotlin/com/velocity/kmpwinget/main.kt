package com.velocity.kmpwinget

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.velocity.kmpwinget.di.appModule
import com.velocity.kmpwinget.ui.MainScreen
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.kmp_winget
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }

    val icon = painterResource(Res.drawable.kmp_winget)
    val state = rememberWindowState(
        width = 800.dp,
        height = 800.dp
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp winget",
        icon = icon,
        resizable = false,
        state = state
    ) {
        MainScreen()
    }
}