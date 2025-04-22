package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.model.domain.OperationResult
import com.velocity.kmpwinget.theme.AppColors
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.searchbar_placeholder
import kmp_winget.composeapp.generated.resources.switch_upgrades_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showUpgradesOnly: Boolean,
    onToggleUpgradesOnly: (Boolean) -> Unit,
    isLoading: OperationResult?,
    onRefreshPackages: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DynamicIconButton(
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(36.dp),
                onClickAction = onRefreshPackages,
                isEnabled = isLoading == null,
                iconImage = Icons.TwoTone.Refresh,
                iconSize = 18.dp,
                iconTint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Refresh packages"
            )

            Spacer(modifier = Modifier.width(10.dp))

            TextField(
                value = query,
                textStyle = TextStyle.Default.copy(fontSize = 14.sp),
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.searchbar_placeholder),
                        style = MaterialTheme.typography.bodySmall,
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
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Switch(
                    checked = showUpgradesOnly,
                    onCheckedChange = onToggleUpgradesOnly,
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppColors.switchCheckedTrackColor,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        checkedThumbColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    enabled = isLoading == null,
                )

                Text(
                    text = stringResource(Res.string.switch_upgrades_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}