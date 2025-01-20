package model

data class Package(
    val id: String,
    val name: String,
    val version: String,
    val availableVersion: String? = null,
    val uniqueId: String = "${System.nanoTime()}_${id}_${version}",
    val isMsStorePackage: Boolean = id.startsWith("msstore") || id.contains("Microsoft."),
    val installLocation: String? = null,
    val publisher: String? = null,
    val description: String? = null
) {
    val hasNonMsStoreUpgrade: Boolean
        get() = !isMsStorePackage && availableVersion != null
}
