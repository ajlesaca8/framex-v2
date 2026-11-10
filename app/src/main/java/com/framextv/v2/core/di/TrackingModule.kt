package com.framextv.v2.core.di

import com.framextv.v2.data.simkl.AndroidSimklAuthStorage
import com.framextv.v2.data.simkl.AndroidSimklSyncStorage
import com.framextv.v2.data.simkl.SimklAuthStorage
import com.framextv.v2.data.simkl.SimklApiSyncRemote
import com.framextv.v2.data.simkl.SimklSyncRemote
import com.framextv.v2.data.simkl.SimklSyncStorage
import com.framextv.v2.core.tracking.TrackingLibraryProvider
import com.framextv.v2.core.tracking.TrackingProvider
import com.framextv.v2.data.repository.TraktTrackingLibraryProvider
import com.framextv.v2.data.repository.TraktTrackingProvider
import com.framextv.v2.data.simkl.SimklLibraryService
import com.framextv.v2.core.tracking.TrackingHistoryWriter
import com.framextv.v2.core.tracking.TrackingProgressProvider
import com.framextv.v2.data.repository.TraktTrackingHistoryWriter
import com.framextv.v2.data.repository.TraktTrackingProgressProvider
import com.framextv.v2.data.simkl.SimklTrackingHistoryWriter
import com.framextv.v2.data.simkl.SimklTrackingProgressProvider
import com.framextv.v2.data.simkl.SimklTrackingProvider
import com.framextv.v2.core.profile.ProfileScopedCredentialStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {
    @Binds
    @Singleton
    abstract fun bindSimklAuthStorage(storage: AndroidSimklAuthStorage): SimklAuthStorage

    @Binds
    @IntoSet
    abstract fun bindSimklProfileScopedCredentialStore(
        storage: AndroidSimklAuthStorage
    ): ProfileScopedCredentialStore

    @Binds
    @Singleton
    abstract fun bindSimklSyncStorage(storage: AndroidSimklSyncStorage): SimklSyncStorage

    @Binds
    @Singleton
    abstract fun bindSimklSyncRemote(remote: SimklApiSyncRemote): SimklSyncRemote

    @Binds
    @IntoSet
    abstract fun bindTraktTrackingLibraryProvider(
        provider: TraktTrackingLibraryProvider
    ): TrackingLibraryProvider

    @Binds
    @IntoSet
    abstract fun bindSimklTrackingLibraryProvider(
        provider: SimklLibraryService
    ): TrackingLibraryProvider

    @Binds
    @IntoSet
    abstract fun bindTraktTrackingProgressProvider(
        provider: TraktTrackingProgressProvider
    ): TrackingProgressProvider

    @Binds
    @IntoSet
    abstract fun bindSimklTrackingProgressProvider(
        provider: SimklTrackingProgressProvider
    ): TrackingProgressProvider

    @Binds
    @IntoSet
    abstract fun bindTraktTrackingHistoryWriter(
        writer: TraktTrackingHistoryWriter
    ): TrackingHistoryWriter

    @Binds
    @IntoSet
    abstract fun bindSimklTrackingHistoryWriter(
        writer: SimklTrackingHistoryWriter
    ): TrackingHistoryWriter

    @Binds
    @IntoSet
    abstract fun bindTraktTrackingProvider(
        provider: TraktTrackingProvider
    ): TrackingProvider

    @Binds
    @IntoSet
    abstract fun bindSimklTrackingProvider(
        provider: SimklTrackingProvider
    ): TrackingProvider
}
