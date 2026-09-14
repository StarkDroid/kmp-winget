package com.velocity.kmpwinget.domain.model

/**
 * Robust semantic and numerical version comparator for Windows software packages.
 * Compares multi-part versions (e.g., 2024.2 vs 2026.1.4.7, 24.09 vs 26.03, 1.18.2 vs 1.14.0.0).
 */
object VersionComparator {

    /**
     * Checks if [available] version is strictly newer than [current] installed version.
     */
    fun isNewer(current: String?, available: String?): Boolean {
        if (available.isNullOrBlank() || current.isNullOrBlank()) return false

        val cur = sanitize(current)
        val avail = sanitize(available)

        if (cur.isBlank() || avail.isBlank()) return false
        if (cur.equals(avail, ignoreCase = true)) return false

        return try {
            compare(avail, cur) > 0
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Compares two version strings. Returns:
     *  > 0 if v1 > v2
     *  < 0 if v1 < v2
     *  = 0 if v1 == v2
     */
    fun compare(v1: String, v2: String): Int {
        val s1 = sanitize(v1)
        val s2 = sanitize(v2)

        val tokens1 = tokenize(s1)
        val tokens2 = tokenize(s2)

        val maxLen = maxOf(tokens1.size, tokens2.size)
        for (i in 0 until maxLen) {
            val t1 = tokens1.getOrNull(i) ?: 0L
            val t2 = tokens2.getOrNull(i) ?: 0L
            if (t1 != t2) {
                return t1.compareTo(t2)
            }
        }
        return 0
    }

    private fun sanitize(version: String): String {
        return version.trim()
            .replace(Regex("^<\\s*"), "")
            .replace(Regex("^v(er(sion)?)?\\.?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*winget\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*msstore\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\(.*?\\)"), "") // remove (x64), (x86) etc
            .trim()
    }

    private fun tokenize(version: String): List<Long> {
        val parts = version.split(Regex("[.\\-_+\\s]"))
            .filter { it.isNotBlank() }

        val numbers = mutableListOf<Long>()
        for (part in parts) {
            val numeric = part.filter { it.isDigit() }
            if (numeric.isNotEmpty()) {
                numeric.toLongOrNull()?.let { numbers.add(it) }
            }
        }
        return numbers.ifEmpty { listOf(0L) }
    }
}
