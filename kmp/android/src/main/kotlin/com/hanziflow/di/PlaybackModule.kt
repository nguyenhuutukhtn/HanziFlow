package com.hanziflow.di

import com.hanziflow.playback.PlayerConnection
import com.hanziflow.playback.PlayerConnectionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {
    /**
     * [PlayerConnection] wraps the single [androidx.media3.session.MediaController] the UI
     * talks to; one singleton instance for the app process, same as the former
     * Activity-scoped controller that outlived configuration changes via the service.
     */
    @Binds
    @Singleton
    abstract fun bindPlayerConnection(impl: PlayerConnectionImpl): PlayerConnection
}
