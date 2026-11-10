package com.framextv.v2.data.framex

/**
 * Public Atlantic iframe embed (https://atlantic.bz/embed-api).
 * One optional Select Server entry — not a FrameX replacement.
 */
object AtlanticEmbed {
    const val PROVIDER_ID = "atlantic_embed"
    const val PROVIDER_NAME = "Atlantic"
    const val PROVIDER_TAGLINE = "Web Embed Player"

    fun provider(): FrameXProvider = FrameXProvider(
        id = PROVIDER_ID,
        name = PROVIDER_NAME,
        tagline = PROVIDER_TAGLINE,
        category = "embed",
        status = FrameXProviderStatus.AVAILABLE,
    )

    fun embedUrl(
        tmdbId: String,
        contentType: String,
        season: Int?,
        episode: Int?,
    ): String? {
        val id = tmdbId.trim().takeIf { it.isNotEmpty() && it.all(Char::isDigit) } ?: return null
        return when (contentType.trim().lowercase()) {
            "series", "tv", "show", "anime" -> {
                val s = (season ?: 1).coerceAtLeast(1)
                val e = (episode ?: 1).coerceAtLeast(1)
                "https://atlantic.st/embed/tv/$id/$s/$e"
            }
            else -> "https://atlantic.st/embed/movie/$id"
        }
    }

    fun isAtlantic(providerId: String): Boolean = providerId == PROVIDER_ID
}
