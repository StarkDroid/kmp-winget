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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.velocity.kmpwinget.theme.AppColors
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
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedPackageIds by viewModel.selectedPackageIds.collectAsState()

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
                result = isLoading,
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
                                .padding(start = 8.dp)
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

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedButton(
                            modifier = Modifier.height(32.dp),
                            onClick = { viewModel.toggleMultiSelectMode() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                        ) {
                            Icon(
                                imageVector = if (isMultiSelectMode)
                                    Icons.TwoTone.Cancel
                                else
                                    Icons.TwoTone.SelectAll,
                                contentDescription = "Toggle select mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isMultiSelectMode) "Cancel" else "Select")
                        }


                        if (isMultiSelectMode && selectedPackageIds.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.upgradeSelectedPackages() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.Download,
                                    contentDescription = "Upgrade selected",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Update (${selectedPackageIds.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.uninstallSelectedPackages() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.deleteButton,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.Delete,
                                    contentDescription = "Uninstall selected",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Uninstall (${selectedPackageIds.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = stringResource(Res.string.table_column_version),
                            style = MaterialTheme.typography.bodyMedium,
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
                            items = filteredPackages,
                            key = { it.id }
                        ) { pkg ->
                            TableRowLayout(
                                pkg = pkg,
                                isSelected = viewModel.isPackageSelected(pkg.id),
                                isMultiSelectMode = isMultiSelectMode,
                                onSelect = { viewModel.togglePackageSelection(pkg.id) },
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