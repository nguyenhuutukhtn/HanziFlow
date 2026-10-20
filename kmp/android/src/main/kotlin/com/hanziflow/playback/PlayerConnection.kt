package com.hanziflow.playback

import kotlinx.coroutines.flow.StateFlow

/**
 * The only thing on the UI side allowed to touch [androidx.media3.session.MediaController] /
 * [androidx.media3.session.MediaSession] types. ViewModels mirror [state]; the service
 * (not this class, not the ViewModel) remains the source of truth for playback and loop state.
 */
interface PlayerConnection {
    val state: StateFlow<PlaybackState>

    fun play(lessonId: String, track: String, uri: String, title: String)
    fun toggle()
    fun seekBack()
    fun seekForward()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun loopSegment(lineId: String, startMs: Long, endMs: Long)
    fun clearLoop()

    /** Plays a short word/sentence clip (lookup) on a side player, pausing main playback. */
    fun playClip(uri: String)
}
