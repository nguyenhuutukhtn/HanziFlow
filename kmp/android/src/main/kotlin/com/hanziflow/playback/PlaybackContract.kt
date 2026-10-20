package com.hanziflow.playback

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hanziflow.R

/** Session command/extra keys shared by [PlaybackService] and [PlayerConnectionImpl], and the MediaItem factory both use. */
object PlaybackContract {
    const val COMMAND_LOOP_SEGMENT = "com.hanziflow.LOOP_SEGMENT"
    const val COMMAND_LOOP_SECTION = "com.hanziflow.LOOP_SECTION"
    const val COMMAND_SECTION_STOP_AFTER_PASS = "com.hanziflow.SECTION_STOP_AFTER_PASS"
    const val COMMAND_SECTION_NEXT = "com.hanziflow.SECTION_NEXT"
    const val COMMAND_CLEAR_LOOP = "com.hanziflow.CLEAR_LOOP"
    const val COMMAND_SET_CHINESE_ONLY = "com.hanziflow.SET_CHINESE_ONLY"

    const val EXTRA_LINE_ID = "line_id"
    const val EXTRA_START_MS = "start_ms"
    const val EXTRA_END_MS = "end_ms"
    /** Session-extras keys, as published by [PlaybackExtras.build]. Absent when that loop/flag isn't active. */
    const val EXTRA_LOOP_LINE_ID = "loop_line_id"
    const val EXTRA_LOOP_SECTION_ID = "loop_section_id"
    const val EXTRA_LOOP_SECTION_TITLE = "loop_section_title"
    const val EXTRA_LOOP_SECTION_PASS = "loop_section_pass"
    const val EXTRA_LOOP_SECTION_STOP_AFTER_PASS = "loop_section_stop_after_pass"
    const val EXTRA_CHINESE_ONLY = "chinese_only"

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
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(trackLabel).build())
            .build()

    data class LineLoopArgs(val lineId: String, val startMs: Long, val endMs: Long)
    data class SectionLoopArgs(val sectionId: String, val title: String, val startMs: Long, val endMs: Long)

    fun lineLoopArgs(args: Bundle): LineLoopArgs? {
        val lineId = args.getString(EXTRA_LINE_ID) ?: return null
        return LineLoopArgs(lineId, args.getLong(EXTRA_START_MS), args.getLong(EXTRA_END_MS))
    }

    fun sectionLoopArgs(args: Bundle): SectionLoopArgs? {
        val sectionId = args.getString(EXTRA_LOOP_SECTION_ID) ?: return null
        return SectionLoopArgs(
            sectionId,
            args.getString(EXTRA_LOOP_SECTION_TITLE).orEmpty(),
            args.getLong(EXTRA_START_MS),
            args.getLong(EXTRA_END_MS),
        )
    }
}
