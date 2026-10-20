package com.hanziflow.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.hanziflow.HanziFlowApplication
import com.hanziflow.MainActivity
import com.hanziflow.data.PlaybackEntity
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaSession
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var saveJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
        val activityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .setSessionActivity(activityIntent)
            .build()
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_POSITION_DISCONTINUITY,
                        Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    )
                ) save()
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) schedulePeriodicSave()
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) clearLoop()
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
                    clearLoop()
                }
            }
        })
        restoreAsynchronously()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    private var loopRange: LongRange? = null
    private var loopJob: Job? = null
    private var ignoreNextSeekDiscontinuity = false

    private fun setLoop(startMs: Long, endMs: Long) {
        loopRange = startMs until endMs
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                delay(LOOP_POLL_MS)
                val range = loopRange ?: break
                if (player.currentPosition >= range.last + 1) {
                    ignoreNextSeekDiscontinuity = true
                    player.seekTo(range.first)
                }
            }
        }
    }

    private fun clearLoop() {
        loopRange = null
        loopJob?.cancel()
        loopJob = null
    }

    private val sessionCallback = object : MediaSession.Callback {
        @UnstableApi
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(SessionCommand(COMMAND_LOOP_SEGMENT, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_CLEAR_LOOP, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(
                sessionCommands,
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS,
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                COMMAND_LOOP_SEGMENT -> setLoop(args.getLong(EXTRA_START_MS), args.getLong(EXTRA_END_MS))
                COMMAND_CLEAR_LOOP -> clearLoop()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        @UnstableApi
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            isForPlayback: Boolean,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val result = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            scope.launch {
                runCatching {
                    val restored = loadResumption() ?: error("No downloaded playback to resume")
                    player.setPlaybackSpeed(restored.saved.speed.coerceIn(0.8f, 1.1f))
                    MediaSession.MediaItemsWithStartPosition(
                        listOf(restored.mediaItem()),
                        0,
                        restored.saved.positionMs,
                    )
                }.onSuccess(result::set).onFailure(result::setException)
            }
            return result
        }
    }

    private fun restoreAsynchronously() {
        scope.launch {
            val restored = loadResumption() ?: return@launch
            if (player.mediaItemCount == 0) {
                player.setMediaItem(restored.mediaItem())
                player.setPlaybackSpeed(restored.saved.speed.coerceIn(0.8f, 1.1f))
                player.prepare()
                player.seekTo(restored.saved.positionMs)
            }
        }
    }

    private suspend fun loadResumption(): Resumption? = withContext(Dispatchers.IO) {
        val app = application as HanziFlowApplication
        val saved = app.database.dao().playback() ?: return@withContext null
        val file = app.repository.trackFile(saved.lessonId, saved.track) ?: return@withContext null
        val title = app.database.dao().lesson(saved.lessonId)?.title ?: saved.lessonId
        Resumption(saved, file, title)
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
        val snapshot = snapshot() ?: return
        persistenceScope.launch {
            (application as HanziFlowApplication).database.dao().putPlayback(snapshot)
        }
    }

    private fun snapshot(): PlaybackEntity? {
        val parts = player.currentMediaItem?.mediaId?.split('|', limit = 2) ?: return null
        if (parts.size != 2) return null
        return PlaybackEntity(
            lessonId = parts[0],
            track = parts[1],
            positionMs = player.currentPosition.coerceAtLeast(0),
            speed = player.playbackParameters.speed,
        )
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        save()
    }

    override fun onDestroy() {
        val finalSnapshot = snapshot()
        saveJob?.cancel()
        session.release()
        player.release()
        scope.cancel()
        if (finalSnapshot == null) {
            persistenceScope.cancel()
        } else {
            persistenceScope.launch {
                try {
                    (application as HanziFlowApplication).database.dao().putPlayback(finalSnapshot)
                } finally {
                    persistenceScope.cancel()
                }
            }
        }
        super.onDestroy()
    }

    private data class Resumption(
        val saved: PlaybackEntity,
        val file: File,
        val title: String,
    ) {
        fun mediaItem(): MediaItem = mediaItem(
            lessonId = saved.lessonId,
            track = saved.track,
            uri = file.toURI().toString(),
            title = title,
        )
    }

    companion object {
        const val COMMAND_LOOP_SEGMENT = "com.hanziflow.LOOP_SEGMENT"
        const val COMMAND_CLEAR_LOOP = "com.hanziflow.CLEAR_LOOP"
        const val EXTRA_START_MS = "start_ms"
        const val EXTRA_END_MS = "end_ms"
        private const val LOOP_POLL_MS = 150L

        fun mediaItem(lessonId: String, track: String, uri: String, title: String = lessonId): MediaItem =
            MediaItem.Builder()
                .setMediaId("$lessonId|$track")
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(if (track == "guided") "Guided" else "Practice")
                        .build()
                )
                .build()
    }
}
