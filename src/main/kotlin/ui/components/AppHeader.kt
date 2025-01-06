package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import model.Package
import model.PerformAction
import theme.AppColors
import theme.ThemeState
import utils.executeCommand
import utils.headingFont
import utils.loadString
import utils.performAction

@Composable
fun AppHeader(
    isDarkMode: Boolean,
    isLoading: Boolean,
    setLoading: (Boolean) -> Unit,
    scope: CoroutineScope,
    onPackagesLoaded: (List<Package>) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            loadString("app.name"),
            fontFamily = headingFont,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            fontSize = 21.sp,
        )

        Spacer(modifier = Modifier.width(10.dp))

        DynamicIconButton(
            backgroundColor = MaterialTheme.colors.background,
            modifier = Modifier.size(36.dp),
            onClickAction = {
                performAction(
                    scope = scope,
                    onPackagesLoaded = onPackagesLoaded,
                    setLoading = setLoading,
                    action = PerformAction.RefreshList
                )
            },
            isEnabled = !isLoading,
            iconImage = Icons.TwoTone.Refresh,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colors.onBackground,
            contentDescription = "Refresh packages"
        )

        Spacer(modifier = Modifier.weight(1f))

        DynamicIconButton(
            backgroundColor = AppColors.diskManagerBackgroundColor,
            modifier = Modifier.size(36.dp),
            onClickAction = {
                executeCommand("cleanmgr")
            },
            isEnabled = !isLoading,
            iconImage = Icons.TwoTone.CleaningServices,
            iconSize = 18.dp,
            iconTint = MaterialTheme.colors.onBackground,
            contentDescription = "Open Disk Manager"
        )

        DynamicIconButton(
            backgroundColor = MaterialTheme.colors.background,
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