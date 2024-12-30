import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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

                IconButton(
                    onClick = {
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
                    enabled = !isLoading,
                ) {
                    Card(
                        modifier = Modifier.size(32.dp),
                        elevation = 4.dp,
                        shape = CircleShape,
                        backgroundColor = MaterialTheme.colors.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp).padding(8.dp),
                            imageVector = Icons.TwoTone.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                }
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
                    strokeWidth = 6.dp
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
