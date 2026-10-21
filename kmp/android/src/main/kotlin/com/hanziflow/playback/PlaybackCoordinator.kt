package com.hanziflow.playback

import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.hanziflow.data.PlaybackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Everything [PlaybackService] does with ExoPlayer that isn't MediaSession wiring:
 * the position-check tick (loop repeat/release), headset cue
 * navigation, resumption restore/save. Runs on the service's own scope, so it keeps
 * working with the screen off and the UI closed.
 */
class PlaybackCoordinator(
    private val player: ExoPlayer,
    private val cueSkipper: CueSkipper,
    private val loopController: LoopController,
    private val resumptionStore: ResumptionStore,
    private val scope: CoroutineScope,
    private val persistenceScope: CoroutineScope,
    private val onStateChanged: () -> Unit,
    private val resumeTarget: suspend (ResumptionStore.Resumption) -> Pair<MediaItem, Long> =
        { it.mediaItem() to it.saved.positionMs },
) {
    private var saveJob: Job? = null
    private var tickerJob: Job? = null
    private var ignoreNextSeekDiscontinuity = false

    val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                )
            ) {
                save()
            }
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) schedulePeriodicSave()
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                loopController.clear()
                cueSkipper.onMediaItemChanged()
                onStateChanged()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_IDLE) {
                loopController.clear()
                onStateChanged()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            if (reason != Player.DISCONTINUITY_REASON_SEEK) return
            if (ignoreNextSeekDiscontinuity) {
                ignoreNextSeekDiscontinuity = false
            } else {
                loopController.clear()
                onStateChanged()
            }
        }
    }

    /**
     * 150ms position-check tick for loop repeat/release and resumption persistence.
     */
    fun start() {
        tickerJob = scope.launch {
            while (isActive) {
                delay(TICK_MS)
                val positionMs = player.currentPosition
                val loopTarget = loopController.onPositionReached(positionMs)
                loopTarget?.let(::seekIgnoringLoop)
                if (loopTarget != null) onStateChanged()
            }
        }
    }

    fun restoreAsynchronously() {
        scope.launch {
            val restored = resumptionStore.load() ?: return@launch
            if (player.mediaItemCount == 0) {
                val target = resumeTarget(restored)
                player.setMediaItem(target.first)
                player.setPlaybackSpeed(restored.saved.speed.coerceIn(0.8f, 1.1f))
                player.prepare()
                player.seekTo(target.second)
            }
        }
    }

    /** Headset / lock-screen next and previous. */
    fun skipCue(forward: Boolean) {
        val positionAtPress = player.currentPosition.coerceAtLeast(0)
        val ids = currentMediaIds()
        if (ids == null) {
            skipFixed(forward)
            return
        }
        scope.launch {
            val target = if (forward) {
                cueSkipper.nextTarget(ids.first, ids.second, positionAtPress)
            } else {
                cueSkipper.previousTarget(ids.first, ids.second, positionAtPress)
            }
            if (target != null) player.seekTo(target) else skipFixed(forward)
        }
    }

    /** "Sang phần sau": clear the section loop (if any) and jump past it now. */
    fun nextSectionNow() {
        scope.launch {
            val ids = currentMediaIds()
            val sections = if (ids != null) cueSkipper.sectionsFor(ids.first, ids.second) else emptyList()
            loopController.nextSectionNow(sections)?.let(::seekIgnoringLoop)
            onStateChanged()
        }
    }

    fun saveNow() = save()

    /** Cancels background jobs and returns the final snapshot to persist, if any. */
    fun stop(): PlaybackEntity? {
        saveJob?.cancel()
        tickerJob?.cancel()
        return snapshot()
    }

    private fun seekIgnoringLoop(target: Long) {
        ignoreNextSeekDiscontinuity = true
        player.seekTo(target)
    }

    private fun skipFixed(forward: Boolean) {
        if (forward) player.seekForward() else player.seekBack()
    }

    private fun currentMediaIds(): Pair<String, String>? {
        val parts = player.currentMediaItem?.mediaId?.split('|', limit = 2) ?: return null
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    private fun schedulePeriodicSave() {
        saveJob?.cancel()
        if (!player.isPlaying) return
        saveJob = scope.launch {
            while (true) {
                delay(5_000)
                save()
            }
        }
    }

    private fun save() {
        val entity = snapshot() ?: return
        persistenceScope.launch { resumptionStore.persist(entity) }
    }

    private fun snapshot(): PlaybackEntity? {
        val (lessonId, track) = currentMediaIds() ?: return null
        return PlaybackEntity(
            lessonId = lessonId,
            track = track,
            positionMs = player.currentPosition.coerceAtLeast(0),
            speed = player.playbackParameters.speed,
        )
    }

    private companion object {
        const val TICK_MS = 150L
    }
}
