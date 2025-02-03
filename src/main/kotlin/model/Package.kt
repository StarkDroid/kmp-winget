package model

data class Package(
    val id: String,
    val name: String,
    val version: String,
    val availableVersion: String? = null
)