package theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val Light = ColorScheme(
        primary = Color(0xFF9F8FFF),          // Refined Vibrant Purple
        secondary = Color(0xFFB5A6FF),        // Harmonious Purple
        background = Color(0xFFFCFCFD),       // Pristine White
        surface = Color(0xFFFFFFFF),          // Pure White
        error = Color(0xFFDC2626),           // Clear Red
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFFFFFFFF),
        onBackground = Color(0xFF18181B),     // Darker Text
        onSurface = Color(0xFF18181B),        // Darker Text
        onError = Color(0xFFFFFFFF),
        switchCheckedTrackColor = Color(0xFF9F8FFF)  // Match Primary
    )

    val Dark = ColorScheme(
        primary = Color(0xFFB4A2FF),          // Luminous Purple
        secondary = Color(0xFFC7B8FF),        // Soft Glow Purple
        background = Color(0xFF0A0A0B),       // Near Black
        surface = Color(0xFF141417),          // Deep Dark
        error = Color(0xFFFF4444),            // Bright Red
        onPrimary = Color(0xFF0A0A0B),        // Near Black
        onSecondary = Color(0xFF0A0A0B),      // Near Black
        onBackground = Color(0xFFFCFCFC),     // Crisp White
        onSurface = Color(0xFFFCFCFC),        // Crisp White
        onError = Color(0xFF0A0A0B),          // Near Black
        switchCheckedTrackColor = Color(0xFFB4A2FF)  // Match Primary
    )

    // Enhanced accent colors
    val headerWingetTextColorLight = Color(0xFF9F8FFF)    // Match Light Primary
    val headerWingetTextColorDark = Color(0xFFB4A2FF)     // Match Dark Primary
    val diskManagerBackgroundColor = Color(0xFFB5A6FF)    // Harmonious Purple

    //  status colors
    val successLight = Color(0xFF10B981)      // Vibrant Green
    val successDark = Color(0xFF34D399)       // Glowing Green
    val warningLight = Color(0xFFF59E0B)      // Bold Orange
    val warningDark = Color(0xFFFFBA08)       // Bright Gold
    val availableUpdateLight = Color(0xFF10B981)  // Vibrant Green
    val availableUpdateDark = Color(0xFF34D399)   // Glowing Green

    // Enhanced action buttons
    val downloadButtonLight = Color(0xFF9F8FFF)  // Match Light Primary
    val downloadButtonDark = Color(0xFFB4A2FF)   // Match Dark Primary
    val deleteButtonLight = Color(0xFFDC2626)    // Clear Red
    val deleteButtonDark = Color(0xFFFF4444)     // Bright Red

    // System accents
    val windowsAccentLight = Color(0xFF9F8FFF)   // Match Light Primary
    val windowsAccentDark = Color(0xFFB4A2FF)    // Match Dark Primary

    // Refined accents
    val accentHighlight = Color(0xFFB5A6FF)      // Harmonious Purple
    val accentGlow = Color(0xFFC7B8FF)           // Soft Glow Purple
    val accentVibrant = Color(0xFF9F8FFF)        // Match Light Primary
}