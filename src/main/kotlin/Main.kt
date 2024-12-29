import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.PowerShellCommand

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
            Text(
                "Package Manager",
                style = MaterialTheme.typography.h3
            )

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Package List")
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
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Version: ${pkg.version}",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Text(
                text = "ID: ${pkg.id}",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
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
    Window(onCloseRequest = ::exitApplication, title = "Package Manager") {
        App()
    }
}
