package com.hanziflow.ui.player

import com.hanziflow.data.ProgressRepository
import com.hanziflow.playback.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class PlaybackProgressSaver(
    private val repository: ProgressRepository,
    private val scope: CoroutineScope,
) {
    private var lastSaved: Triple<String, String, Long>? = null

    fun save(playback: PlaybackState) {
        val lessonId = playback.lessonId ?: return
        if (playback.durationMs <= 0 && playback.positionMs == 0L) return
        val ended = playback.durationMs > 0 && playback.positionMs >= playback.durationMs
        val position = if (ended) 0L else playback.positionMs
        val previous = lastSaved
        if (previous?.first == lessonId && previous.second == playback.track && !ended && abs(previous.third - position) < 1_000) return
        lastSaved = Triple(lessonId, playback.track, position)
        scope.launch { repository.savePosition(lessonId, playback.track, position) }
    }
}

internal object NoOpProgressRepository : ProgressRepository {
    override suspend fun positionMs(lessonId: String, track: String) = 0L
    override suspend fun savePosition(lessonId: String, track: String, positionMs: Long) = Unit
}
