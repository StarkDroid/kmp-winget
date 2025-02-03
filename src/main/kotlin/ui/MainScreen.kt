package ui

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
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.PerformAction
import theme.AppTheme
import theme.ThemeState
import ui.components.AppHeader
import ui.components.LoaderDialog
import ui.components.SearchBar
import ui.components.TableRowLayout
import utils.bodyFont
import utils.loadString
import utils.performAction

@Composable
@Preview
fun MainScreen() {
    var showUpgradesOnly by remember { mutableStateOf(false) }
    var packages by remember { mutableStateOf<List<model.Package>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isDarkMode = ThemeState.isDarkMode.value

    val filteredPackages = packages.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
    }

    val refreshPackages = {
        performAction(
            scope = scope,
            onPackagesLoaded = { result -> packages = result },
            setLoading = { isLoading = it },
            action = PerformAction.RefreshList,
            showUpgradesOnly = showUpgradesOnly
        )
    }

    LaunchedEffect(Unit) {
        refreshPackages()
    }

    AppTheme {
        if (isLoading) {
            LoaderDialog()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                AppHeader(
                    isDarkMode = isDarkMode,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    scope = scope,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    showUpgradesOnly = showUpgradesOnly,
                    onToggleChange = {
                        showUpgradesOnly = it
                        refreshPackages()
                    },
                    onPackagesLoaded = { packages = it },
                    setLoading = { isLoading = it },
                    isLoading = isLoading
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
                            text = loadString("table.column.name"),
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface,
                            fontFamily = bodyFont,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp)
                                .background(
                                    color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = "${filteredPackages.size}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface,
                                fontFamily = bodyFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = loadString("table.column.version"),
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface,
                            fontFamily = bodyFont,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
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
                                scope = scope,
                                setLoading = { isLoading = it },
                                refreshPackages = refreshPackages
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
                            unhoverColor = MaterialTheme.colors.onSurface.copy(0.3f),
                            hoverColor = MaterialTheme.colors.onSurface.copy(0.7f),
                            hoverDurationMillis = 150
                        )
                    )
                }
            }
        }
    }
}