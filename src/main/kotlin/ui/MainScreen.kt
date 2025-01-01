package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.ModeNight
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.WbSunny
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.PerformAction
import theme.AppTheme
import theme.ThemeState
import utils.bodyFont
import utils.headingFont
import utils.performAction

@Composable
@Preview
fun MainScren() {
    var packages by remember { mutableStateOf<List<model.Package>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val isDarkMode = ThemeState.isDarkMode.value

    performAction(
        scope = scope,
        onPackagesLoaded = { result ->
            packages = result
        },
        setLoading = { isLoading = it },
        action = PerformAction.RefreshList
    )

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.surface)
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Package Manager",
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
                        errorMessage = null
                        performAction(
                            scope = scope,
                            onPackagesLoaded = { result ->
                                packages = result
                            },
                            setLoading = { isLoading = it },
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

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colors.onSurface
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(packages) { pkg ->
                        PackageCard(pkg)
                    }
                }
            }
        }
    }
}

@Composable
fun PackageCard(pkg: model.Package) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.onBackground.copy(0.1f), shape = RoundedCornerShape(4.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    fontFamily = bodyFont,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "ID: ${pkg.id}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                    fontFamily = bodyFont,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!pkg.availableVersion.isNullOrBlank()) {
                DynamicIconButton(
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier
                        .size(32.dp),
                    onClickAction = {
                        println("Upgrade button clicked for ${pkg.name}")
                    },
                    iconImage = Icons.TwoTone.Download,
                    iconSize = 18.dp,
                    iconTint = MaterialTheme.colors.onBackground
                )
            }

            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = pkg.version,
                    style = MaterialTheme.typography.subtitle2,
                    textAlign = TextAlign.End,
                    fontFamily = bodyFont,
                    color = MaterialTheme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = pkg.availableVersion ?: "",
                    style = MaterialTheme.typography.subtitle2,
                    textAlign = TextAlign.End,
                    fontFamily = bodyFont,
                    color = Color.Green
                )
            }
        }
    }
}