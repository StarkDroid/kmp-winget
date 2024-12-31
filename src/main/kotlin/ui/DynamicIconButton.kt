package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun DynamicIconButton(
    onClickAction: () -> Unit,
    isEnabled: Boolean = true,
    iconImage: ImageVector,
    iconTint: Color = MaterialTheme.colors.onBackground,
    buttonBackgroundSize: Dp = 32.dp,
    iconSize: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.colors.surface,
    borderColor: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
    elevation: Dp = 4.dp,
    contentDescription: String = "Refresh button"
) {
    IconButton(
        onClick = onClickAction,
        enabled = isEnabled,
    ) {
        Card(
            modifier = Modifier.size(buttonBackgroundSize),
            elevation = elevation,
            shape = CircleShape,
            backgroundColor = backgroundColor,
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