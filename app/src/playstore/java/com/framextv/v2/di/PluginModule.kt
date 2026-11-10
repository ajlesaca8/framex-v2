package com.framextv.v2.di

import com.framextv.v2.core.plugin.PluginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PluginModule {
    @Provides
    @Singleton
    fun providePluginManager(): PluginManager {
        return PluginManager()
    }
}
