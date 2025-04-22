package com.velocity.kmpwinget.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

object ThemeState {
    val isDarkMode = mutableStateOf(false)
}

@Composable
fun AppTheme(
    isDarkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = AppTypography()
    )
}