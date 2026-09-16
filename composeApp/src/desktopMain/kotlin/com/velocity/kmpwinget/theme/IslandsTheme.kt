package com.velocity.kmpwinget.theme

import androidx.compose.animation.animateColorAsState
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
        targetValue = if (isHovered) 1.25f else 0.85f,
        animationSpec = tween(durationMillis = 150),
        label = "btnGlowHover"
    )

    this.drawBehind {
        val glowHeight = 10.dp.toPx()
        val beamHeight = 2.dp.toPx()
        val width = size.width
        val height = size.height

        val baseDiffuseAlpha = (if (isDarkMode) 0.28f else 0.18f) * animatedHoverAlpha.coerceAtMost(1.5f)
        val beamAlpha = (if (isDarkMode) 0.90f else 0.80f) * animatedHoverAlpha.coerceAtMost(1.0f)

        // 1. Diffuse soft glow radiating upward/downward from the bottom
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = baseDiffuseAlpha * 0.2f),
                    glowColor.copy(alpha = baseDiffuseAlpha)
                ),
                startY = (height - glowHeight).coerceAtLeast(0f),
                endY = height
            ),
            topLeft = Offset(0f, (height - glowHeight).coerceAtLeast(0f)),
            size = Size(width, glowHeight.coerceAtMost(height))
        )

        // 2. Focused bright underglow beam at the bottom edge
        val beamInset = width * 0.08f
        val beamWidth = width - (beamInset * 2f)

        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = beamAlpha),
                    glowColor,
                    glowColor.copy(alpha = beamAlpha),
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

    val glowColor = if (isDarkMode) AppColors.glowAccentDark else AppColors.primaryLight
    val shape = RoundedCornerShape(6.dp)

    val baseBg = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
    val hoverBg = if (isDarkMode) AppColors.glowAccentDark else Color(0xFF4F40EE)
    val animatedBg by animateColorAsState(
        targetValue = if (isHovered) hoverBg else baseBg,
        animationSpec = tween(150),
        label = "updateBtnBg"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isHovered) 4.dp else 1.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = if (isDarkMode) 0.5f else 0.25f),
                spotColor = glowColor.copy(alpha = if (isDarkMode) 0.6f else 0.35f)
            )
            .clip(shape)
            .background(animatedBg)
            .border(
                1.dp,
                if (isDarkMode) Color(0x44FFFFFF) else Color(0x22FFFFFF),
                shape
            )
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
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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

    val glowColor = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
    val shape = RoundedCornerShape(6.dp)

    val defaultBg = if (isDarkMode) AppColors.dangerBadgeBgDark else AppColors.dangerBadgeBgLight
    val hoverBg = if (isDarkMode) Color(0xFF5C0E0E) else Color(0xFFFEE2E2)
    val animatedBg by animateColorAsState(
        targetValue = if (isHovered) hoverBg else defaultBg,
        animationSpec = tween(150),
        label = "deleteBtnBg"
    )

    val strokeColor = if (isDarkMode) {
        if (isHovered) AppColors.dangerRedDark.copy(alpha = 0.8f) else AppColors.dangerRedDark.copy(alpha = 0.45f)
    } else {
        if (isHovered) AppColors.dangerRedLight.copy(alpha = 0.7f) else AppColors.dangerRedLight.copy(alpha = 0.35f)
    }
    val animatedStroke by animateColorAsState(
        targetValue = strokeColor,
        animationSpec = tween(150),
        label = "deleteBtnStroke"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isHovered) 3.dp else 0.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = if (isDarkMode) 0.4f else 0.2f),
                spotColor = glowColor.copy(alpha = if (isDarkMode) 0.5f else 0.25f)
            )
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
                else PaddingValues(6.dp)
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
                modifier = Modifier.size(if (text != null) 15.dp else 16.dp),
                tint = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
            )
            if (text != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                    )
                )
            }
        }
    }
}
