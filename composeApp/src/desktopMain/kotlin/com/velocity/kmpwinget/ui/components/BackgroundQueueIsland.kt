package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.BackgroundQueueState
import com.velocity.kmpwinget.domain.model.TaskStatus
import com.velocity.kmpwinget.domain.model.UpdateTask
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun BackgroundQueueIsland(
    queueState: BackgroundQueueState,
    isDarkMode: Boolean,
    onToggleExpand: () -> Unit,
    onDismiss: () -> Unit
) {
    if (queueState.tasks.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "queueSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "queueRotation"
    )

    val activeTask = queueState.inProgressTask

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .width(if (queueState.isExpanded) 520.dp else 420.dp)
                .padding(16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = if (isDarkMode) Color.Black else Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDarkMode) AppColors.islandSurfaceDark else AppColors.islandSurfaceLight
                )
                .border(
                    1.dp,
                    if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight,
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (queueState.isRunning) {
                                    if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                                } else {
                                    if (isDarkMode) AppColors.upgradeBadgeBgDark else AppColors.upgradeBadgeBgLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (queueState.isRunning) Icons.TwoTone.Sync else Icons.TwoTone.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .let { if (queueState.isRunning) it.rotate(rotation) else it },
                            tint = if (queueState.isRunning) {
                                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            } else {
                                if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                            }
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (queueState.isRunning) {
                                "Updating ${queueState.completedCount + 1} of ${queueState.totalCount} in background"
                            } else {
                                "Background Updates Complete (${queueState.completedCount}/${queueState.totalCount})"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (activeTask != null) {
                            Text(
                                text = "Currently updating: ${activeTask.pkg.name}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    // Expand / Collapse Drawer Toggle
                    Box(
                        modifier = Modifier
                            .subtleIslandControl(shape = RoundedCornerShape(6.dp), isDarkMode = isDarkMode)
                            .clickable { onToggleExpand() }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = if (queueState.isExpanded) Icons.TwoTone.ExpandLess else Icons.TwoTone.ExpandMore,
                            contentDescription = "Expand logs",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Dismiss Button (when complete or user closes)
                    Box(
                        modifier = Modifier
                            .subtleIslandControl(shape = RoundedCornerShape(6.dp), isDarkMode = isDarkMode)
                            .clickable { onDismiss() }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Overall Queue Progress Bar
                LinearProgressIndicator(
                    progress = { queueState.overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                    trackColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000)
                )

                // Expanded Task List & Live Logs
                AnimatedVisibility(visible = queueState.isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Queued Applications",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(queueState.tasks, key = { it.pkg.uniqueId }) { task ->
                                QueueTaskRow(task = task, isDarkMode = isDarkMode)
                            }
                        }

                        // Active Task Live Log
                        if (activeTask != null && activeTask.logs.isNotBlank()) {
                            Text(
                                text = "Live Terminal Output",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0C0C0E))
                                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                val logLines = activeTask.logs.lines().filter { it.isNotBlank() }
                                val terminalListState = rememberLazyListState()

                                LaunchedEffect(logLines.size) {
                                    if (logLines.isNotEmpty()) {
                                        terminalListState.scrollToItem(logLines.size - 1)
                                    }
                                }

                                LazyColumn(state = terminalListState, modifier = Modifier.fillMaxSize()) {
                                    items(logLines) { line ->
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFFD4D4D4)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueTaskRow(task: UpdateTask, isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (statusIcon, iconTint) = when (task.status) {
            TaskStatus.QUEUED -> Pair(Icons.TwoTone.HourglassEmpty, MaterialTheme.colorScheme.onSurfaceVariant)
            TaskStatus.IN_PROGRESS -> Pair(
                Icons.TwoTone.Sync,
                if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
            )
            TaskStatus.COMPLETED -> Pair(
                Icons.TwoTone.CheckCircle,
                if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
            )
            TaskStatus.FAILED -> Pair(
                Icons.TwoTone.Error,
                if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
            )
        }

        Icon(
            imageVector = statusIcon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = iconTint
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = task.pkg.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Text(
            text = when (task.status) {
                TaskStatus.QUEUED -> "Queued"
                TaskStatus.IN_PROGRESS -> "Updating..."
                TaskStatus.COMPLETED -> "Done"
                TaskStatus.FAILED -> "Failed"
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        )
    }
}
