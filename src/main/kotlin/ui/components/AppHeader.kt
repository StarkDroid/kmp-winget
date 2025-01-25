package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CleaningServices
import androidx.compose.material.icons.twotone.ModeNight
import androidx.compose.material.icons.twotone.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.AppColors
import theme.ThemeState
import utils.executeCommand
import utils.headingFont
import utils.loadString

@Composable
fun AppHeader(
    isDarkMode: Boolean,
    isLoading: Boolean,
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
                painter = painterResource("drawables/kmp-winget.png"),
                modifier = Modifier
                    .height(36.dp)
                    .width(36.dp),
                contentDescription = "App brand icon"
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    loadString("app.header.subtitle"),
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.body1,
                    color = if (isDarkMode) {
                        AppColors.headerWingetTextColorDark
                    } else {
                        AppColors.headerWingetTextColorLight
                    }
                )

                Text(
                    loadString("app.header.title"),
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 21.sp,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DynamicIconButton(
            backgroundColor = MaterialTheme.colors.primaryVariant,
            modifier = Modifier.size(36.dp),
            onClickAction = {
                executeCommand("cleanmgr")
            },
            isEnabled = !isLoading,
            iconImage = Icons.TwoTone.CleaningServices,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colors.onSecondary,
            contentDescription = "Open Disk Manager"
        )

        DynamicIconButton(
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.size(36.dp),
            onClickAction = {
                ThemeState.isDarkMode.value = !isDarkMode
            },
            isEnabled = !isLoading,
            iconImage = if (isDarkMode) Icons.TwoTone.WbSunny else Icons.TwoTone.ModeNight,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colors.onBackground,
            contentDescription = "Switch theme"
        )
    }
}