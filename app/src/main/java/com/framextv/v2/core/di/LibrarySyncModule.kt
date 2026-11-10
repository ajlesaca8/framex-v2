package com.framextv.v2.core.di

import com.framextv.v2.core.sync.library.LibrarySyncLocalStore
import com.framextv.v2.core.sync.library.LibrarySyncRemoteDataSource
import com.framextv.v2.data.local.LibraryPreferences
import com.framextv.v2.data.remote.supabase.SupabaseLibrarySyncRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LibrarySyncModule {
    @Binds
    @Singleton
    abstract fun bindLibrarySyncLocalStore(
        implementation: LibraryPreferences
    ): LibrarySyncLocalStore

    @Binds
    @Singleton
    abstract fun bindLibrarySyncRemoteDataSource(
        implementation: SupabaseLibrarySyncRemoteDataSource
    ): LibrarySyncRemoteDataSource
}
