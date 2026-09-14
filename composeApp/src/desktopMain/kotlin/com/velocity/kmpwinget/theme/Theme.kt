package com.velocity.kmpwinget.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import com.velocity.kmpwinget.data.datasource.WindowsNativeBridge

val LocalIsDarkMode = compositionLocalOf { false }

object ThemeState {
    val isDarkMode = mutableStateOf(WindowsNativeBridge.isSystemInDarkMode())

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }
}

@Composable
fun AppTheme(
    isDarkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) darkColorScheme else lightColorScheme

    CompositionLocalProvider(
        LocalIsDarkMode provides isDarkTheme
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography(),
            content = content
        )
    }
}
