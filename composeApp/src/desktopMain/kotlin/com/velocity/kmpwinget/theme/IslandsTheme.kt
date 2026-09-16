package com.velocity.kmpwinget.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
 * Aesthetic Button Underglow Modifier.
 * Casts a luminous underglow beneath interactive action buttons, intensifying when hovered.
 */
fun Modifier.buttonUnderglow(
    isDarkMode: Boolean,
    glowColor: Color,
    isHovered: Boolean = false
): Modifier = composed {
    val animatedHoverAlpha by animateFloatAsState(
        targetValue = if (isHovered) 1.35f else 0.9f,
        animationSpec = tween(durationMillis = 150),
        label = "btnGlowHover"
    )

    this.drawBehind {
        val beamHeight = 2.dp.toPx()
        val width = size.width
        val height = size.height

        val beamAlpha = (if (isDarkMode) 0.95f else 0.85f) * animatedHoverAlpha.coerceAtMost(1.0f)

        // Focused bright underglow beam at the bottom edge
        val beamInset = width * 0.08f
        val beamWidth = width - (beamInset * 2f)

        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = beamAlpha * 0.85f),
                    glowColor.copy(alpha = beamAlpha),
                    glowColor.copy(alpha = beamAlpha * 0.85f),
                    Color.Transparent
                ),
                startX = beamInset,
                endX = beamInset + beamWidth
            ),
            topLeft = Offset(beamInset, height - beamHeight),
            size = Size(beamWidth, beamHeight),
            cornerRadius = CornerRadius(beamHeight / 2f, beamHeight / 2f)
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

/**
 * Aesthetic rounded checkbox matching the Islands design system.
 */
@Composable
fun IslandRoundedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val checkedBg = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
    val uncheckedBg = when {
        isHovered -> if (isDarkMode) Color(0x2EFFFFFF) else Color(0x14000000)
        else -> if (isDarkMode) Color(0x14FFFFFF) else Color(0x0A000000)
    }

    val targetBg = if (checked) checkedBg else uncheckedBg
    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBg"
    )

    val targetBorder = when {
        checked -> checkedBg
        isHovered -> if (isDarkMode) Color(0x55FFFFFF) else Color(0x44000000)
        else -> if (isDarkMode) Color(0x33FFFFFF) else Color(0x24000000)
    }
    val animatedBorder by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBorder"
    )

    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = modifier
            .size(size)
            .hoverable(interactionSource)
            .clip(shape)
            .background(animatedBg)
            .border(1.dp, animatedBorder, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (checked) "Checked" else "Unchecked",
                tint = if (isDarkMode) Color.Black else Color.White,
                modifier = Modifier.size(size * 0.72f)
            )
        }
    }
}

/**
 * Luminous underglow action button for Update actions.
 */
@Composable
fun UpdateUnderglowButton(
    text: String = "Update",
    onClick: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.TwoTone.Download
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isHovered) (-2).dp else 0.dp,
        animationSpec = tween(150),
        label = "updateBtnOffset"
    )

    val glowColor = if (isDarkMode) AppColors.glowAccentDark else AppColors.primaryLight
    val shape = RoundedCornerShape(8.dp)

    val defaultBg = if (isDarkMode) Color(0x1F9D8CFF) else Color(0x145B4DFF)
    val hoverBg = if (isDarkMode) Color(0x359D8CFF) else Color(0x245B4DFF)
    val animatedBg by animateColorAsState(
        targetValue = if (isHovered) hoverBg else defaultBg,
        animationSpec = tween(150),
        label = "updateBtnBg"
    )

    val strokeColor = if (isDarkMode) {
        if (isHovered) Color(0x669D8CFF) else Color(0x339D8CFF)
    } else {
        if (isHovered) Color(0x555B4DFF) else Color(0x265B4DFF)
    }
    val animatedStroke by animateColorAsState(
        targetValue = strokeColor,
        animationSpec = tween(150),
        label = "updateBtnStroke"
    )

    val contentColor = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .clip(shape)
            .background(animatedBg)
            .border(1.dp, animatedStroke, shape)
            .buttonUnderglow(
                isDarkMode = isDarkMode,
                glowColor = glowColor,
                isHovered = isHovered
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(15.dp),
                    tint = contentColor
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}

/**
 * Luminous underglow action button for Delete/Uninstall actions.
 */
@Composable
fun DeleteUnderglowButton(
    onClick: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    text: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isHovered) (-2).dp else 0.dp,
        animationSpec = tween(150),
        label = "deleteBtnOffset"
    )

    val glowColor = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
    val shape = RoundedCornerShape(8.dp)

    val defaultBg = if (isDarkMode) Color(0x1CFF8383) else Color(0x12DC2626)
    val hoverBg = if (isDarkMode) Color(0x35FF8383) else Color(0x22DC2626)
    val animatedBg by animateColorAsState(
        targetValue = if (isHovered) hoverBg else defaultBg,
        animationSpec = tween(150),
        label = "deleteBtnBg"
    )

    val strokeColor = if (isDarkMode) {
        if (isHovered) Color(0x66FF8383) else Color(0x33FF8383)
    } else {
        if (isHovered) Color(0x55DC2626) else Color(0x26DC2626)
    }
    val animatedStroke by animateColorAsState(
        targetValue = strokeColor,
        animationSpec = tween(150),
        label = "deleteBtnStroke"
    )

    val contentColor = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .clip(shape)
            .background(animatedBg)
            .border(1.dp, animatedStroke, shape)
            .buttonUnderglow(
                isDarkMode = isDarkMode,
                glowColor = glowColor,
                isHovered = isHovered
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                if (text != null) PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                else PaddingValues(7.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.TwoTone.Delete,
                contentDescription = text ?: "Uninstall",
                modifier = Modifier.size(15.dp),
                tint = contentColor
            )
            if (text != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                )
            }
        }
    }
}
