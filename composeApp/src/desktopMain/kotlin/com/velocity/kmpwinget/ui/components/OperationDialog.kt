package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.velocity.kmpwinget.domain.model.OperationResult
import com.velocity.kmpwinget.theme.AppColors

@Composable
fun OperationDialog(
    result: OperationResult,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    if (result is OperationResult.Idle) return

    var isLogExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        if (result !is OperationResult.Loading) {
            onDismiss()
        }
    }) {
        val infiniteTransition = rememberInfiniteTransition(label = "dialogSpin")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dialogSpinRot"
        )

        Card(
            modifier = Modifier
                .width(if (isLogExpanded) 560.dp else 440.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) AppColors.islandSurfaceDark else MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Status Icon
                val (icon, tint, isRotating) = when (result) {
                    is OperationResult.Loading -> Triple(
                        Icons.TwoTone.Sync,
                        if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                        true
                    )
                    is OperationResult.Success -> Triple(
                        Icons.TwoTone.CheckCircle,
                        if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight,
                        false
                    )
                    is OperationResult.Error -> Triple(
                        Icons.TwoTone.Error,
                        if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight,
                        false
                    )
                    else -> Triple(Icons.TwoTone.Info, MaterialTheme.colorScheme.onSurface, false)
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = if (isDarkMode) 0.18f else 0.12f))
                        .border(1.dp, tint.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .let { if (isRotating) it.rotate(rotation) else it },
                        tint = tint
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                val title = when (result) {
                    is OperationResult.Loading -> result.title
                    is OperationResult.Success -> "Operation Complete"
                    is OperationResult.Error -> "Operation Failed"
                    else -> ""
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Message / Subtitle
                val message = when (result) {
                    is OperationResult.Loading -> result.message
                    is OperationResult.Success -> result.message
                    is OperationResult.Error -> result.message
                    else -> ""
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 3
                )

                // Progress Indicator for Loading
                if (result is OperationResult.Loading) {
                    Spacer(Modifier.height(18.dp))

                    if (result.currentProgress != null && result.currentProgress > 0f) {
                        LinearProgressIndicator(
                            progress = { result.currentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                            trackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                            trackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
                        )
                    }

                    if (result.totalSteps > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Step ${result.step} of ${result.totalSteps}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // Log Output viewer section
                val logText = when (result) {
                    is OperationResult.Loading -> result.logOutput
                    is OperationResult.Success -> result.logOutput
                    is OperationResult.Error -> result.logOutput
                    else -> ""
                }

                if (logText.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLogExpanded = !isLogExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isLogExpanded) Icons.TwoTone.ExpandLess else Icons.TwoTone.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isLogExpanded) "Hide Terminal Output" else "Show Terminal Output",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    AnimatedVisibility(visible = isLogExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C0C0E))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            val listState = rememberLazyListState()
                            val logLines = logText.lines().filter { it.isNotBlank() }

                            LaunchedEffect(logLines.size) {
                                if (logLines.isNotEmpty()) {
                                    listState.scrollToItem(logLines.size - 1)
                                }
                            }

                            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                items(logLines.size) { index ->
                                    Text(
                                        text = logLines[index],
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = Color(0xFFD4D4D4)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                if (result !is OperationResult.Loading) {
                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
