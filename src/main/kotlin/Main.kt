import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Package Manager",
                style = MaterialTheme.typography.h4
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
                    isLoading = true
                    errorMessage = null
                    refreshPackages(scope, powerShell) { result ->
                        isLoading = false
                        packages = result
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Package List")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = pkg.name, style = MaterialTheme.typography.h6)
                Text(text = "Version: ${pkg.version}", style = MaterialTheme.typography.subtitle2)
                Text(text = "ID: ${pkg.id}", style = MaterialTheme.typography.body2)
            }
        }
    }
}

private fun refreshPackages(
    scope: CoroutineScope,
    powerShell: PowerShellCommand,
    onPackagesLoaded: (List<model.Package>) -> Unit
) {
    scope.launch {
        try {
            val packages = withContext(Dispatchers.IO) {
                powerShell.listInstalledPackages()
            }
            onPackagesLoaded(packages)
        } catch (e: Exception) {
            println("Error loading packages: ${e.message}")
            onPackagesLoaded(emptyList())
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Package Manager") {
        App()
    }
}
