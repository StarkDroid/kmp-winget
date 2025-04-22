package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun DynamicIconButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean = true,
    iconImage: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    buttonBackgroundSize: Dp = 32.dp,
    iconSize: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
    elevation: Dp = 4.dp,
    contentDescription: String = "Refresh button",
) {
    IconButton(
        onClick = onClickAction,
        enabled = isEnabled,
    ) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Icon(
                modifier = Modifier.size(iconSize).padding((buttonBackgroundSize - iconSize) / 2),
                imageVector = iconImage,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }
    }
}