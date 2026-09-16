package com.velocity.kmpwinget.domain.model

import androidx.compose.runtime.mutableStateOf
import java.util.prefs.Preferences

enum class NotchPosition(val displayName: String) {
    TOP_CENTER("Top Center (MacBook Notch)"),
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right")
}

object FavoritesManager {
    private val prefs = Preferences.userNodeForPackage(FavoritesManager::class.java)
    private const val KEY_FAVORITES = "favorite_package_ids"
    private const val KEY_NOTCH_ENABLED = "notch_overlay_enabled"
    private const val KEY_NOTCH_POSITION = "notch_position"

    val isNotchEnabled = mutableStateOf(prefs.getBoolean(KEY_NOTCH_ENABLED, false))
    val notchPosition = mutableStateOf(
        try {
            NotchPosition.valueOf(prefs.get(KEY_NOTCH_POSITION, NotchPosition.TOP_CENTER.name))
        } catch (_: Throwable) {
            NotchPosition.TOP_CENTER
        }
    )

    private val _favoriteIds = mutableStateOf(loadFavorites())
    val favoriteIds: androidx.compose.runtime.State<Set<String>> = _favoriteIds

    private fun loadFavorites(): Set<String> {
        val raw = prefs.get(KEY_FAVORITES, "")
        return if (raw.isBlank()) emptySet() else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun isFavorite(packageId: String): Boolean {
        return _favoriteIds.value.contains(packageId)
    }

    fun toggleFavorite(packageId: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(packageId)) {
            current.remove(packageId)
        } else {
            current.add(packageId)
        }
        _favoriteIds.value = current
        saveFavorites(current)
    }

    fun setNotchEnabled(enabled: Boolean) {
        isNotchEnabled.value = enabled
        prefs.putBoolean(KEY_NOTCH_ENABLED, enabled)
    }

    fun setPosition(pos: NotchPosition) {
        notchPosition.value = pos
        prefs.put(KEY_NOTCH_POSITION, pos.name)
    }

    private fun saveFavorites(set: Set<String>) {
        prefs.put(KEY_FAVORITES, set.joinToString(","))
    }
}
