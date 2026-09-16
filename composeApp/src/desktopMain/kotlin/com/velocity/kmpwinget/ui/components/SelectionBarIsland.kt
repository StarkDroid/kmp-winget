package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.DeleteUnderglowButton
import com.velocity.kmpwinget.theme.UpdateUnderglowButton
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun SelectionBarIsland(
    isVisible: Boolean,
    selectedCount: Int,
    isAllSelected: Boolean,
    isDarkMode: Boolean,
    onSelectAllToggle: () -> Unit,
    onBatchUpgrade: () -> Unit,
    onBatchUninstall: () -> Unit,
    onCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = if (isDarkMode) Color.Black else Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDarkMode) AppColors.islandSurfaceDark else AppColors.islandSurfaceLight
                )
                .border(
                    1.dp,
                    if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Select All / Deselect Toggle
                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(6.dp), isDarkMode = isDarkMode)
                        .clickable { onSelectAllToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.TwoTone.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isAllSelected) "Deselect All" else "Select All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Batch Actions
                if (selectedCount > 0) {
                    // Update Selected
                    UpdateUnderglowButton(
                        text = "Update Selected ($selectedCount)",
                        onClick = onBatchUpgrade,
                        isDarkMode = isDarkMode
                    )

                    Spacer(Modifier.width(8.dp))

                    // Uninstall Selected
                    DeleteUnderglowButton(
                        text = "Uninstall Selected ($selectedCount)",
                        onClick = onBatchUninstall,
                        isDarkMode = isDarkMode
                    )

                    Spacer(Modifier.width(8.dp))
                }

                // Close Selection Mode
                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(6.dp), isDarkMode = isDarkMode)
                        .clickable { onCancel() }
                        .padding(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
