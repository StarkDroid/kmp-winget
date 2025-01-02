package ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = {
            Text(text = "Search through the installed apps")
        },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.TwoTone.Search,
                contentDescription = "Search"
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background,
            focusedIndicatorColor = MaterialTheme.colors.onSurface,
            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            textColor = MaterialTheme.colors.onSurface
        )
    )
}