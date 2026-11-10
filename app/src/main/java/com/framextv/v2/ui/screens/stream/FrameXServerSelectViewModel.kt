package com.framextv.v2.ui.screens.stream

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framextv.v2.core.profile.ProfileManager
import com.framextv.v2.core.tmdb.TmdbService
import com.framextv.v2.data.framex.AtlanticEmbed
import com.framextv.v2.data.framex.FrameXCatalogService
import com.framextv.v2.data.framex.FrameXProvider
import com.framextv.v2.data.framex.FrameXProviderStatus
import com.framextv.v2.data.framex.FrameXResult
import com.framextv.v2.data.local.PlayerPreference
import com.framextv.v2.data.local.PlayerSettingsDataStore
import com.framextv.v2.domain.model.Stream
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FrameXServerSelectUiState(
    val title: String = "",
    val poster: String? = null,
    val backdrop: String? = null,
    val providers: List<FrameXProvider> = emptyList(),
    val isLoading: Boolean = true,
    val connectingProviderId: String? = null,
    val errorMessage: String? = null,
    val connectErrorMessage: String? = null,
)

@HiltViewModel
class FrameXServerSelectViewModel @Inject constructor(
    private val frameXCatalogService: FrameXCatalogService,
    private val tmdbService: TmdbService,
    private val playerSettingsDataStore: PlayerSettingsDataStore,
    profileManager: ProfileManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val videoId: String = savedStateHandle["videoId"] ?: ""
    private val contentType: String = savedStateHandle["contentType"] ?: ""
    private val title: String = savedStateHandle["title"] ?: ""
    private val poster: String? = savedStateHandle.getOptionalString("poster")
    private val backdrop: String? = savedStateHandle.getOptionalString("backdrop")
    private val logo: String? = savedStateHandle.getOptionalString("logo")
    private val season: Int? = savedStateHandle.get<String>("season")?.toIntOrNull()
    private val episode: Int? = savedStateHandle.get<String>("episode")?.toIntOrNull()
    private val episodeName: String? = savedStateHandle.getOptionalString("episodeName")
    private val year: String? = savedStateHandle.getOptionalString("year")
    private val contentId: String? = savedStateHandle.getOptionalString("contentId")
    private val contentName: String? = savedStateHandle.getOptionalString("contentName")
    private val contentLanguage: String? = savedStateHandle.getOptionalString("contentLanguage")
    private val playbackProfileId: Int = savedStateHandle.get<String>("profileId")?.toIntOrNull()
        ?: profileManager.activeProfileId.value

    private val _uiState = MutableStateFlow(
        FrameXServerSelectUiState(
            title = title,
            poster = poster,
            backdrop = backdrop,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<FrameXServerSelectUiState> = _uiState.asStateFlow()

    val playerPreference: StateFlow<PlayerPreference?> = playerSettingsDataStore.playerSettings
        .map { it.playerPreference }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    connectErrorMessage = null,
                    connectingProviderId = null,
                )
            }
            val preferredCategory = categoryFor(contentType)
            val result = when (val preferred = frameXCatalogService.providers(preferredCategory)) {
                is FrameXResult.Success -> if (preferred.value.isNotEmpty()) {
                    preferred
                } else {
                    frameXCatalogService.providers(category = null)
                }
                is FrameXResult.Failure -> frameXCatalogService.providers(category = null).let { fallback ->
                    if (fallback is FrameXResult.Success) fallback else preferred
                }
            }
            when (result) {
                is FrameXResult.Failure -> {
                    // Still offer Atlantic even if FrameX providers fail.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            providers = listOf(AtlanticEmbed.provider()),
                            errorMessage = null,
                            connectErrorMessage = result.message.ifBlank { "Could not load FrameX servers" },
                        )
                    }
                }
                is FrameXResult.Success -> {
                    val sorted = result.value.sortedWith(
                        compareBy<FrameXProvider> {
                            when (it.status) {
                                FrameXProviderStatus.AVAILABLE -> 0
                                FrameXProviderStatus.UNKNOWN -> 1
                                FrameXProviderStatus.OFFLINE -> 2
                            }
                        }.thenBy { it.name.lowercase() },
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            providers = listOf(AtlanticEmbed.provider()) + sorted,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    fun selectProvider(
        provider: FrameXProvider,
        onReady: (ServerSelectResult) -> Unit,
    ) {
        if (!provider.isSelectable) return
        if (_uiState.value.connectingProviderId != null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    connectingProviderId = provider.id,
                    connectErrorMessage = null,
                )
            }
            if (AtlanticEmbed.isAtlantic(provider.id)) {
                resolveAtlanticEmbed(onReady)
                return@launch
            }
            when (
                val result = frameXCatalogService.streams(
                    type = contentType,
                    videoId = videoId,
                    season = season,
                    episode = episode,
                    provider = provider.id,
                )
            ) {
                is FrameXResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            connectingProviderId = null,
                            connectErrorMessage = result.message.ifBlank {
                                "No stream on ${provider.name}"
                            },
                        )
                    }
                }
                is FrameXResult.Success -> {
                    val stream = result.value.firstOrNull { !it.url.isNullOrBlank() }
                    if (stream == null) {
                        _uiState.update {
                            it.copy(
                                connectingProviderId = null,
                                connectErrorMessage = "No playable URL on ${provider.name}",
                            )
                        }
                        return@launch
                    }
                    _uiState.update { it.copy(connectingProviderId = null) }
                    onReady(ServerSelectResult.NativeStream(stream.toPlaybackInfo(provider)))
                }
            }
        }
    }

    private suspend fun resolveAtlanticEmbed(onReady: (ServerSelectResult) -> Unit) {
        val lookupId = contentId?.takeIf { it.isNotBlank() } ?: videoId
        val tmdbId = runCatching {
            tmdbService.ensureTmdbId(lookupId, contentType)
        }.getOrNull()
        val embedUrl = tmdbId?.let {
            AtlanticEmbed.embedUrl(
                tmdbId = it,
                contentType = contentType,
                season = season,
                episode = episode,
            )
        }
        if (embedUrl == null) {
            _uiState.update {
                it.copy(
                    connectingProviderId = null,
                    connectErrorMessage = "Atlantic needs a TMDB id for this title",
                )
            }
            return
        }
        val displayTitle = contentName?.takeIf { it.isNotBlank() } ?: title
        _uiState.update { it.copy(connectingProviderId = null) }
        onReady(
            ServerSelectResult.EmbedPlayer(
                embedUrl = embedUrl,
                title = displayTitle,
                contentId = contentId ?: videoId.substringBefore(":"),
                contentType = contentType,
            ),
        )
    }

    private fun Stream.toPlaybackInfo(provider: FrameXProvider): StreamPlaybackInfo {
        val displayTitle = this@FrameXServerSelectViewModel.contentName
            ?.takeIf { it.isNotBlank() }
            ?: this@FrameXServerSelectViewModel.title
        val hints = behaviorHints
        return StreamPlaybackInfo(
            url = url,
            title = displayTitle,
            streamName = listOfNotNull(provider.name, quality).joinToString(" • "),
            year = this@FrameXServerSelectViewModel.year,
            isExternal = false,
            isTorrent = false,
            infoHash = null,
            ytId = null,
            headers = hints?.proxyHeaders?.request,
            contentId = this@FrameXServerSelectViewModel.contentId
                ?: this@FrameXServerSelectViewModel.videoId.substringBefore(":"),
            contentType = this@FrameXServerSelectViewModel.contentType,
            contentName = displayTitle,
            poster = this@FrameXServerSelectViewModel.poster,
            backdrop = this@FrameXServerSelectViewModel.backdrop,
            logo = this@FrameXServerSelectViewModel.logo,
            videoId = this@FrameXServerSelectViewModel.videoId,
            season = this@FrameXServerSelectViewModel.season,
            episode = this@FrameXServerSelectViewModel.episode,
            episodeTitle = this@FrameXServerSelectViewModel.episodeName,
            bingeGroup = hints?.bingeGroup,
            profileId = playbackProfileId,
            filename = hints?.filename,
            videoHash = hints?.videoHash,
            videoSize = hints?.videoSize,
            addonName = addonName,
            addonLogo = addonLogo,
            streamDescription = title,
            fileIdx = fileIdx,
            sources = null,
            contentLanguage = this@FrameXServerSelectViewModel.contentLanguage,
        )
    }

    private fun categoryFor(type: String): String = when (type.trim().lowercase()) {
        "anime" -> "anime"
        else -> "movie_tv"
    }

    private fun SavedStateHandle.getOptionalString(key: String): String? {
        val value = get<String>(key) ?: return null
        return value.takeIf { it.isNotBlank() }
    }
}
