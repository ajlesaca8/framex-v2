package com.framextv.v2.core.sync.library

import com.framextv.v2.domain.model.LibraryDeltaEvent
import com.framextv.v2.domain.model.LibrarySyncKey
import com.framextv.v2.domain.model.SavedLibraryItem

interface LibrarySyncRemoteDataSource {
    suspend fun pullSnapshot(
        profileId: Int,
        pageSize: Int
    ): List<SavedLibraryItem>

    suspend fun getDeltaCursor(profileId: Int): Long

    suspend fun pullDelta(
        profileId: Int,
        sinceEventId: Long,
        limit: Int
    ): List<LibraryDeltaEvent>

    suspend fun pushItems(
        profileId: Int,
        items: Collection<SavedLibraryItem>
    )

    suspend fun deleteItems(
        profileId: Int,
        keys: Collection<LibrarySyncKey>
    )
}
