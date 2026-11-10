package com.framextv.v2.core.tracking

import com.framextv.v2.core.sync.StartupSyncService
import com.framextv.v2.core.sync.WatchedItemsSyncService
import com.framextv.v2.core.profile.ProfileManager
import com.framextv.v2.data.local.ContinueWatchingEnrichmentCache
import com.framextv.v2.data.local.TraktSettingsDataStore
import com.framextv.v2.data.local.WatchProgressPreferences
import com.framextv.v2.data.local.WatchProgressSource
import com.framextv.v2.data.local.WatchedItemsPreferences
import com.framextv.v2.data.local.WatchedSeriesStateHolder
import com.framextv.v2.data.repository.TraktProgressService
import com.framextv.v2.data.repository.isTraktCompatibleId
import com.framextv.v2.data.simkl.SimklSyncRepository
import com.framextv.v2.domain.model.LibrarySourceMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class TrackingSourceController @Inject constructor(
    private val settingsDataStore: TraktSettingsDataStore,
    private val traktProgressService: TraktProgressService,
    private val simklSyncRepository: SimklSyncRepository,
    private val startupSyncService: StartupSyncService,
    private val watchedItemsPreferences: WatchedItemsPreferences,
    private val watchProgressPreferences: WatchProgressPreferences,
    private val watchedItemsSyncService: WatchedItemsSyncService,
    private val watchedSeriesStateHolder: WatchedSeriesStateHolder,
    private val continueWatchingEnrichmentCache: ContinueWatchingEnrichmentCache,
    private val profileManager: ProfileManager
) {
    val watchProgressSource = settingsDataStore.watchProgressSource
    val librarySourceMode = settingsDataStore.librarySourceMode

    private val mutationMutex = Mutex()

    suspend fun selectWatchProgressSource(source: WatchProgressSource) {
        val profileId = profileManager.activeProfileId.value
        mutationMutex.withLock {
            if (profileManager.activeProfileId.value != profileId) return
            if (settingsDataStore.getWatchProgressSource(profileId) == source) return
            applyWatchProgressSource(source, profileId)
        }
    }

    suspend fun selectLibrarySourceMode(mode: LibrarySourceMode) {
        val profileId = profileManager.activeProfileId.value
        mutationMutex.withLock {
            if (profileManager.activeProfileId.value != profileId) return
            if (settingsDataStore.getLibrarySourceMode(profileId) == mode) return
            applyLibrarySourceMode(mode, profileId)
        }
    }

    suspend fun reconcileConnectedProviders(
        connectedProviderIds: Set<TrackingProviderId>
    ): TrackingSourceSelection = mutationMutex.withLock {
        val profileId = profileManager.activeProfileId.value
        val requested = TrackingSourceSelection(
            watchProgressSource = settingsDataStore.getWatchProgressSource(profileId),
            librarySourceMode = settingsDataStore.getLibrarySourceMode(profileId)
        )
        val effective = effectiveTrackingSourceSelection(requested, connectedProviderIds)
        if (profileManager.activeProfileId.value != profileId) return@withLock effective
        if (requested.watchProgressSource != effective.watchProgressSource) {
            applyWatchProgressSource(effective.watchProgressSource, profileId)
        }
        if (requested.librarySourceMode != effective.librarySourceMode) {
            applyLibrarySourceMode(effective.librarySourceMode, profileId)
        }
        effective
    }

    private suspend fun applyWatchProgressSource(source: WatchProgressSource, profileId: Int) {
        settingsDataStore.setWatchProgressSource(source, profileId)
        if (profileManager.activeProfileId.value != profileId) return
        continueWatchingEnrichmentCache.saveInProgressSnapshot(emptyList(), force = true)
        continueWatchingEnrichmentCache.saveNextUpSnapshot(emptyList(), force = true)
        when (source) {
            WatchProgressSource.TRAKT -> {
                watchProgressPreferences.clearAllPreservingNonTraktIds(profileId) { contentId ->
                    !isTraktCompatibleId(contentId)
                }
                watchedItemsPreferences.clearAll(profileId)
                watchedSeriesStateHolder.update(emptySet())
                traktProgressService.refreshNow()
            }
            WatchProgressSource.SIMKL -> {
                watchedItemsPreferences.clearAll(profileId)
                watchedSeriesStateHolder.update(emptySet())
                simklSyncRepository.refresh(TrackingRefreshIntent.USER_INITIATED)
            }
            WatchProgressSource.NUVIO_SYNC -> {
                repopulateWatchedItemsFromNuvioSync(profileId)
                if (profileManager.activeProfileId.value == profileId) {
                    startupSyncService.requestSyncNow()
                }
            }
        }
    }

    private suspend fun applyLibrarySourceMode(mode: LibrarySourceMode, profileId: Int) {
        settingsDataStore.setLibrarySourceMode(mode, profileId)
        if (profileManager.activeProfileId.value != profileId) return
        if (mode == LibrarySourceMode.SIMKL) {
            simklSyncRepository.refresh(TrackingRefreshIntent.USER_INITIATED)
        }
    }

    private suspend fun repopulateWatchedItemsFromNuvioSync(profileId: Int) {
        runCatching {
            watchedItemsSyncService.syncSnapshotFromRemote(profileId).getOrElse { return }
        }
    }
}
