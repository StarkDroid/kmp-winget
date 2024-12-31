package theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
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
    val colors = if (isDarkTheme) {
        darkColors(
            primary = AppColors.Dark.primary,
            secondary = AppColors.Dark.secondary,
            background = AppColors.Dark.background,
            surface = AppColors.Dark.surface,
            onPrimary = AppColors.Dark.onPrimary,
            onSecondary = AppColors.Dark.onSecondary,
            onBackground = AppColors.Dark.onBackground,
            onSurface = AppColors.Dark.onSurface,
            onError = AppColors.Dark.onError
        )
    } else {
        lightColors(
            primary = AppColors.Light.primary,
            secondary = AppColors.Light.secondary,
            background = AppColors.Light.background,
            surface = AppColors.Light.surface,
            onPrimary = AppColors.Light.onPrimary,
            onSecondary = AppColors.Light.onSecondary,
            onBackground = AppColors.Light.onBackground,
            onSurface = AppColors.Light.onSurface,
            onError = AppColors.Light.onError
        )
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}