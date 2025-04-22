package com.velocity.kmpwinget.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kmp_winget.composeapp.generated.resources.Lato_Bold
import kmp_winget.composeapp.generated.resources.Lato_Regular
import kmp_winget.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun AppTypography(): Typography {
    val titleFont = FontFamily(
        Font(resource = Res.font.Lato_Bold)
    )

    val bodyFont = FontFamily(
        Font(resource = Res.font.Lato_Regular)
    )

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = titleFont,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
        ),

        bodyLarge = TextStyle(
            fontFamily = titleFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        ),

        bodyMedium = TextStyle(
            fontFamily = titleFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        ),

        bodySmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
        ),
    )
}