package com.velocity.kmpwinget.data.datasource

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import javax.swing.filechooser.FileSystemView

object Win32IconLoader {

    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()
    private val fileSystemView: FileSystemView by lazy { FileSystemView.getFileSystemView() }

    /**
     * Loads Windows system icon for an executable or icon path with in-memory caching.
     */
    suspend fun loadIcon(iconPath: String?): ImageBitmap? = withContext(Dispatchers.IO) {
        if (iconPath.isNullOrBlank()) return@withContext null

        val cleanPath = iconPath.trim()
            .removeSurrounding("\"")
            .substringBefore(",") // remove ",0" index suffix
            .trim()

        if (cleanPath.isBlank()) return@withContext null

        iconCache[cleanPath]?.let { return@withContext it }

        try {
            val file = File(cleanPath)
            if (file.exists()) {
                val swingIcon: Icon? = fileSystemView.getSystemIcon(file)
                if (swingIcon != null && swingIcon.iconWidth > 0 && swingIcon.iconHeight > 0) {
                    val bufferedImage = BufferedImage(
                        swingIcon.iconWidth,
                        swingIcon.iconHeight,
                        BufferedImage.TYPE_INT_ARGB
                    )
                    val g2d: Graphics2D = bufferedImage.createGraphics()
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    swingIcon.paintIcon(null, g2d, 0, 0)
                    g2d.dispose()

                    val composeBitmap = bufferedImage.toComposeImageBitmap()
                    iconCache[cleanPath] = composeBitmap
                    return@withContext composeBitmap
                }
            }
        } catch (_: Throwable) {}

        null
    }
}
