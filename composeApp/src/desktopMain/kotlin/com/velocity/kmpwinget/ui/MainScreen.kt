package com.velocity.kmpwinget.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.velocity.kmpwinget.theme.AppTheme
import com.velocity.kmpwinget.theme.ThemeState
import com.velocity.kmpwinget.ui.components.AppHeader
import com.velocity.kmpwinget.ui.components.LoaderDialog
import com.velocity.kmpwinget.ui.components.SearchBar
import com.velocity.kmpwinget.ui.components.TableRowLayout
import com.velocity.kmpwinget.viewmodel.MainViewModel
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.table_column_name
import kmp_winget.composeapp.generated.resources.table_column_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
@Preview
fun MainScreen() {
    val viewModel = koinInject<MainViewModel>()

    val packages by viewModel.packages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showUpgradesOnly by viewModel.showUpgradesOnly.collectAsState()

    val listState = rememberLazyListState()
    val isDarkMode = ThemeState.isDarkMode.value

    val filteredPackages = packages.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
    }

    val refreshPackages = {
        viewModel.loadPackages()
    }

    AppTheme {
        if (isLoading != null) {
            LoaderDialog(
                onDismiss = { viewModel.clearOperationResult() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                AppHeader(
                    isDarkMode = isDarkMode,
                    onCleanDisk = { viewModel.cleanDisk() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchPackages(it) },
                    showUpgradesOnly = showUpgradesOnly,
                    onToggleUpgradesOnly = {
                        viewModel.toggleUpgradesOnly(it)
                    },
                    isLoading = isLoading,
                    onRefreshPackages = refreshPackages
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(Res.string.table_column_name),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = "${filteredPackages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = stringResource(Res.string.table_column_version),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(end = 12.dp)
                    ) {
                        items(
                            items = filteredPackages
                        ) { pkg ->
                            TableRowLayout(
                                pkg = pkg,
                                onUpgrade = { viewModel.upgradePackage(pkg.id) },
                                onUninstall = { viewModel.uninstallPackage(pkg.id) }
                            )
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState = listState),
                        style = ScrollbarStyle(
                            shape = RoundedCornerShape(4.dp),
                            minimalHeight = 40.dp,
                            thickness = 8.dp,
                            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                            hoverColor = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            hoverDurationMillis = 150
                        )
                    )
                }
            }
        }
    }
}