package com.velocity.kmpwinget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.velocity.kmpwinget.model.domain.Package
import com.velocity.kmpwinget.theme.AppColors

@Composable
fun TableRowLayout(
    pkg: Package,
    onUpgrade: () -> Unit,
    onUninstall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onBackground.copy(0.1f),
                    shape = RoundedCornerShape(4.dp)
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "ID: ${pkg.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (pkg.hasUpdate) {
                DynamicIconButton(
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(32.dp),
                    onClickAction = onUpgrade,
                    iconImage = Icons.TwoTone.Download,
                    iconSize = 18.dp,
                    iconTint = MaterialTheme.colorScheme.onSecondary,
                    contentDescription = "Upgrade package"
                )
            }

            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = pkg.version,
                    style = MaterialTheme.typography.bodyMedium.merge(
                        TextStyle(
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = pkg.availableVersion ?: "",
                    style = MaterialTheme.typography.bodyMedium.merge(
                        TextStyle(
                            textAlign = TextAlign.End,
                            color = if (isSystemInDarkTheme().not())
                                AppColors.upgradeAvailableLight
                            else AppColors.upgradeAvailableDark
                        )
                    )
                )
            }

            DynamicIconButton(
                backgroundColor = AppColors.deleteButton,
                modifier = Modifier
                    .size(32.dp),
                onClickAction = onUninstall,
                iconImage = Icons.TwoTone.Delete,
                iconSize = 18.dp,
                iconTint = Color.Black,
                contentDescription = "Uninstall package button"
            )
        }
    }
}