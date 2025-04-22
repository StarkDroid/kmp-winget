package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.velocity.kmpwinget.model.domain.OperationResult
import com.velocity.kmpwinget.theme.AppColors
import kmp_winget.composeapp.generated.resources.Res
import kmp_winget.composeapp.generated.resources.dialog_loading
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoaderDialog(
    result: OperationResult? = null,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = {
        if (result !is OperationResult.Loading) onDismiss()
    }) {
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
            elevation = CardDefaults.elevatedCardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
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
                    val (icon, tint, needsAnimation) = when (result) {
                        is OperationResult.Success -> Triple(
                            Icons.Filled.CheckCircle,
                            AppColors.upgradeAvailableLight,
                            false
                        )

                        is OperationResult.Error -> Triple(
                            Icons.Filled.Error,
                            AppColors.deleteButton,
                            false
                        )

                        else -> Triple(
                            Icons.Filled.Refresh,
                            MaterialTheme.colorScheme.primaryContainer,
                            true
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .let {
                                if (needsAnimation) {
                                    it.rotate(rotation).alpha(scale)
                                } else {
                                    it
                                }
                            },
                        tint = tint
                    )

                    Text(
                        text = when (result) {
                            is OperationResult.Success -> result.message
                            is OperationResult.Error -> "Error"
                            else -> stringResource(Res.string.dialog_loading)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = when (result) {
                            is OperationResult.Error -> result.message
                            is OperationResult.Success -> "Operation completed successfully"
                            else -> stringResource(Res.string.dialog_loading)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.alpha(if (needsAnimation) scale else 1f)
                    )

                    if (result is OperationResult.Loading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            trackColor = ProgressIndicatorDefaults.linearTrackColor,
                            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                        )
                    }
                }
            }
        }
    }
}