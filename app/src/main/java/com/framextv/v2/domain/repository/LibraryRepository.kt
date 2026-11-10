package com.framextv.v2.domain.repository

import com.framextv.v2.domain.model.LibraryEntry
import com.framextv.v2.domain.model.LibraryEntryInput
import com.framextv.v2.domain.model.LibraryListTab
import com.framextv.v2.domain.model.LibrarySourceMode
import com.framextv.v2.domain.model.ListMembershipChanges
import com.framextv.v2.domain.model.ListMembershipSnapshot
import com.framextv.v2.domain.model.TraktListPrivacy
import com.framextv.v2.core.tracking.TrackingMembershipApplyResult
import com.framextv.v2.core.tracking.TrackingProviderId
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    val sourceMode: Flow<LibrarySourceMode>
    val isSyncing: Flow<Boolean>
    val libraryItems: Flow<List<LibraryEntry>>
    val listTabs: Flow<List<LibraryListTab>>
    val membershipListTabs: Flow<List<LibraryListTab>>

    fun isInLibrary(itemId: String, itemType: String): Flow<Boolean>
    fun isInWatchlist(itemId: String, itemType: String): Flow<Boolean>

    suspend fun toggleDefault(
        item: LibraryEntryInput,
        confirmedRemovalProviders: Set<TrackingProviderId> = emptySet()
    ): TrackingMembershipApplyResult
    suspend fun getMembershipSnapshot(item: LibraryEntryInput): ListMembershipSnapshot
    suspend fun applyMembershipChanges(
        item: LibraryEntryInput,
        changes: ListMembershipChanges,
        confirmedRemovalProviders: Set<TrackingProviderId> = emptySet()
    ): TrackingMembershipApplyResult

    suspend fun createPersonalList(
        name: String,
        description: String?,
        privacy: TraktListPrivacy
    )

    suspend fun updatePersonalList(
        listId: String,
        name: String,
        description: String?,
        privacy: TraktListPrivacy
    )

    suspend fun deletePersonalList(listId: String)
    suspend fun reorderPersonalLists(orderedListIds: List<String>)
    suspend fun refreshNow()
}
