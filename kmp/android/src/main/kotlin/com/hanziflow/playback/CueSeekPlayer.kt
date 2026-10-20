package com.hanziflow.playback

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * Makes next/previous available on the session (notification, lock screen, media
 * buttons) even though the app plays one media item. The service resolves a
 * transcript cue when either command is used.
 */
@OptIn(markerClass = [UnstableApi::class])
class CueSeekPlayer(
    private val wrapped: Player,
    private val onNextCue: () -> Unit,
    private val onPreviousCue: () -> Unit,
) : ForwardingPlayer(wrapped) {
    override fun getAvailableCommands(): Player.Commands =
        super.getAvailableCommands().buildUpon()
            .addAll(
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            )
            .build()

    override fun isCommandAvailable(command: Int): Boolean = when (command) {
        Player.COMMAND_SEEK_TO_NEXT,
        Player.COMMAND_SEEK_TO_PREVIOUS,
        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        -> true
        else -> super.isCommandAvailable(command)
    }

    override fun hasNextMediaItem(): Boolean = wrapped.mediaItemCount > 0 || super.hasNextMediaItem()

    override fun hasPreviousMediaItem(): Boolean = wrapped.mediaItemCount > 0 || super.hasPreviousMediaItem()

    override fun seekToNext() = onNextCue()

    override fun seekToPrevious() = onPreviousCue()

    override fun seekToNextMediaItem() = onNextCue()

    override fun seekToPreviousMediaItem() = onPreviousCue()
}
