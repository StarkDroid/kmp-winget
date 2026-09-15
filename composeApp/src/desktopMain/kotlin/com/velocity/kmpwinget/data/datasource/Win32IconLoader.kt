package com.velocity.kmpwinget.data.datasource

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.MultiResolutionImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.swing.filechooser.FileSystemView

object Win32IconLoader {

    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()
    private val fileSystemView: FileSystemView by lazy { FileSystemView.getFileSystemView() }

    /**
     * Extracts ultra-crisp, high-DPI (64x64 / 128x128) Windows system icons with in-memory caching.
     */
    suspend fun loadIcon(iconPath: String?): ImageBitmap? = withContext(Dispatchers.IO) {
        if (iconPath.isNullOrBlank()) return@withContext null

        val cleanPath = iconPath.trim()
            .removeSurrounding("\"")
            .substringBefore(",")
            .trim()

        if (cleanPath.isBlank()) return@withContext null

        iconCache[cleanPath]?.let { return@withContext it }

        try {
            val file = File(cleanPath)
            if (file.exists()) {
                // 1. High-DPI MultiResolution ShellFolder extraction
                try {
                    val shellFolderClass = Class.forName("sun.awt.shell.ShellFolder")
                    val getShellFolderMethod = shellFolderClass.getMethod("getShellFolder", File::class.java)
                    val shellFolder = getShellFolderMethod.invoke(null, file)
                    val getIconMethod = shellFolderClass.getMethod("getIcon", Boolean::class.javaPrimitiveType)
                    val rawIcon = getIconMethod.invoke(shellFolder, true) as? Image

                    if (rawIcon != null) {
                        // Extract high-DPI resolution variant (64px / 128px) if available
                        val highResImage = if (rawIcon is MultiResolutionImage) {
                            val variants = rawIcon.resolutionVariants
                            variants.maxByOrNull { it.getWidth(null) } ?: rawIcon.getResolutionVariant(64.0, 64.0)
                        } else {
                            rawIcon
                        }

                        val composeBitmap = renderCrispBitmap(highResImage, targetSize = 72)
                        if (composeBitmap != null) {
                            iconCache[cleanPath] = composeBitmap
                            return@withContext composeBitmap
                        }
                    }
                } catch (_: Throwable) {}

                // 2. Fallback to FileSystemView with supersampling
                val swingIcon = fileSystemView.getSystemIcon(file)
                if (swingIcon != null && swingIcon.iconWidth > 0 && swingIcon.iconHeight > 0) {
                    val bufferedImage = BufferedImage(
                        swingIcon.iconWidth * 2,
                        swingIcon.iconHeight * 2,
                        BufferedImage.TYPE_INT_ARGB
                    )
                    val g2d: Graphics2D = bufferedImage.createGraphics()
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                    g2d.scale(2.0, 2.0)
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

    private fun renderCrispBitmap(image: Image, targetSize: Int): ImageBitmap? {
        try {
            val srcW = image.getWidth(null).takeIf { it > 0 } ?: targetSize
            val srcH = image.getHeight(null).takeIf { it > 0 } ?: targetSize

            val outputSize = maxOf(targetSize, minOf(srcW, 128))
            val bufferedImage = BufferedImage(outputSize, outputSize, BufferedImage.TYPE_INT_ARGB)
            val g2d: Graphics2D = bufferedImage.createGraphics()
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
            g2d.drawImage(image, 0, 0, outputSize, outputSize, null)
            g2d.dispose()

            return bufferedImage.toComposeImageBitmap()
        } catch (_: Throwable) {
            return null
        }
    }
}
