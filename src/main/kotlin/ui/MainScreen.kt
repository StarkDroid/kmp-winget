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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.PerformAction
import theme.AppTheme
import theme.ThemeState
import ui.components.AppHeader
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
            onPackagesLoaded = { result ->
                packages = result
            },
            setLoading = { isLoading = it },
            action = PerformAction.RefreshList,
            showUpgradesOnly = showUpgradesOnly
        )
    }

    LaunchedEffect(Unit) {
        refreshPackages()
    }

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp)
            ) {

                AppHeader(
                    scope = scope,
                    isDarkMode = isDarkMode,
                    isLoading = isLoading,
                    onPackagesLoaded = { result ->
                        packages = result
                    },
                    setLoading = { isLoading = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        searchQuery = query
                    },
                    showUpgradesOnly = showUpgradesOnly,
                    onToggleChange = { isChecked ->
                        showUpgradesOnly = isChecked
                        refreshPackages()
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = loadString("table.column.name"),
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                        fontFamily = bodyFont,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = loadString("table.column.version"),
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

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            strokeCap = StrokeCap.Round,
                            strokeWidth = 6.dp,
                            color = MaterialTheme.colors.onSurface
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 8.dp)
                        ) {
                            items(filteredPackages) { pkg ->
                                TableRowLayout(
                                    pkg = pkg,
                                    scope = scope,
                                    setLoading = { isLoading = it },
                                    refreshPackages = refreshPackages
                                )
                            }
                        }

                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState = listState),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight(),
                            style = ScrollbarStyle(
                                shape = RoundedCornerShape(4.dp),
                                minimalHeight = 40.dp,
                                thickness = 8.dp,
                                unhoverColor = MaterialTheme.colors.onSurface.copy(0.5f),
                                hoverColor = MaterialTheme.colors.onSurface,
                                hoverDurationMillis = 150
                            )
                        )
                    }
                }
            }
        }
    }
}