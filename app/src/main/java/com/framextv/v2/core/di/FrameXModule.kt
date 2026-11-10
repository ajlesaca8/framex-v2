package com.framextv.v2.core.di

import com.framextv.v2.data.framex.FrameXApiClient
import com.framextv.v2.data.framex.FrameXTransport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FrameXModule {
    @Binds
    @Singleton
    abstract fun bindFrameXTransport(impl: FrameXApiClient): FrameXTransport
}
