package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import model.Package
import model.PerformAction
import utils.bodyFont
import utils.performAction

@Composable
fun TableRowLayout(
    pkg: Package,
    scope: CoroutineScope,
    setLoading: (Boolean) -> Unit,
    refreshPackages: () -> Unit
) {
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
                        performAction(
                            scope = scope,
                            action = PerformAction.UpgradePackage(packageName = pkg.name),
                            setLoading = setLoading,
                            onPackagesLoaded = { /* No-op for upgrades */ },
                            onActionComplete = { success ->
                                if (success) {
                                    println("Package upgraded successfully")
                                    refreshPackages()
                                } else {
                                    println("Package upgrade failed")
                                }
                            }
                        )
                    },
                    iconImage = Icons.TwoTone.Download,
                    iconSize = 18.dp,
                    iconTint = MaterialTheme.colors.onBackground,
                    contentDescription = "Upgrade package"
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

            DynamicIconButton(
                backgroundColor = MaterialTheme.colors.onError,
                modifier = Modifier
                    .size(32.dp),
                onClickAction = {
                    performAction(
                        scope = scope,
                        action = PerformAction.UninstallPackage(packageName = pkg.name),
                        setLoading = setLoading,
                        onPackagesLoaded = { /* No-op for uninstalls */ },
                        onActionComplete = { success ->
                            if (success) {
                                println("Package uninstalled successfully")
                                refreshPackages()
                            } else {
                                println("Package uninstalled failed")
                            }
                        }
                    )
                },
                iconImage = Icons.TwoTone.Delete,
                iconSize = 18.dp,
                iconTint = MaterialTheme.colors.onBackground,
                contentDescription = "Uninstall package button"
            )
        }
    }
}