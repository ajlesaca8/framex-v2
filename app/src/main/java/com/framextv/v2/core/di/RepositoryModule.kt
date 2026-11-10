package com.framextv.v2.core.di

import com.framextv.v2.data.repository.AddonRepositoryImpl
import com.framextv.v2.data.repository.CatalogRepositoryImpl
import com.framextv.v2.data.repository.LibraryRepositoryImpl
import com.framextv.v2.data.repository.MetaRepositoryImpl
import com.framextv.v2.data.repository.StreamRepositoryImpl
import com.framextv.v2.data.repository.SubtitleRepositoryImpl
import com.framextv.v2.data.repository.SyncRepositoryImpl
import com.framextv.v2.data.repository.WatchProgressRepositoryImpl
import com.framextv.v2.domain.repository.AddonRepository
import com.framextv.v2.domain.repository.CatalogRepository
import com.framextv.v2.domain.repository.LibraryRepository
import com.framextv.v2.domain.repository.MetaRepository
import com.framextv.v2.domain.repository.StreamRepository
import com.framextv.v2.domain.repository.SubtitleRepository
import com.framextv.v2.domain.repository.SyncRepository
import com.framextv.v2.domain.repository.WatchProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAddonRepository(impl: AddonRepositoryImpl): AddonRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindMetaRepository(impl: MetaRepositoryImpl): MetaRepository

    @Binds
    @Singleton
    abstract fun bindStreamRepository(impl: StreamRepositoryImpl): StreamRepository

    @Binds
    @Singleton
    abstract fun bindSubtitleRepository(impl: SubtitleRepositoryImpl): SubtitleRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    @Singleton
    abstract fun bindWatchProgressRepository(impl: WatchProgressRepositoryImpl): WatchProgressRepository
}
