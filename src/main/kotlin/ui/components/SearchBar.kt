package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import utils.bodyFont

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showUpgradesOnly: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Box(
        modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier
                .fillMaxWidth(0.5f)
                .height(50.dp),
            placeholder = {
                Text(
                    text = "Search through the installed apps",
                    style = MaterialTheme.typography.caption,
                    fontFamily = bodyFont,
                    fontSize = 12.sp,
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.TwoTone.Search,
                    contentDescription = "Search"
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.onBackground.copy(0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                placeholderColor = MaterialTheme.colors.onSurface.copy(0.3f),
                textColor = MaterialTheme.colors.onSurface,
                cursorColor = MaterialTheme.colors.onSurface
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Row {
            Text(
                text = "Show upgrades only",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(end = 8.dp)
            )
            Switch(
                checked = showUpgradesOnly,
                onCheckedChange = onToggleChange,
            )
        }
    }
}