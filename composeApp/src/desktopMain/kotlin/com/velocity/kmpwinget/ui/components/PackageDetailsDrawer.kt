package com.velocity.kmpwinget.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velocity.kmpwinget.data.datasource.Win32IconLoader
import com.velocity.kmpwinget.domain.model.Package
import com.velocity.kmpwinget.domain.model.PackageDetails
import com.velocity.kmpwinget.domain.model.PackageSource
import com.velocity.kmpwinget.theme.AppColors
import com.velocity.kmpwinget.theme.DeleteUnderglowButton
import com.velocity.kmpwinget.theme.UpdateUnderglowButton
import com.velocity.kmpwinget.theme.islandContainer
import kotlinx.coroutines.delay
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

@Composable
fun PackageDetailsDrawer(
    pkg: Package?,
    details: PackageDetails?,
    isLoading: Boolean,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: (Package) -> Unit,
    onUninstall: (Package) -> Unit
) {
    val isVisible = pkg != null

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Backdrop Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isDarkMode) 0.55f else 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        // Slide-out Drawer Panel
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(280)
            ) + fadeIn(tween(280)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(220)
            ) + fadeOut(tween(220))
        ) {
            if (pkg != null) {
                DrawerContent(
                    pkg = pkg,
                    details = details,
                    isLoading = isLoading,
                    isDarkMode = isDarkMode,
                    onDismiss = onDismiss,
                    onUpgrade = onUpgrade,
                    onUninstall = onUninstall
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    pkg: Package,
    details: PackageDetails?,
    isLoading: Boolean,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: (Package) -> Unit,
    onUninstall: (Package) -> Unit
) {
    val scrollState = rememberScrollState()

    var appIcon by remember(pkg.iconPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pkg.iconPath) {
        if (!pkg.iconPath.isNullOrBlank()) {
            appIcon = Win32IconLoader.loadIcon(pkg.iconPath)
        }
    }

    val displayPkg = details ?: PackageDetails(
        id = pkg.targetUpgradeId.ifBlank { pkg.id },
        name = pkg.name,
        version = pkg.availableVersion ?: pkg.version,
        installedVersion = pkg.version.ifBlank { null },
        availableVersion = pkg.availableVersion,
        hasUpdate = pkg.hasUpdate,
        publisher = pkg.publisher,
        description = pkg.description,
        iconPath = pkg.iconPath,
        source = pkg.source
    )

    val drawerBg = if (isDarkMode) AppColors.islandSurfaceDark else AppColors.islandSurfaceLight
    val strokeColor = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 400.dp, max = 480.dp)
            .fillMaxWidth(0.38f)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .background(drawerBg)
            .border(
                1.dp,
                strokeColor,
                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume clicks to prevent dismissing scrim */ }
    ) {
        // Drawer Header
        DrawerHeader(
            pkg = pkg,
            details = displayPkg,
            appIcon = appIcon,
            isDarkMode = isDarkMode,
            onDismiss = onDismiss
        )

        // Loading Progress Bar
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.5.dp),
                color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight,
                trackColor = if (isDarkMode) AppColors.primaryContainerDark else AppColors.primaryContainerLight
            )
        } else {
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
            )
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Version & Update Status Card
            VersionStatusCard(
                pkg = pkg,
                details = displayPkg,
                isDarkMode = isDarkMode
            )

            // Overview & Description
            OverviewSection(
                details = displayPkg,
                isDarkMode = isDarkMode
            )

            // Security & Integrity
            SecurityIntegritySection(
                details = displayPkg,
                isDarkMode = isDarkMode
            )

            // Release Notes & Changelog (if available)
            if (!displayPkg.releaseNotes.isNullOrBlank() || !displayPkg.releaseNotesUrl.isNullOrBlank()) {
                ReleaseNotesSection(
                    details = displayPkg,
                    isDarkMode = isDarkMode
                )
            }

            // Publisher & License Links
            PublisherLicenseSection(
                details = displayPkg,
                isDarkMode = isDarkMode
            )

            // Tags
            if (displayPkg.tags.isNotEmpty()) {
                TagsSection(
                    tags = displayPkg.tags,
                    isDarkMode = isDarkMode
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = if (isDarkMode) AppColors.islandStrokeDark else AppColors.islandStrokeLight
        )

        // Fixed Bottom Actions
        DrawerActionsFooter(
            pkg = pkg,
            details = displayPkg,
            isDarkMode = isDarkMode,
            onUpgrade = onUpgrade,
            onUninstall = onUninstall
        )
    }
}

@Composable
private fun DrawerHeader(
    pkg: Package,
    details: PackageDetails,
    appIcon: ImageBitmap?,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val initial = (details.name.firstOrNull() ?: pkg.name.firstOrNull() ?: 'P').uppercaseChar().toString()
    val avatarGradient = getAvatarGradient(details.name)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (appIcon != null) Modifier.background(Color.Transparent) else Modifier.background(avatarGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = details.name,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Title & Publisher
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val publisherName = details.publisher ?: pkg.publisher
                    if (!publisherName.isNullOrBlank()) {
                        Text(
                            text = publisherName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DrawerSourceBadge(source = details.source, isDarkMode = isDarkMode)
                }
            }

            // Close Button
            CloseIconButton(onClick = onDismiss, isDarkMode = isDarkMode)
        }

        // Package ID Pill with Copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDarkMode) Color(0x14FFFFFF) else Color(0x0A000000))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = details.id,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            CopyIconButton(
                textToCopy = details.id,
                isDarkMode = isDarkMode,
                tooltip = "Copy ID"
            )
        }
    }
}

@Composable
private fun VersionStatusCard(
    pkg: Package,
    details: PackageDetails,
    isDarkMode: Boolean
) {
    val hasUpdate = details.hasUpdate || pkg.hasUpdate
    val curVer = details.installedVersion ?: pkg.version
    val availVer = details.availableVersion ?: pkg.availableVersion

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (hasUpdate) {
                    if (isDarkMode) AppColors.upgradeBadgeBgDark.copy(alpha = 0.6f) else AppColors.upgradeBadgeBgLight
                } else {
                    if (isDarkMode) Color(0x12FFFFFF) else Color(0x08000000)
                }
            )
            .border(
                1.dp,
                if (hasUpdate) {
                    if (isDarkMode) AppColors.upgradeAvailableDark.copy(alpha = 0.35f) else AppColors.upgradeAvailableLight.copy(alpha = 0.35f)
                } else {
                    if (isDarkMode) Color(0x1AFFFFFF) else Color(0x10000000)
                },
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasUpdate) "Update Available" else "Up to Date",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (hasUpdate) {
                            if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                )

                if (hasUpdate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEW VERSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                color = if (isDarkMode) Color.Black else Color.White
                            )
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Up to date",
                        modifier = Modifier.size(16.dp),
                        tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                    )
                }
            }

            if (hasUpdate) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (curVer.isNotBlank()) "v$curVer" else "Installed",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.TwoTone.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                    )

                    Text(
                        text = if (!availVer.isNullOrBlank()) "v$availVer" else "Latest",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
                        )
                    )
                }
            } else {
                Text(
                    text = if (curVer.isNotBlank()) "Installed Version: v$curVer" else "Current build installed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (!details.releaseDate.isNullOrBlank()) {
                Text(
                    text = "Released: ${details.releaseDate}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(
    details: PackageDetails,
    isDarkMode: Boolean
) {
    SectionContainer(title = "Overview", icon = Icons.TwoTone.Info, isDarkMode = isDarkMode) {
        val desc = details.description?.trim()
        if (!desc.isNullOrBlank()) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        } else {
            Text(
                text = "No detailed description provided by the package maintainer.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun SecurityIntegritySection(
    details: PackageDetails,
    isDarkMode: Boolean
) {
    SectionContainer(
        title = "Security & Integrity",
        icon = Icons.TwoTone.Shield,
        isDarkMode = isDarkMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // SHA-256 Hash
            if (!details.installerSha256.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SHA-256 Checksum",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkMode) Color(0x18FFFFFF) else Color(0x0C000000))
                            .border(1.dp, if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = details.installerSha256,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        CopyIconButton(
                            textToCopy = details.installerSha256,
                            isDarkMode = isDarkMode,
                            tooltip = "Copy SHA-256"
                        )
                    }
                }
            }

            // Specs Row: Installer Type & Architecture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecPill(
                    label = "Installer Type",
                    value = details.installerType?.uppercase() ?: "Standard",
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )

                SpecPill(
                    label = "Architecture",
                    value = details.architecture?.uppercase() ?: "x64",
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }

            // Direct Installer URL (if available)
            if (!details.installerUrl.isNullOrBlank()) {
                LinkButton(
                    text = "Direct Installer Download",
                    icon = Icons.TwoTone.Download,
                    onClick = { openBrowser(details.installerUrl) },
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun ReleaseNotesSection(
    details: PackageDetails,
    isDarkMode: Boolean
) {
    SectionContainer(
        title = "Release Notes & Changelog",
        icon = Icons.TwoTone.Description,
        isDarkMode = isDarkMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!details.releaseNotes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0x18000000) else Color(0x0A000000))
                        .border(1.dp, if (isDarkMode) Color(0x1AFFFFFF) else Color(0x12000000), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = details.releaseNotes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            if (!details.releaseNotesUrl.isNullOrBlank()) {
                LinkButton(
                    text = "View Online Changelog",
                    icon = Icons.AutoMirrored.TwoTone.OpenInNew,
                    onClick = { openBrowser(details.releaseNotesUrl) },
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun PublisherLicenseSection(
    details: PackageDetails,
    isDarkMode: Boolean
) {
    SectionContainer(
        title = "Publisher & License",
        icon = Icons.TwoTone.Business,
        isDarkMode = isDarkMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Homepage Link
            if (!details.homepage.isNullOrBlank()) {
                LinkRow(
                    label = "Official Homepage",
                    url = details.homepage,
                    onClick = { openBrowser(details.homepage) },
                    isDarkMode = isDarkMode
                )
            }

            // Publisher Support Link
            if (!details.publisherSupportUrl.isNullOrBlank()) {
                LinkRow(
                    label = "Publisher Support",
                    url = details.publisherSupportUrl,
                    onClick = { openBrowser(details.publisherSupportUrl) },
                    isDarkMode = isDarkMode
                )
            }

            // Publisher URL
            if (!details.publisherUrl.isNullOrBlank() && details.publisherUrl != details.homepage) {
                LinkRow(
                    label = "Publisher Website",
                    url = details.publisherUrl,
                    onClick = { openBrowser(details.publisherUrl) },
                    isDarkMode = isDarkMode
                )
            }

            // License
            if (!details.license.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "License",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = details.license,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (!details.licenseUrl.isNullOrBlank()) {
                        IconButton(
                            onClick = { openBrowser(details.licenseUrl) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.OpenInNew,
                                contentDescription = "Open License",
                                modifier = Modifier.size(15.dp),
                                tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                            )
                        }
                    }
                }
            }

            // Copyright
            if (!details.copyright.isNullOrBlank()) {
                Text(
                    text = details.copyright,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
    isDarkMode: Boolean
) {
    SectionContainer(
        title = "Tags & Categories",
        icon = Icons.TwoTone.LocalOffer,
        isDarkMode = isDarkMode
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tags.take(6).forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDarkMode) Color(0x1AFFFFFF) else Color(0x0C000000))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerActionsFooter(
    pkg: Package,
    details: PackageDetails,
    isDarkMode: Boolean,
    onUpgrade: (Package) -> Unit,
    onUninstall: (Package) -> Unit
) {
    val hasUpdate = details.hasUpdate || pkg.hasUpdate

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasUpdate) {
            UpdateUnderglowButton(
                text = "Update Package",
                onClick = { onUpgrade(pkg) },
                isDarkMode = isDarkMode,
                modifier = Modifier.weight(1f)
            )
        }

        DeleteUnderglowButton(
            text = "Uninstall",
            onClick = { onUninstall(pkg) },
            isDarkMode = isDarkMode,
            modifier = if (hasUpdate) Modifier else Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionContainer(
    title: String,
    icon: ImageVector,
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0x0EFFFFFF) else Color(0x06000000))
            .border(1.dp, if (isDarkMode) Color(0x18FFFFFF) else Color(0x0E000000), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        content()
    }
}

@Composable
private fun SpecPill(
    label: String,
    value: String,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkMode) Color(0x18FFFFFF) else Color(0x0C000000))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun LinkButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = if (isDarkMode) {
        if (isHovered) Color(0x289D8CFF) else Color(0x159D8CFF)
    } else {
        if (isHovered) Color(0x1E5B4DFF) else Color(0x0E5B4DFF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                1.dp,
                if (isDarkMode) Color(0x339D8CFF) else Color(0x225B4DFF),
                RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.OpenInNew,
            contentDescription = "Open",
            modifier = Modifier.size(13.dp),
            tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
        )
    }
}

@Composable
private fun LinkRow(
    label: String,
    url: String,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.OpenInNew,
            contentDescription = "Open",
            modifier = Modifier.size(14.dp),
            tint = if (isDarkMode) AppColors.primaryDark else AppColors.primaryLight
        )
    }
}

@Composable
private fun CopyIconButton(
    textToCopy: String,
    isDarkMode: Boolean,
    tooltip: String
) {
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    IconButton(
        onClick = {
            copyToClipboard(textToCopy)
            copied = true
        },
        modifier = Modifier.size(24.dp)
    ) {
        if (copied) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Copied",
                modifier = Modifier.size(13.dp),
                tint = if (isDarkMode) AppColors.upgradeAvailableDark else AppColors.upgradeAvailableLight
            )
        } else {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = tooltip,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CloseIconButton(
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = if (isDarkMode) {
        if (isHovered) Color(0x33FFFFFF) else Color(0x14FFFFFF)
    } else {
        if (isHovered) Color(0x1A000000) else Color(0x0A000000)
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DrawerSourceBadge(source: PackageSource, isDarkMode: Boolean) {
    val (bg, textColor) = when (source) {
        PackageSource.WINGET -> Pair(
            if (isDarkMode) AppColors.badgeWingetBgDark else AppColors.badgeWingetBgLight,
            if (isDarkMode) AppColors.badgeWingetDark else AppColors.badgeWingetLight
        )
        PackageSource.MSSTORE -> Pair(
            if (isDarkMode) AppColors.badgeStoreBgDark else AppColors.badgeStoreBgLight,
            if (isDarkMode) AppColors.badgeStoreDark else AppColors.badgeStoreLight
        )
        PackageSource.LOCAL, PackageSource.UNKNOWN -> Pair(
            if (isDarkMode) AppColors.badgeLocalBgDark else AppColors.badgeLocalBgLight,
            if (isDarkMode) AppColors.badgeLocalDark else AppColors.badgeLocalLight
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    ) {
        Text(
            text = source.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

private fun openBrowser(url: String) {
    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    } catch (_: Throwable) {}
}

private fun copyToClipboard(text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Throwable) {}
}

private fun getAvatarGradient(name: String): Brush {
    val hash = kotlin.math.abs(name.hashCode())
    val palettes = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
        listOf(Color(0xFF10B981), Color(0xFF047857)),
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        listOf(Color(0xFFF97316), Color(0xFFC2410C)),
        listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
        listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
    )
    val chosen = palettes[hash % palettes.size]
    return Brush.linearGradient(chosen)
}
