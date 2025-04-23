package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CleaningServices
import androidx.compose.material.icons.twotone.ModeNight
import androidx.compose.material.icons.twotone.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.utils.BuildConfig
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.header_subtitle
import kmp_winget.composeapp.generated.resources.header_title
import kmp_winget.composeapp.generated.resources.kmp_winget
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppHeader(
    isDarkMode: Boolean,
    onCleanDisk: () -> Unit
) {

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.kmp_winget),
                modifier = Modifier
                    .height(36.dp)
                    .width(36.dp),
                contentDescription = "App brand icon"
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = stringResource(Res.string.header_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) {
                        AppColors.headerWingetTextColorDark
                    } else {
                        AppColors.headerWingetTextColorLight
                    }
                )

                Text(
                    text = stringResource(Res.string.header_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "v" + BuildConfig.VERSION,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DynamicIconButton(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp),
            onClickAction = onCleanDisk,
            iconImage = Icons.TwoTone.CleaningServices,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colorScheme.onSecondary,
            contentDescription = "Open Disk Manager"
        )

        DynamicIconButton(
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(36.dp),
            onClickAction = {
                ThemeState.isDarkMode.value = !isDarkMode
            },
            iconImage = if (isDarkMode) Icons.TwoTone.WbSunny else Icons.TwoTone.ModeNight,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colorScheme.onBackground,
            contentDescription = "Switch theme"
        )
    }
}