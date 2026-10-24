package com.hanziflow.playback

/** Mirrors the service's [androidx.media3.session.MediaController] state, including loop (session extras). */
data class PlaybackState(
    val connected: Boolean = false,
    val playing: Boolean = false,
    val lessonId: String? = null,
    val track: String = "guided",
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
    val loopedLineId: String? = null,
    val sectionLoop: SectionLoopInfo? = null,
    val repeatEachEnabled: Boolean = false,
)

data class SectionLoopInfo(
    val sectionId: String,
    val title: String,
    val pass: Int,
    val stopAfterPass: Boolean,
)
