package theme

import androidx.compose.ui.graphics.Color

data class ColorScheme(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val switchCheckedTrackColor: Color  // Add this new property
)
