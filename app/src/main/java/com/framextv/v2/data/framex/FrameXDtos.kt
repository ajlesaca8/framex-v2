package com.framextv.v2.data.framex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class HomeDto(
    val success: Boolean? = null,
    val hero: CatalogTitleDto? = null,
    val rails: Map<String, CatalogEnvelopeDto>? = null,
)

@Serializable
data class CatalogEnvelopeDto(
    val success: Boolean? = null,
    val page: Int? = null,
    val totalPages: Int? = null,
    @SerialName("total_pages") val totalPagesSnakeCase: Int? = null,
    val results: List<CatalogTitleDto>? = null,
)

@Serializable
data class CatalogTitleDto(
    val id: JsonElement? = null,
    val title: String? = null,
    val name: String? = null,
    val type: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    val poster: String? = null,
    val image: String? = null,
    val backdrop: String? = null,
    val overview: String? = null,
    val year: JsonElement? = null,
    val rating: JsonElement? = null,
)

@Serializable
data class DetailsDto(
    val success: Boolean? = null,
    val id: JsonElement? = null,
    val title: String? = null,
    val name: String? = null,
    val type: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    val overview: String? = null,
    val poster: String? = null,
    val image: String? = null,
    val backdrop: String? = null,
    val year: JsonElement? = null,
    val rating: JsonElement? = null,
    val runtime: JsonElement? = null,
    val genres: List<String>? = null,
    val cast: List<CastDto>? = null,
    val seasons: List<SeasonSummaryDto>? = null,
    val trailers: List<TrailerDto>? = null,
    val videos: List<TrailerDto>? = null,
)

@Serializable
data class SeasonSummaryDto(
    val number: Int? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    val name: String? = null,
    @SerialName("episode_count") val episodeCount: Int? = null,
)

@Serializable
data class CastDto(
    val name: String? = null,
    val character: String? = null,
    val profile: String? = null,
)

@Serializable
data class TrailerDto(
    val key: String? = null,
    val site: String? = null,
    val type: String? = null,
)

@Serializable
data class SeasonDto(
    val success: Boolean? = null,
    val number: Int? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    val episodes: List<EpisodeDto>? = null,
)

@Serializable
data class EpisodeDto(
    val number: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    val name: String? = null,
    val title: String? = null,
    val overview: String? = null,
    val still: String? = null,
)

@Serializable
data class StreamDto(
    val success: Boolean? = null,
    val provider: String? = null,
    val sources: List<SourceDto>? = null,
    val subtitles: List<SubtitleDto>? = null,
    val alternates: List<String>? = null,
    val errors: List<StreamErrorDto>? = null,
)

@Serializable
data class SourceDto(
    val url: String? = null,
    val quality: String? = null,
    val type: String? = null,
    val server: String? = null,
    val provider: String? = null,
    val language: String? = null,
    val lang: String? = null,
    val headers: Map<String, String>? = null,
    val skipProxy: Boolean? = null,
)

@Serializable
data class SubtitleDto(
    val url: String? = null,
    val label: String? = null,
    val language: String? = null,
    val lang: String? = null,
)

@Serializable
data class StreamErrorDto(
    val provider: String? = null,
    val code: String? = null,
    val ms: Long? = null,
    val error: String? = null,
)

@Serializable
data class ProvidersDto(
    val providers: List<ProviderDto>? = null,
)

@Serializable
data class ProviderDto(
    val id: String? = null,
    val name: String? = null,
    val tagline: String? = null,
    val category: String? = null,
    val status: String? = null,
)
