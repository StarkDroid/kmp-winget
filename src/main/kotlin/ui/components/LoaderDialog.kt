package ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import utils.bodyFont
import utils.loadString

@Composable
fun LoaderDialog(
    message: String = loadString("dialog.loading"),
    secondaryMessage: String = loadString("dialog.loading.message")
) {
    Dialog(onDismissRequest = {}) {
        val infiniteTransition = rememberInfiniteTransition()

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Card(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            elevation = 12.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colors.background,
                                MaterialTheme.colors.background
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(rotation)
                            .alpha(scale),
                        tint = MaterialTheme.colors.primaryVariant
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.h6,
                        fontFamily = bodyFont,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface
                    )

                    Text(
                        text = secondaryMessage,
                        style = MaterialTheme.typography.caption,
                        fontFamily = bodyFont,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.alpha(scale)
                    )

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colors.primaryVariant,
                        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
