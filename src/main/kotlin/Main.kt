import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.DynamicIconButton
import utils.PowerShellCommand
import utils.bodyFont
import utils.headingFont

@Composable
@Preview
fun App() {
    var packages by remember { mutableStateOf<List<model.Package>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val powerShell = remember { PowerShellCommand() }

    refreshPackages(
        scope = scope,
        powerShell = powerShell,
        onPackagesLoaded = { result ->
            packages = result
        },
        setLoading = { isLoading = it }
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Package Manager",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.h6,
                    fontSize = 21.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                DynamicIconButton(
                    modifier = Modifier.size(36.dp),
                    onClickAction = {
                        errorMessage = null
                        refreshPackages(
                            scope = scope,
                            powerShell = powerShell,
                            onPackagesLoaded = { result ->
                                packages = result
                            },
                            setLoading = { isLoading = it }
                        )
                    },
                    isEnabled = !isLoading,
                    iconImage = Icons.TwoTone.Refresh,
                    iconSize = 18.dp
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
                        modifier = Modifier.weight(1f),
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider(
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
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
                    .background(Color.LightGray.copy(0.2f), shape = RoundedCornerShape(4.dp)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = pkg.name,
                        style = MaterialTheme.typography.body2,
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "ID: ${pkg.id}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                        fontFamily = bodyFont
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!pkg.availableVersion.isNullOrBlank()) {
                    DynamicIconButton(
                        modifier = Modifier
                            .size(32.dp),
                        onClickAction = {
                            println("Upgrade button clicked for ${pkg.name}")
                        },
                        iconImage = Icons.TwoTone.Download,
                        iconSize = 18.dp
                    )
                }

                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        text = pkg.version,
                        style = MaterialTheme.typography.subtitle2,
                        textAlign = TextAlign.End,
                        fontFamily = bodyFont
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

private fun refreshPackages(
    scope: CoroutineScope,
    powerShell: PowerShellCommand,
    onPackagesLoaded: (List<model.Package>) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    scope.launch {
        try {
            setLoading(true)
            val packages = withContext(Dispatchers.IO) {
                powerShell.listInstalledPackages()
            }.map { pkg ->
                pkg.copy(
                    version = pkg.version.takeIf { it != "Unknown" && it != "winget" } ?: "",
                    availableVersion = pkg.availableVersion
                        ?.replace(Regex("\\s*winget\\b", RegexOption.IGNORE_CASE), "")
                        ?.trim()
                        ?: ""
                )
            }
            onPackagesLoaded(packages)
        } catch (e: Exception) {
            println("Error loading packages: ${e.message}")
            onPackagesLoaded(emptyList())
        } finally {
            setLoading(false)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Package Manager", resizable = false) {
        App()
    }
}
