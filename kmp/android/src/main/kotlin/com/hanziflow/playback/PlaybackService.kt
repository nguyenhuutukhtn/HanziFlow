package com.hanziflow.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.hanziflow.MainActivity
import com.hanziflow.data.CourseDatabase
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.SettingsRepository
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.TrackSwitch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** MediaSession wiring only: loop/skip/resumption logic lives in [PlaybackCoordinator] & friends. */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var courseRepository: CourseRepository
    @Inject lateinit var courseDatabase: CourseDatabase
    @Inject lateinit var settingsRepository: SettingsRepository
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaSession
    private lateinit var coordinator: PlaybackCoordinator
    private lateinit var resumptionStore: ResumptionStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cueSkipper by lazy { CueSkipper(courseRepository) }
    private val loopController = LoopController()

    override fun onCreate() {
        super.onCreate()
        resumptionStore = ResumptionStore(this, courseRepository, courseDatabase)
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
        coordinator = PlaybackCoordinator(
            player, cueSkipper, loopController, resumptionStore, scope, persistenceScope, ::publishState, ::resumeTarget,
        )
        val activityIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val cuePlayer = CueSeekPlayer(
            wrapped = player,
            onNextCue = { coordinator.skipCue(forward = true) },
            onPreviousCue = { coordinator.skipCue(forward = false) },
        )
        session = MediaSession.Builder(this, cuePlayer)
            .setCallback(sessionCallback)
            .setSessionActivity(activityIntent)
            .build()
        player.addListener(coordinator.playerListener)
        coordinator.restoreAsynchronously()
        coordinator.start()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    private fun publishState() {
        session.setSessionExtras(PlaybackExtras.build(loopController.current))
        session.setCustomLayout(PlaybackExtras.customLayout(loopController.current))
    }

    private val sessionCallback = object : MediaSession.Callback {
        @UnstableApi
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(SessionCommand(PlaybackContract.COMMAND_LOOP_SEGMENT, Bundle.EMPTY))
                .add(SessionCommand(PlaybackContract.COMMAND_LOOP_SECTION, Bundle.EMPTY))
                .add(SessionCommand(PlaybackContract.COMMAND_SECTION_STOP_AFTER_PASS, Bundle.EMPTY))
                .add(SessionCommand(PlaybackContract.COMMAND_SECTION_NEXT, Bundle.EMPTY))
                .add(SessionCommand(PlaybackContract.COMMAND_CLEAR_LOOP, Bundle.EMPTY))
                .add(SessionCommand(PlaybackContract.COMMAND_SWITCH_TRACK, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        @UnstableApi
        override fun onMediaButtonEvent(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val event = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            // Consume both down and up so the default handler can't also move to the (nonexistent) next media item.
            if (event?.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || event?.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    coordinator.skipCue(forward = event.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
                }
                return true
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
                PlaybackContract.COMMAND_LOOP_SEGMENT -> {
                    val loop = PlaybackContract.lineLoopArgs(args)
                        ?: return Futures.immediateFuture(SessionResult(SessionError.ERROR_BAD_VALUE))
                    loopController.startLineLoop(loop.lineId, loop.startMs, loop.endMs)
                    publishState()
                }
                PlaybackContract.COMMAND_LOOP_SECTION -> {
                    val section = PlaybackContract.sectionLoopArgs(args)
                        ?: return Futures.immediateFuture(SessionResult(SessionError.ERROR_BAD_VALUE))
                    loopController.startSectionLoop(
                        TimelineSection(section.sectionId, section.title, section.startMs, section.endMs),
                    )
                    publishState()
                }
                PlaybackContract.COMMAND_SECTION_STOP_AFTER_PASS -> {
                    loopController.stopSectionLoopAfterPass()
                    publishState()
                }
                PlaybackContract.COMMAND_SECTION_NEXT -> coordinator.nextSectionNow()
                PlaybackContract.COMMAND_CLEAR_LOOP -> {
                    loopController.clear()
                    publishState()
                }
                PlaybackContract.COMMAND_SWITCH_TRACK -> switchTrack(args.getString(PlaybackContract.EXTRA_TRACK).orEmpty())
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
                    val restored = resumptionStore.load() ?: error("No downloaded playback to resume")
                    val target = resumeTarget(restored)
                    player.setPlaybackSpeed(restored.saved.speed.coerceIn(0.8f, 1.1f))
                    MediaSession.MediaItemsWithStartPosition(listOf(target.first), 0, target.second)
                }.onSuccess(result::set).onFailure(result::setException)
            }
            return result
        }
    }

    private fun switchTrack(requestedTrack: String) {
        scope.launch {
            val current = player.currentMediaItem ?: return@launch
            val parts = current.mediaId.split('|', limit = 2)
            if (parts.size != 2 || requestedTrack !in setOf("guided", "guided_zh") || parts[1] !in setOf("guided", "guided_zh")) return@launch
            val target = requestedTrack
            if (parts[1] == target) return@launch
            val item = courseRepository.trackFile(parts[0], target) ?: return@launch
            val sourceTimeline = courseRepository.timelineFile(parts[0], parts[1]) ?: return@launch
            val targetTimeline = courseRepository.timelineFile(parts[0], target) ?: return@launch
            val source = CourseJson.timeline(sourceTimeline.readText())
            val destination = CourseJson.timeline(targetTimeline.readText())
            val position = TrackSwitch.positionFor(source.cues, destination.cues, player.currentPosition)
            settingsRepository.setChineseOnlyEnabled(parts[0], "guided", target == "guided_zh")
            val wasPlaying = player.playWhenReady
            val speed = player.playbackParameters.speed
            loopController.clear()
            player.setMediaItem(
                PlaybackContract.mediaItem(
                    parts[0], target, item.toURI().toString(),
                    player.mediaMetadata.title?.toString() ?: parts[0],
                    getString(PlaybackContract.trackLabelRes(target)),
                ),
            )
            player.setPlaybackSpeed(speed)
            player.prepare()
            player.seekTo(position)
            player.playWhenReady = wasPlaying
            publishState()
        }
    }

    private suspend fun resumeTarget(resumption: ResumptionStore.Resumption): Pair<MediaItem, Long> {
        val saved = resumption.saved
        val enabled = settingsRepository.chineseOnlyEnabled(saved.lessonId, "guided")
        val targetTrack = when {
            saved.track == "guided" && enabled -> "guided_zh"
            saved.track == "guided_zh" && !enabled -> "guided"
            else -> saved.track
        }
        if (targetTrack == saved.track) return resumption.mediaItem() to saved.positionMs
        val sourceFile = courseRepository.timelineFile(saved.lessonId, saved.track) ?: return resumption.mediaItem() to saved.positionMs
        val targetFile = courseRepository.timelineFile(saved.lessonId, targetTrack) ?: return resumption.mediaItem() to saved.positionMs
        val trackFile = courseRepository.trackFile(saved.lessonId, targetTrack) ?: return resumption.mediaItem() to saved.positionMs
        val source = CourseJson.timeline(sourceFile.readText())
        val target = CourseJson.timeline(targetFile.readText())
        val position = TrackSwitch.positionFor(source.cues, target.cues, saved.positionMs)
        val item = PlaybackContract.mediaItem(
            saved.lessonId, targetTrack, trackFile.toURI().toString(), resumption.title,
            getString(PlaybackContract.trackLabelRes(targetTrack)),
        )
        return item to position
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        coordinator.saveNow()
    }

    override fun onDestroy() {
        val finalSnapshot = coordinator.stop()
        session.release()
        player.release()
        scope.cancel()
        if (finalSnapshot != null) {
            persistenceScope.launch {
                try { resumptionStore.persist(finalSnapshot) } finally { persistenceScope.cancel() }
            }
        } else {
            persistenceScope.cancel()
        }
        super.onDestroy()
    }
}
