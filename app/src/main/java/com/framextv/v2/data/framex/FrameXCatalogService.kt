package com.framextv.v2.data.framex

import com.framextv.v2.domain.model.CatalogRow
import com.framextv.v2.domain.model.ContentType
import com.framextv.v2.domain.model.Meta
import com.framextv.v2.domain.model.MetaCastMember
import com.framextv.v2.domain.model.MetaPreview
import com.framextv.v2.domain.model.MetaTrailer
import com.framextv.v2.domain.model.PosterShape
import com.framextv.v2.domain.model.Stream
import com.framextv.v2.domain.model.StreamBehaviorHints
import com.framextv.v2.domain.model.Subtitle
import com.framextv.v2.domain.model.Video
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.JsonElement

@Singleton
class FrameXCatalogService @Inject constructor(
    private val transport: FrameXTransport,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Volatile
    private var homeCache: HomeDto? = null
    @Volatile
    private var homeCachedAtMs: Long = 0L

    suspend fun catalogRow(
        catalogId: String,
        catalogName: String,
        type: String,
    ): FrameXResult<CatalogRow> {
        val home = loadHome()
        val envelope = home.rails?.get(catalogId)
            ?: return FrameXResult.Failure("Unknown frameX catalog: $catalogId")
        val items = envelope.results.orEmpty().mapNotNull { it.toMetaPreview() }
        return FrameXResult.Success(
            CatalogRow(
                addonId = FrameXBuiltinAddon.ID,
                addonName = FrameXBuiltinAddon.NAME,
                addonBaseUrl = FrameXBuiltinAddon.BASE_URL,
                catalogId = catalogId,
                catalogName = catalogName.ifBlank { FrameXBuiltinAddon.catalogTitle(catalogId) },
                type = ContentType.fromString(type),
                rawType = type,
                items = items,
                isLoading = false,
                hasMore = false,
                currentPage = 0,
                supportsSkip = false,
                skipStep = 0,
                nextSkip = 0,
            ),
        )
    }

    suspend fun meta(type: String, id: String): FrameXResult<Meta> {
        val frameType = toFrameXType(type)
        val contentId = stripTypePrefix(id)
        return when (
            val raw = transport.get(
                "/api/details",
                linkedMapOf("type" to frameType, "id" to contentId),
            )
        ) {
            is FrameXResult.Failure -> raw
            is FrameXResult.Success -> {
                val dto = runCatching { json.decodeFromString<DetailsDto>(raw.value) }
                    .getOrElse { return FrameXResult.Failure("Invalid details response") }
                val meta = dto.toMeta(fallbackType = type, fallbackId = contentId)
                    ?: return FrameXResult.Failure("Invalid details payload")
                // Prefetch first season episodes for series so Play has videos.
                val withEpisodes = if (meta.type == ContentType.SERIES || meta.type == ContentType.TV) {
                    enrichEpisodes(meta, frameType, contentId)
                } else {
                    meta
                }
                FrameXResult.Success(withEpisodes)
            }
        }
    }

    suspend fun providers(category: String? = "movie_tv"): FrameXResult<List<FrameXProvider>> {
        return when (val raw = transport.get("/api/providers")) {
            is FrameXResult.Failure -> raw
            is FrameXResult.Success -> {
                val dto = runCatching { json.decodeFromString<ProvidersDto>(raw.value) }
                    .getOrElse { return FrameXResult.Failure("Invalid providers response") }
                val list = dto.providers.orEmpty()
                    .mapNotNull { it.toDomain() }
                    .filter { provider ->
                        category.isNullOrBlank() ||
                            provider.category.isNullOrBlank() ||
                            provider.category.equals(category, ignoreCase = true) ||
                            provider.category.equals("all", ignoreCase = true)
                    }
                if (list.isEmpty()) {
                    FrameXResult.Failure("No frameX providers")
                } else {
                    FrameXResult.Success(list)
                }
            }
        }
    }

    suspend fun streams(
        type: String,
        videoId: String,
        season: Int?,
        episode: Int?,
        provider: String? = null,
    ): FrameXResult<List<Stream>> {
        val (contentId, seasonNum, episodeNum) = parseVideoId(videoId, season, episode)
        val frameType = toFrameXType(type)
        val query = linkedMapOf(
            "type" to frameType,
            "id" to contentId,
            "season" to (seasonNum ?: 0).toString(),
            "episode" to (episodeNum ?: 0).toString(),
        )
        provider?.trim()?.takeIf { it.isNotEmpty() }?.let { query["provider"] = it }
        return when (val raw = transport.get("/api/stream", query)) {
            is FrameXResult.Failure -> raw
            is FrameXResult.Success -> {
                val dto = runCatching { json.decodeFromString<StreamDto>(raw.value) }
                    .getOrElse { return FrameXResult.Failure("Invalid stream response") }
                val streams = dto.sources.orEmpty().mapNotNull { source ->
                    val url = source.url?.trim()?.takeIf { it.startsWith("http") } ?: return@mapNotNull null
                    val language = source.language ?: source.lang
                    Stream(
                        name = source.quality ?: source.server ?: dto.provider,
                        title = listOfNotNull(source.quality, language, source.server)
                            .joinToString(" • ")
                            .ifBlank { dto.provider },
                        description = null,
                        url = url,
                        ytId = null,
                        infoHash = null,
                        fileIdx = null,
                        externalUrl = null,
                        behaviorHints = StreamBehaviorHints(
                            notWebReady = null,
                            bingeGroup = null,
                            countryWhitelist = null,
                            proxyHeaders = source.headers?.let { headers ->
                                com.framextv.v2.domain.model.ProxyHeaders(
                                    request = headers,
                                    response = null,
                                )
                            },
                        ),
                        addonName = FrameXBuiltinAddon.NAME,
                        addonLogo = null,
                        quality = source.quality,
                        subtitles = dto.subtitles.orEmpty().mapNotNull { sub ->
                            val subUrl = sub.url?.trim()?.takeIf { it.startsWith("http") } ?: return@mapNotNull null
                            Subtitle(
                                id = subUrl,
                                url = subUrl,
                                lang = sub.language ?: sub.lang ?: "und",
                                addonName = FrameXBuiltinAddon.NAME,
                                addonLogo = null,
                            )
                        },
                    )
                }
                if (streams.isEmpty()) {
                    FrameXResult.Failure("No frameX streams")
                } else {
                    FrameXResult.Success(streams)
                }
            }
        }
    }

    private suspend fun loadHome(): HomeDto {
        val now = System.currentTimeMillis()
        homeCache?.takeIf { now - homeCachedAtMs < HOME_TTL_MS }?.let { return it }
        return when (val raw = transport.get("/api/home")) {
            is FrameXResult.Failure -> HomeDto()
            is FrameXResult.Success -> {
                val dto = runCatching { json.decodeFromString<HomeDto>(raw.value) }.getOrElse { HomeDto() }
                homeCache = dto
                homeCachedAtMs = now
                dto
            }
        }
    }

    private suspend fun enrichEpisodes(meta: Meta, frameType: String, contentId: String): Meta {
        val seasonNumber = meta.videos.mapNotNull { it.season }.minOrNull()
            ?: 1
        return when (
            val raw = transport.get(
                "/api/season",
                linkedMapOf(
                    "type" to frameType,
                    "id" to contentId,
                    "season" to seasonNumber.toString(),
                ),
            )
        ) {
            is FrameXResult.Failure -> meta
            is FrameXResult.Success -> {
                val dto = runCatching { json.decodeFromString<SeasonDto>(raw.value) }.getOrNull()
                    ?: return meta
                val episodes = dto.episodes.orEmpty().mapNotNull { ep ->
                    val number = ep.number ?: ep.episodeNumber ?: return@mapNotNull null
                    Video(
                        id = "$contentId:$seasonNumber:$number",
                        title = ep.title ?: ep.name ?: "Episode $number",
                        released = null,
                        season = seasonNumber,
                        episode = number,
                        thumbnail = ep.still,
                        overview = ep.overview,
                        available = true,
                    )
                }
                if (episodes.isEmpty()) meta else meta.copy(videos = episodes)
            }
        }
    }

    private fun CatalogTitleDto.toMetaPreview(): MetaPreview? {
        val contentId = id.scalarText()?.trim().orEmpty()
        val contentTitle = (title ?: name)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val contentType = (type ?: mediaType).toNuvioContentType()
        if (contentId.isEmpty()) return null
        return MetaPreview(
            id = contentId,
            type = contentType,
            name = contentTitle,
            poster = poster ?: image,
            posterShape = PosterShape.POSTER,
            background = backdrop,
            logo = null,
            description = overview,
            releaseInfo = year.scalarText(),
            imdbRating = rating.scalarText()?.toFloatOrNull(),
            genres = emptyList(),
            sourceAddonBaseUrl = FrameXBuiltinAddon.BASE_URL,
        )
    }

    private fun DetailsDto.toMeta(fallbackType: String, fallbackId: String): Meta? {
        val contentId = id.scalarText()?.trim()?.ifEmpty { null } ?: fallbackId
        val contentTitle = (title ?: name)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val contentType = (type ?: mediaType)?.toNuvioContentType()
            ?: ContentType.fromString(fallbackType)
        val ytIds = (trailers.orEmpty() + videos.orEmpty())
            .filter { it.site.equals("YouTube", ignoreCase = true) || it.key != null }
            .mapNotNull { it.key?.trim()?.takeIf(String::isNotEmpty) }
        return Meta(
            id = contentId,
            type = contentType,
            name = contentTitle,
            poster = poster ?: image,
            posterShape = PosterShape.POSTER,
            background = backdrop,
            logo = null,
            description = overview,
            releaseInfo = year.scalarText(),
            imdbRating = rating.scalarText()?.toFloatOrNull(),
            genres = genres.orEmpty(),
            runtime = runtime.scalarText(),
            director = emptyList(),
            cast = cast.orEmpty().mapNotNull { it.name },
            castMembers = cast.orEmpty().mapNotNull { member ->
                val name = member.name?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                MetaCastMember(name = name, character = member.character, photo = member.profile)
            },
            videos = emptyList(),
            country = null,
            awards = null,
            language = null,
            links = emptyList(),
            trailerYtIds = ytIds,
            trailers = ytIds.map { MetaTrailer(source = "youtube", type = "trailer", ytId = it) },
        )
    }

    private fun parseVideoId(
        videoId: String,
        season: Int?,
        episode: Int?,
    ): Triple<String, Int?, Int?> {
        val parts = videoId.split(':')
        return when {
            parts.size >= 3 && parts[1].toIntOrNull() != null && parts[2].toIntOrNull() != null ->
                Triple(parts[0], parts[1].toInt(), parts[2].toInt())
            else -> Triple(stripTypePrefix(videoId), season, episode)
        }
    }

    private fun stripTypePrefix(id: String): String {
        val trimmed = id.trim()
        val prefixes = listOf("movie:", "tv:", "anime:", "series:")
        for (prefix in prefixes) {
            if (trimmed.startsWith(prefix, ignoreCase = true)) {
                return trimmed.substring(prefix.length)
            }
        }
        return trimmed
    }

    private fun toFrameXType(type: String): String = when (type.trim().lowercase()) {
        "series", "tv" -> "tv"
        "anime" -> "anime"
        else -> "movie"
    }

    private fun String?.toNuvioContentType(): ContentType = when (this?.trim()?.lowercase()) {
        "tv", "series" -> ContentType.SERIES
        "anime" -> ContentType.SERIES
        "channel" -> ContentType.CHANNEL
        else -> ContentType.MOVIE
    }

    private fun JsonElement?.scalarText(): String? =
        (this as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val HOME_TTL_MS = 5 * 60 * 1000L
    }
}
