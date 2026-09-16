package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.velocity.kmpwinget.data.datasource.AppLauncher
import com.velocity.kmpwinget.data.datasource.Win32IconLoader
import com.velocity.kmpwinget.domain.model.FavoritesManager
import com.velocity.kmpwinget.domain.model.NotchPosition
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.ThemeState
import kotlinx.coroutines.launch
import java.awt.GraphicsEnvironment

/**
 * Floating Notch Overlay window snapped to the top of the primary monitor screen.
 * Displays a sleek, minimalist resting capsule and expands with authentic macOS dock fisheye
 * magnification when hovered for instant quick-launching of pinned favorite apps.
 */
@Composable
fun FloatingNotchWindow(
    allPackages: List<Package>,
    onDismiss: () -> Unit = {}
) {
    val windowState = rememberWindowState(
        size = DpSize(660.dp, 110.dp)
    )

    Window(
        onCloseRequest = onDismiss,
        title = "KMP WinGet Quick Launch",
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false,
        state = windowState
    ) {
        val positionSetting by FavoritesManager.notchPosition

        // Snapped to Top Center (or configured position) of the screen
        LaunchedEffect(window, positionSetting) {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val bounds = ge.defaultScreenDevice.defaultConfiguration.bounds
            val winWidth = window.width
            val targetX = when (positionSetting) {
                NotchPosition.TOP_LEFT -> bounds.x + 40
                NotchPosition.TOP_RIGHT -> bounds.x + bounds.width - winWidth - 40
                NotchPosition.TOP_CENTER -> bounds.x + (bounds.width - winWidth) / 2
            }
            window.setLocation(targetX, bounds.y)
        }

        val favoriteIds by FavoritesManager.favoriteIds
        val favoritePackages = remember(favoriteIds, allPackages) {
            favoriteIds.mapNotNull { id -> allPackages.find { it.id == id } }
        }

        val coroutineScope = rememberCoroutineScope()
        val isDarkMode = ThemeState.isDarkMode.value

        val notchInteractionSource = remember { MutableInteractionSource() }
        val isNotchHovered by notchInteractionSource.collectIsHoveredAsState()
        var hoveredIconIndex by remember { mutableStateOf<Int?>(null) }

        val isExpanded = isNotchHovered || hoveredIconIndex != null

        // Resting: 200.dp x 30.dp, Hovered: minOf(600.dp, maxOf(280.dp, favorites.size * 68.dp)) x 80.dp
        val targetWidth = if (isExpanded) {
            minOf(600.dp, maxOf(280.dp, (favoritePackages.size * 68).dp))
        } else {
            200.dp
        }
        val targetHeight = if (isExpanded) 80.dp else 30.dp

        val animatedWidth by animateDpAsState(
            targetValue = targetWidth,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "notchWidth"
        )
        val animatedHeight by animateDpAsState(
            targetValue = targetHeight,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "notchHeight"
        )

        val alignment = when (positionSetting) {
            NotchPosition.TOP_LEFT -> Alignment.TopStart
            NotchPosition.TOP_RIGHT -> Alignment.TopEnd
            NotchPosition.TOP_CENTER -> Alignment.TopCenter
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            val notchShape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            )

            val notchBg = if (isDarkMode) Color(0xF2121218) else Color(0xF5181824)
            val strokeColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x22FFFFFF)
            val shadowColor = Color.Black.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .shadow(
                        elevation = if (isExpanded) 12.dp else 4.dp,
                        shape = notchShape,
                        clip = false,
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(notchShape)
                    .background(notchBg)
                    .border(1.dp, strokeColor, notchShape)
                    .hoverable(notchInteractionSource),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(90)) },
                    label = "notchContent"
                ) { expanded ->
                    if (!expanded) {
                        // Resting State: Minimalist, sleek rounded notch capsule with glowing dot and text
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.upgradeGlow.copy(alpha = pulseAlpha))
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = "Quick Launch",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.3.sp
                                )
                            )

                            if (favoritePackages.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x2EFFFFFF))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${favoritePackages.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA78BFA)
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Hovered State: macOS Dock Fisheye Magnification
                        if (favoritePackages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⭐ Star apps in WinGet to pin here",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                favoritePackages.forEachIndexed { index, pkg ->
                                    NotchDockItem(
                                        pkg = pkg,
                                        index = index,
                                        hoveredIndex = hoveredIconIndex,
                                        onHoverChanged = { isItemHovered ->
                                            hoveredIconIndex = if (isItemHovered) index else {
                                                if (hoveredIconIndex == index) null else hoveredIconIndex
                                            }
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                AppLauncher.launchPackage(pkg)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotchDockItem(
    pkg: Package,
    index: Int,
    hoveredIndex: Int?,
    onHoverChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var appIcon by remember(pkg.iconPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pkg.iconPath) {
        if (!pkg.iconPath.isNullOrBlank()) {
            appIcon = Win32IconLoader.loadIcon(pkg.iconPath)
        }
    }

    val isDirectlyHovered = hoveredIndex == index
    val isAdjacent = hoveredIndex != null && (hoveredIndex == index - 1 || hoveredIndex == index + 1)

    val targetScale = when {
        isDirectlyHovered -> 1.35f
        isAdjacent -> 1.15f
        else -> 1.0f
    }
    val targetOffsetY = when {
        isDirectlyHovered -> (-8).dp
        isAdjacent -> (-4).dp
        else -> 0.dp
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dockScale"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dockOffset"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isItemHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isItemHovered) {
        onHoverChanged(isItemHovered)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        // App name tooltip when hovered
        if (isDirectlyHovered) {
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xF0181822))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

        // App Icon or Letter Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .then(
                    if (appIcon != null) Modifier.background(Color.Transparent)
                    else Modifier.background(getNotchAvatarGradient(pkg.name))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon!!,
                    contentDescription = pkg.name,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                val initial = pkg.name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

private fun getNotchAvatarGradient(name: String): Brush {
    val hash = kotlin.math.abs(name.hashCode())
    val palettes = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
        listOf(Color(0xFF10B981), Color(0xFF047857)),
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        listOf(Color(0xFFF97316), Color(0xFFC2410C)),
        listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
        listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
    )
    val chosen = palettes[hash % palettes.size]
    return Brush.linearGradient(chosen)
}
