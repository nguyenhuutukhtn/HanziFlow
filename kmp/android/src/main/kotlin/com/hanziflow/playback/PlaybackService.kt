package com.hanziflow.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.hanziflow.MainActivity
import com.hanziflow.R
import com.hanziflow.data.CourseDatabase
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.PlaybackEntity
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.transcript.CueNavigation
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var courseRepository: CourseRepository
    @Inject lateinit var courseDatabase: CourseDatabase
    private lateinit var player: ExoPlayer
    private lateinit var cuePlayer: CueSeekPlayer
    private lateinit var session: MediaSession
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var saveJob: Job? = null
    private var cueCache: CueCache? = null

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
        cuePlayer = CueSeekPlayer(
            wrapped = player,
            onNextCue = { skipCue(forward = true) },
            onPreviousCue = { skipCue(forward = false) },
        )
        session = MediaSession.Builder(this, cuePlayer)
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
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    clearLoop()
                    cueCache = null
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_IDLE) clearLoop()
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

    private val loopPublisher = LoopPublisher()
    private var loopJob: Job? = null
    private var ignoreNextSeekDiscontinuity = false

    private fun setLoop(lineId: String, startMs: Long, endMs: Long) {
        loopPublisher.start(lineId, startMs, endMs)
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                delay(LOOP_POLL_MS)
                val loop = loopPublisher.current ?: break
                if (player.currentPosition >= loop.endMs) {
                    ignoreNextSeekDiscontinuity = true
                    player.seekTo(loop.startMs)
                }
            }
        }
        publishLoopState()
    }

    private fun clearLoop() {
        if (loopPublisher.current == null) return
        loopPublisher.clear()
        loopJob?.cancel()
        loopJob = null
        publishLoopState()
    }

    /**
     * The service is the single source of truth for the loop: every connected or
     * reconnecting controller reads it from here (session extras + a notification
     * action), never from its own memory, so a recreated UI can't lose track of an
     * active loop.
     */
    private fun publishLoopState() {
        val loop = loopPublisher.current
        val extras = Bundle()
        if (loop != null) {
            extras.putString(EXTRA_LOOP_LINE_ID, loop.lineId)
            extras.putLong(EXTRA_START_MS, loop.startMs)
            extras.putLong(EXTRA_END_MS, loop.endMs)
        }
        session.setSessionExtras(extras)
        session.setCustomLayout(
            if (loop == null) emptyList() else listOf(
                CommandButton.Builder(CommandButton.ICON_REPEAT_OFF)
                    .setSessionCommand(SessionCommand(COMMAND_CLEAR_LOOP, Bundle.EMPTY))
                    .setDisplayName("Stop looping")
                    .build()
            )
        )
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
                session.player.availableCommands,
            )
        }

        @UnstableApi
        override fun onMediaButtonEvent(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val event = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            if (event != null) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_NEXT,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                    -> {
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            skipCue(forward = event.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
                        }
                        // Consume both down and up so the default handler can't move
                        // to the (nonexistent) next media item as well.
                        return true
                    }
                }
            }
            return super.onMediaButtonEvent(session, controller, intent)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                COMMAND_LOOP_SEGMENT -> {
                    val lineId = args.getString(EXTRA_LINE_ID) ?: return Futures.immediateFuture(
                        SessionResult(SessionError.ERROR_BAD_VALUE)
                    )
                    setLoop(lineId, args.getLong(EXTRA_START_MS), args.getLong(EXTRA_END_MS))
                }
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
        val saved = courseDatabase.dao().playback() ?: return@withContext null
        val file = courseRepository.trackFile(saved.lessonId, saved.track) ?: return@withContext null
        val title = courseDatabase.dao().lesson(saved.lessonId)?.title ?: saved.lessonId
        Resumption(saved, file, title, getString(trackLabelRes(saved.track)))
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
            courseDatabase.dao().putPlayback(snapshot)
        }
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

    /**
     * Headset / lock-screen next and previous. The service reads the current track's
     * timeline itself, so this works with the screen off and the UI process gone.
     */
    private fun skipCue(forward: Boolean) {
        val positionAtPress = player.currentPosition.coerceAtLeast(0)
        val ids = currentMediaIds()
        if (ids == null) {
            skipFixed(forward)
            return
        }
        scope.launch {
            val cues = cuesFor(ids.first, ids.second)
            val target = if (forward) {
                CueNavigation.nextCueStartMs(cues, positionAtPress)
            } else {
                CueNavigation.previousCueStartMs(cues, positionAtPress)
            }
            if (target != null) player.seekTo(target) else skipFixed(forward)
        }
    }

    private fun skipFixed(forward: Boolean) {
        if (forward) player.seekForward() else player.seekBack()
    }

    private fun currentMediaIds(): Pair<String, String>? {
        val parts = player.currentMediaItem?.mediaId?.split('|', limit = 2) ?: return null
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    /** One read per lesson/track; the cache is dropped on a media-item transition. */
    private suspend fun cuesFor(lessonId: String, track: String): List<TimelineCue> {
        cueCache?.takeIf { it.lessonId == lessonId && it.track == track }?.let { return it.cues }
        return withContext(Dispatchers.IO) {
            val cues = courseRepository.timelineFile(lessonId, track)
                ?.let { file -> runCatching { CourseJson.timeline(file.readText()).cues }.getOrNull() }
                .orEmpty()
            cueCache = CueCache(lessonId, track, cues)
            cues
        }
    }

    private data class CueCache(
        val lessonId: String,
        val track: String,
        val cues: List<TimelineCue>,
    )

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
                    courseDatabase.dao().putPlayback(finalSnapshot)
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
        val trackLabel: String,
    ) {
        fun mediaItem(): MediaItem = mediaItem(
            lessonId = saved.lessonId,
            track = saved.track,
            uri = file.toURI().toString(),
            title = title,
            trackLabel = trackLabel,
        )
    }

    companion object {
        const val COMMAND_LOOP_SEGMENT = "com.hanziflow.LOOP_SEGMENT"
        const val COMMAND_CLEAR_LOOP = "com.hanziflow.CLEAR_LOOP"
        const val EXTRA_LINE_ID = "line_id"
        const val EXTRA_START_MS = "start_ms"
        const val EXTRA_END_MS = "end_ms"
        /** Session-extras key: the looped line, as published by [publishLoopState]. Absent when no loop is active. */
        const val EXTRA_LOOP_LINE_ID = "loop_line_id"
        private const val LOOP_POLL_MS = 150L

        /** Display names only; persisted ids stay `guided` / `practice`. Used by UI and the lock screen. */
        fun trackLabelRes(track: String): Int =
            if (track == "practice") R.string.track_practice else R.string.track_guided

        fun mediaItem(
            lessonId: String,
            track: String,
            uri: String,
            title: String = lessonId,
            trackLabel: String = track,
        ): MediaItem =
            MediaItem.Builder()
                .setMediaId("$lessonId|$track")
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(trackLabel)
                        .build()
                )
                .build()
    }
}
