package com.velocity.kmpwinget.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val lightColorScheme = lightColorScheme(
    primary = AppColors.primaryLight,
    onPrimary = Color.White,
    primaryContainer = AppColors.primaryContainerLight,
    onPrimaryContainer = AppColors.primaryLight,
    secondary = AppColors.glowAccentLight,
    onSecondary = Color.White,
    secondaryContainer = AppColors.upgradeBadgeBgLight,
    onSecondaryContainer = AppColors.upgradeAvailableLight,
    background = AppColors.canvasBackgroundLight,
    onBackground = Color(0xFF111827),
    surface = AppColors.islandSurfaceLight,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0x1F000000),
    error = AppColors.dangerRedLight,
    onError = Color.White
)

val darkColorScheme = darkColorScheme(
    primary = AppColors.primaryDark,
    onPrimary = Color(0xFF0F0B24),
    primaryContainer = AppColors.primaryContainerDark,
    onPrimaryContainer = AppColors.primaryDark,
    secondary = AppColors.glowAccentDark,
    onSecondary = Color(0xFF0F0B24),
    secondaryContainer = AppColors.upgradeBadgeBgDark,
    onSecondaryContainer = AppColors.upgradeAvailableDark,
    background = AppColors.canvasBackgroundDark,
    onBackground = Color(0xFFF9FAFB),
    surface = AppColors.islandSurfaceDark,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1E1E24),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0x2EFFFFFF),
    error = AppColors.dangerRedDark,
    onError = Color(0xFF450A0A)
)
