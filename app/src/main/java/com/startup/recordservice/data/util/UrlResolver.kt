package com.startup.recordservice.data.util

import com.startup.recordservice.data.config.AppConfig

/**
 * Backends often return relative image paths like:
 * - "/uploads/x.jpg"
 * - "uploads/x.jpg"
 * This helper converts them into absolute URLs based on the app BASE_URL.
 */
object UrlResolver {
    fun resolve(url: String?): String? {
        val raw = url?.trim().orEmpty()
        if (raw.isBlank()) return null

        // Already absolute
        if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            return raw
        }

        // Build origin from BASE_URL (strip trailing /api/ if present)
        val base = AppConfig.BASE_URL.trim()
        val origin = base
            .removeSuffix("/")
            .removeSuffix("/api")
            .removeSuffix("/api/")

        val path = if (raw.startsWith("/")) raw else "/$raw"
        return origin + path
    }
}

