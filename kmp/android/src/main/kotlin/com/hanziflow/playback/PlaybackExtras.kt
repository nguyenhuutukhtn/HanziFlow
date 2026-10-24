package com.hanziflow.playback

import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand

/** Builds the session extras / custom layout [PlaybackService] publishes; split out to keep it under the line budget. */
object PlaybackExtras {
    fun build(loop: LoopState?, repeatEachEnabled: Boolean = loop is LoopState.RepeatEach): Bundle {
        val extras = Bundle()
        extras.putBoolean(PlaybackContract.EXTRA_REPEAT_EACH_ENABLED, repeatEachEnabled)
        when (loop) {
            is LoopState.Line -> {
                extras.putString(PlaybackContract.EXTRA_LOOP_LINE_ID, loop.lineId)
                extras.putLong(PlaybackContract.EXTRA_START_MS, loop.startMs)
                extras.putLong(PlaybackContract.EXTRA_END_MS, loop.endMs)
            }
            is LoopState.Section -> {
                extras.putString(PlaybackContract.EXTRA_LOOP_SECTION_ID, loop.sectionId)
                extras.putString(PlaybackContract.EXTRA_LOOP_SECTION_TITLE, loop.title)
                extras.putInt(PlaybackContract.EXTRA_LOOP_SECTION_PASS, loop.pass)
                extras.putBoolean(PlaybackContract.EXTRA_LOOP_SECTION_STOP_AFTER_PASS, loop.stopAfterPass)
            }
            is LoopState.RepeatEach -> extras.putBoolean(PlaybackContract.EXTRA_REPEAT_EACH_ENABLED, true)
            null -> Unit
        }
        return extras
    }

    fun customLayout(loop: LoopState?): List<CommandButton> =
        if (loop == null || loop is LoopState.RepeatEach) {
            emptyList()
        } else {
            listOf(
                CommandButton.Builder(CommandButton.ICON_REPEAT_OFF)
                    .setSessionCommand(SessionCommand(PlaybackContract.COMMAND_CLEAR_LOOP, Bundle.EMPTY))
                    .setDisplayName("Stop looping")
                    .build(),
            )
        }
}
