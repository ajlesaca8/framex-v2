package com.framextv.v2.data.framex

import com.framextv.v2.domain.model.Addon
import com.framextv.v2.domain.model.AddonResource
import com.framextv.v2.domain.model.CatalogDescriptor
import com.framextv.v2.domain.model.ContentType

object FrameXBuiltinAddon {
    const val ID = "com.framextv.catalog"
    const val BASE_URL = "framex://catalog"
    const val NAME = "frameX"

    private val rowTitles = linkedMapOf(
        "trending_movies" to ("Trending movies" to ContentType.MOVIE),
        "popular_movies" to ("Popular movies" to ContentType.MOVIE),
        "new_releases" to ("New releases" to ContentType.MOVIE),
        "popular_tv" to ("Popular series" to ContentType.SERIES),
        "popular_anime" to ("Popular anime" to ContentType.SERIES),
        "action_movies" to ("Action" to ContentType.MOVIE),
        "scifi_movies" to ("Sci-fi" to ContentType.MOVIE),
        "thriller_movies" to ("Thriller" to ContentType.MOVIE),
        "comedy_movies" to ("Comedy" to ContentType.MOVIE),
        "airing_anime" to ("Airing anime" to ContentType.SERIES),
    )

    val addon: Addon = Addon(
        id = ID,
        name = NAME,
        displayName = NAME,
        version = "1.0.0",
        description = "frameX catalogue and streams",
        logo = null,
        baseUrl = BASE_URL,
        catalogs = rowTitles.map { (id, titleAndType) ->
            CatalogDescriptor(
                type = titleAndType.second,
                id = id,
                name = titleAndType.first,
                showInHome = true,
                hasExplicitShowInHome = true,
            )
        },
        types = listOf(ContentType.MOVIE, ContentType.SERIES),
        resources = listOf(
            AddonResource(name = "catalog", types = listOf("movie", "series"), idPrefixes = null),
            AddonResource(name = "meta", types = listOf("movie", "series"), idPrefixes = null),
            AddonResource(name = "stream", types = listOf("movie", "series"), idPrefixes = null),
        ),
        enabled = true,
    )

    fun isFrameX(addonId: String?, baseUrl: String?): Boolean {
        if (addonId == ID) return true
        val url = baseUrl?.trim()?.trimEnd('/') ?: return false
        return url.equals(BASE_URL, ignoreCase = true) || url.startsWith("framex://", ignoreCase = true)
    }

    fun catalogTitle(catalogId: String): String =
        rowTitles[catalogId]?.first ?: catalogId.replace('_', ' ')
}
