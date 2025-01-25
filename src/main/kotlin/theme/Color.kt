package theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val Light = ColorScheme(
        primary = Color(0xFF6200EE),
        primaryVariant = Color(0xFF9F8FFF),
        secondary = Color(0xFF03DAC6),
        secondaryVariant = Color(0xFFFFFFFF),
        background = Color(0xFFFCFCFD),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFB00020),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFF000000),
        onError = Color(0xFFFF8383),
    )

    val Dark = ColorScheme(
        primary = Color(0xFF2E236C),
        primaryVariant = Color(0xFF907ce5),
        secondary = Color(0xFFC8ACD6),
        secondaryVariant = Color(0xFFFFFFFF),
        background = Color(0xFF0A0A0B),
        surface = Color(0xFF141417),
        error = Color(0xFFCF6679),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFCFCFC),
        onSurface = Color(0xFFFFFFFF),
        onError = Color(0xFFFF8383),
    )

    // Define Custom Colors
    val switchCheckedTrackColor = Color(0xFF16C47F)

    val headerWingetTextColorLight = Color(0xFF0A5EB0)
    val headerWingetTextColorDark = Color(0xFF0D92F4)

    val upgradeAvailableLight = Color(0xFF10B981)
    val upgradeAvailableDark = Color(0xFF34D399)

    val deleteButton = Color(0xFFFF8383)
}