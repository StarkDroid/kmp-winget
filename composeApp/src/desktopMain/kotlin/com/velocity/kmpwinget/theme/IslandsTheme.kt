package com.velocity.kmpwinget.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Islands Theme Container Modifier.
 * Groups UI sections into beautiful elevated "Islands" with clean borders and subtle shadows.
 */
fun Modifier.islandContainer(
    shape: Shape = RoundedCornerShape(16.dp),
    isDarkMode: Boolean = false,
    elevation: Dp = 2.dp
): Modifier = composed {
    val bg = if (isDarkMode) AppColors.islandSurfaceDark else AppColors.islandSurfaceLight
    val strokeColor = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
    val shadowColor = if (isDarkMode) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.05f)

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(bg)
        .border(1.dp, strokeColor, shape)
}

/**
 * Aesthetic Tab Underglow Modifier.
 * Renders a soft luminous neon underglow beam directly beneath the selected tab item.
 */
fun Modifier.tabUnderglow(
    isSelected: Boolean,
    isDarkMode: Boolean,
    glowColor: Color? = null
): Modifier = composed {
    if (!isSelected) return@composed this

    val accent = glowColor ?: (if (isDarkMode) AppColors.glowAccentDark else AppColors.glowAccentLight)

    this.drawBehind {
        val glowHeight = 12.dp.toPx()
        val beamHeight = 2.5.dp.toPx()
        val width = size.width
        val height = size.height

        // 1. Diffuse soft glow radiating upward/downward from the bottom
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    accent.copy(alpha = if (isDarkMode) 0.08f else 0.04f),
                    accent.copy(alpha = if (isDarkMode) 0.35f else 0.22f)
                ),
                startY = height - glowHeight,
                endY = height
            ),
            topLeft = Offset(0f, height - glowHeight),
            size = Size(width, glowHeight)
        )

        // 2. Focused bright underglow beam at the bottom edge
        val beamInset = width * 0.12f
        val beamWidth = width - (beamInset * 2)

        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    accent.copy(alpha = if (isDarkMode) 0.95f else 0.85f),
                    accent,
                    accent.copy(alpha = if (isDarkMode) 0.95f else 0.85f),
                    Color.Transparent
                ),
                startX = beamInset,
                endX = beamInset + beamWidth
            ),
            topLeft = Offset(beamInset, height - beamHeight),
            size = Size(beamWidth, beamHeight)
        )
    }
}

/**
 * Subtle interactive island control button/chip modifier.
 */
fun Modifier.subtleIslandControl(
    shape: Shape = RoundedCornerShape(8.dp),
    isDarkMode: Boolean = false,
    selected: Boolean = false
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        selected -> if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
        isHovered -> if (isDarkMode) Color(0x1FFFFFFF) else Color(0x0F000000)
        else -> if (isDarkMode) Color(0x0FFFFFFF) else Color(0x08000000)
    }

    val strokeColor = when {
        selected -> if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
        isHovered -> if (isDarkMode) Color(0x33FFFFFF) else Color(0x1F000000)
        else -> if (isDarkMode) Color(0x14FFFFFF) else Color(0x0D000000)
    }

    this
        .hoverable(interactionSource)
        .clip(shape)
        .background(bg)
        .border(1.dp, strokeColor, shape)
}
