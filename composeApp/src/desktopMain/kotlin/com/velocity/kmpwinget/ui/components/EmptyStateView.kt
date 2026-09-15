package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.domain.model.NavigationTab
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.subtleIslandControl

@Composable
fun EmptyStateView(
    activeTab: NavigationTab,
    searchQuery: String,
    isDarkMode: Boolean,
    onClearSearch: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (searchQuery.isNotEmpty()) {
                // No search results
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) Color(0x22FFFFFF) else Color(0x0F000000)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "No packages found",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "No installed applications matched '$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable { onClearSearch() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.TwoTone.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Clear Search",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            } else if (activeTab == NavigationTab.UPGRADES_AVAILABLE) {
                // All apps up to date
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) AppColors.upgradeBadgeBgDark else AppColors.upgradeBadgeBgLight
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) AppColors.upgradeAvailableDark.copy(alpha = 0.4f) else AppColors.upgradeAvailableLight.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "All Apps Are Up to Date!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "There are currently no pending updates detected for your installed packages.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable { onRefresh() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.TwoTone.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Scan for Updates",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "No packages available",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .subtleIslandControl(shape = RoundedCornerShape(8.dp), isDarkMode = isDarkMode)
                        .clickable { onRefresh() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.TwoTone.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
