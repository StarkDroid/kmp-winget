package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun ConfirmUninstallDialog(
    pkg: Package?,
    isBatch: Boolean,
    batchCount: Int = 0,
    isDarkMode: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isBatch && pkg == null) return
    if (isBatch && batchCount <= 0) return

    val title = if (isBatch) "Uninstall $batchCount Packages?" else "Uninstall ${pkg?.name}?"
    val message = if (isBatch) {
        "This will permanently uninstall the $batchCount selected applications and their components from your Windows system."
    } else {
        "Are you sure you want to uninstall '${pkg?.name}' (v${pkg?.version})? This will run the Windows Package uninstaller in silent mode."
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) AppColors.islandSurfaceDark else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
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
                // Danger Warning Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) AppColors.dangerBadgeBgDark else AppColors.dangerBadgeBgLight
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) AppColors.dangerRedDark.copy(alpha = 0.5f) else AppColors.dangerRedLight.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Delete,
                        contentDescription = "Warning",
                        modifier = Modifier.size(26.dp),
                        tint = if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Confirm Uninstall Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDarkMode) AppColors.dangerRedDark else AppColors.dangerRedLight
                            )
                            .clickable { onConfirm() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Uninstall",
                            style = MaterialTheme.typography.labelMedium.copy(
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
