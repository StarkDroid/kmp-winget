package theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val Light = ColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFB00020),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFF000000),
        onError = Color(0xFFFF8383),
    )

    // Dark Theme Colors
    val Dark = ColorScheme(
        primary = Color(0xFF2E236C),
        secondary = Color(0xFFC8ACD6),
        background = Color(0xFF9AA6B2),
        surface = Color(0xFF222831),
        error = Color(0xFFCF6679),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),
        onError = Color(0xFFFF8383),
    )

    // Define Custom Colors
    val darkSwitchCheckedTrackColor = Color(0xFF16C47F)
    val lightSwitchCheckedTrackColor = Color(0xFF54C392)
    val diskManagerBackgroundColor = Color(0xFFffd65a)
}