package com.framextv.v2.core.streams

import com.framextv.v2.domain.model.Addon
import com.framextv.v2.domain.model.Meta
import com.framextv.v2.domain.model.ScraperInfo
import com.framextv.v2.domain.model.Video

internal fun Addon.supportsStreamResource(type: String, videoId: String): Boolean =
    resources.any { resource ->
        resource.name == "stream" &&
            (resource.types.isEmpty() || resource.types.contains(type)) &&
            run {
                val prefixes = resource.idPrefixes?.takeIf { it.isNotEmpty() }
                    ?: idPrefixes.takeIf { it.isNotEmpty() }
                prefixes == null || prefixes.any { videoId.startsWith(it) }
            }
    }

internal data class PlaybackAvailability(
    val addons: List<Addon> = emptyList(),
    val scrapers: List<ScraperInfo> = emptyList(),
    val isLoaded: Boolean = false,
    private val cachedMeta: (String, String) -> Meta? = { _, _ -> null }
) {
    fun canStream(
        type: String,
        videoId: String,
        contentId: String = videoId,
        video: Video? = null
    ): Boolean = video?.takeIf { it.id == videoId }?.streams?.isNotEmpty() == true ||
        cachedMeta(type, contentId)?.videos?.any { it.id == videoId && it.streams.isNotEmpty() } == true ||
        addons.any { it.enabled && it.supportsStreamResource(type, videoId) } ||
        scrapers.any { it.enabled && it.supportsType(type) }
}
