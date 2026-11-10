package com.framextv.v2.core.sync.library

import com.framextv.v2.domain.model.LibraryDeltaApplyResult
import com.framextv.v2.domain.model.LibraryDeltaEvent
import com.framextv.v2.domain.model.LibrarySnapshotApplyResult
import com.framextv.v2.domain.model.LibrarySyncState
import com.framextv.v2.domain.model.SavedLibraryItem

interface LibrarySyncLocalStore {
    suspend fun getSyncState(profileId: Int): LibrarySyncState

    suspend fun applyRemoteSnapshot(
        profileId: Int,
        remoteItems: Collection<SavedLibraryItem>,
        cursorEventId: Long
    ): LibrarySnapshotApplyResult

    suspend fun applyRemoteDelta(
        profileId: Int,
        events: Collection<LibraryDeltaEvent>
    ): LibraryDeltaApplyResult

    suspend fun queueAllItemsForPush(profileId: Int): LibrarySyncState

    suspend fun acknowledgePush(
        profileId: Int,
        expectedMutationRevision: Long
    ): Boolean
}
