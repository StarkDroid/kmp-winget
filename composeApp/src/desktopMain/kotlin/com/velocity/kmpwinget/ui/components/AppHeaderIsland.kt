package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ModeNight
import androidx.compose.material.icons.twotone.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.theme.islandContainer
import com.velocity.kmpwinget.theme.subtleIslandControl
import com.velocity.kmpwinget.utils.BuildConfig
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.kmp_winget
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppHeaderIsland(
    isDarkMode: Boolean,
    wingetVersion: String,
    isWingetAvailable: Boolean = true
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "themeRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .islandContainer(shape = RoundedCornerShape(16.dp), isDarkMode = isDarkMode)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity Brand
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                    )
                    .border(
                        1.dp,
                        if (isDarkMode) AppColors.primaryDark.copy(alpha = 0.4f) else AppColors.primaryLight.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.kmp_winget),
                    modifier = Modifier.size(26.dp),
                    contentDescription = "App Logo"
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WinGet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Package Manager",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                val isAvailable = isWingetAvailable && wingetVersion != "Not Detected"
                Text(
                    text = if (isAvailable) {
                        "WinGet $wingetVersion • v${BuildConfig.VERSION}"
                    } else {
                        "WinGet Not Detected (Error) • v${BuildConfig.VERSION}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isAvailable) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (isAvailable) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                        }
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Dark / Light Mode Toggle Button
        Box(
            modifier = Modifier
                .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                .clickable { ThemeState.toggleDarkMode() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.TwoTone.WbSunny else Icons.TwoTone.ModeNight,
                contentDescription = "Toggle Theme",
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation),
                tint = if (isDarkMode) Color(0xFFFFD54F) else AppColors.primaryLight
            )
        }
    }
}
