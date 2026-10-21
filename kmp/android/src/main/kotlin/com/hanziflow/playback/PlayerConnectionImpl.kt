package com.hanziflow.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import androidx.core.os.bundleOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class PlayerConnectionImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlayerConnection {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var clipPlayer: ExoPlayer? = null

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, token)
            .setListener(object : MediaController.Listener {
                override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
                    applyLoopExtras(extras)
                }
            })
            .buildAsync()
        controllerFuture.addListener({
            runCatching { controllerFuture.get() }.onSuccess { connected ->
                controller = connected
                connected.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) = refresh()
                })
                // The service is the single source of truth for the loop: read whatever
                // it is already doing rather than assuming "no loop" for a fresh connection.
                applyLoopExtras(connected.sessionExtras)
                refresh()
            }
        }, ContextCompat.getMainExecutor(context))
        scope.launch {
            while (true) {
                delay(500)
                refresh()
            }
        }
    }

    override fun play(lessonId: String, track: String, uri: String, title: String) {
        controller?.apply {
            setMediaItem(
                PlaybackContract.mediaItem(
                    lessonId = lessonId,
                    track = track,
                    uri = uri,
                    title = title,
                    trackLabel = context.getString(PlaybackContract.trackLabelRes(track)),
                ),
            )
            prepare()
            play()
        }
    }

    override fun toggle() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    override fun seekBack() { controller?.seekBack() }
    override fun seekForward() { controller?.seekForward() }
    override fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    override fun setSpeed(speed: Float) { controller?.setPlaybackSpeed(speed) }

    override fun loopSegment(lineId: String, startMs: Long, endMs: Long) {
        sendCommand(
            PlaybackContract.COMMAND_LOOP_SEGMENT,
            bundleOf(
                PlaybackContract.EXTRA_LINE_ID to lineId,
                PlaybackContract.EXTRA_START_MS to startMs,
                PlaybackContract.EXTRA_END_MS to endMs,
            ),
        )
    }

    override fun loopSection(sectionId: String, title: String, startMs: Long, endMs: Long) {
        sendCommand(
            PlaybackContract.COMMAND_LOOP_SECTION,
            bundleOf(
                PlaybackContract.EXTRA_LOOP_SECTION_ID to sectionId,
                PlaybackContract.EXTRA_LOOP_SECTION_TITLE to title,
                PlaybackContract.EXTRA_START_MS to startMs,
                PlaybackContract.EXTRA_END_MS to endMs,
            ),
        )
    }

    override fun stopSectionLoopAfterPass() = sendCommand(PlaybackContract.COMMAND_SECTION_STOP_AFTER_PASS)
    override fun nextSection() = sendCommand(PlaybackContract.COMMAND_SECTION_NEXT)
    override fun clearLoop() = sendCommand(PlaybackContract.COMMAND_CLEAR_LOOP)

    override fun switchTrack(track: String) =
        sendCommand(PlaybackContract.COMMAND_SWITCH_TRACK, bundleOf(PlaybackContract.EXTRA_TRACK to track))

    override fun playClip(uri: String) {
        controller?.pause()
        val player = clipPlayer ?: ExoPlayer.Builder(context).build().also { clipPlayer = it }
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    // No optimistic local update: the service publishes the new state via session
    // extras, picked up by onExtrasChanged.
    private fun sendCommand(action: String, args: Bundle = Bundle.EMPTY) {
        controller?.sendCustomCommand(SessionCommand(action, Bundle.EMPTY), args)
    }

    private fun refresh() {
        val current = controller ?: return
        val parts = current.currentMediaItem?.mediaId?.split('|', limit = 2).orEmpty()
        _state.value = _state.value.copy(
            connected = true,
            playing = current.isPlaying,
            lessonId = parts.getOrNull(0),
            track = parts.getOrNull(1) ?: "guided",
            positionMs = current.currentPosition.coerceAtLeast(0),
            durationMs = current.duration.coerceAtLeast(0),
            speed = current.playbackParameters.speed,
        )
    }

    /** Applies the service's published loop/Chinese-only state; never set these any other way. */
    private fun applyLoopExtras(extras: Bundle) {
        val sectionId = extras.getString(PlaybackContract.EXTRA_LOOP_SECTION_ID)
        _state.value = _state.value.copy(
            loopedLineId = extras.getString(PlaybackContract.EXTRA_LOOP_LINE_ID),
            sectionLoop = sectionId?.let {
                SectionLoopInfo(
                    sectionId = it,
                    title = extras.getString(PlaybackContract.EXTRA_LOOP_SECTION_TITLE).orEmpty(),
                    pass = extras.getInt(PlaybackContract.EXTRA_LOOP_SECTION_PASS, 1),
                    stopAfterPass = extras.getBoolean(PlaybackContract.EXTRA_LOOP_SECTION_STOP_AFTER_PASS),
                )
            },
        )
    }
}
