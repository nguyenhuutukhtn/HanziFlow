package com.hanziflow.playback

/** The active loop: a looped transcript line, or a looped section (up to 5 passes). */
sealed interface LoopState {
    data class Line(val lineId: String, val startMs: Long, val endMs: Long) : LoopState

    data class Section(
        val sectionId: String,
        val title: String,
        val startMs: Long,
        val endMs: Long,
        val pass: Int,
        val stopAfterPass: Boolean,
    ) : LoopState

    data class RepeatEach(val unitIndex: Int, val pass: Int) : LoopState
}

/**
 * The single source of truth for the active loop (line or section, never both).
 * [PlaybackService] is the only writer; every controller (including one reconnecting
 * after its UI was recreated) must read [current] instead of remembering its own copy.
 */
class LoopPublisher {
    var current: LoopState? = null
        private set

    fun startLine(lineId: String, startMs: Long, endMs: Long) {
        current = LoopState.Line(lineId, startMs, endMs)
    }

    fun startSection(sectionId: String, title: String, startMs: Long, endMs: Long) {
        current = LoopState.Section(sectionId, title, startMs, endMs, pass = 1, stopAfterPass = false)
    }

    fun updateSection(transform: (LoopState.Section) -> LoopState.Section) {
        (current as? LoopState.Section)?.let { current = transform(it) }
    }

    fun startRepeatEach(unitIndex: Int, pass: Int = 1) {
        current = LoopState.RepeatEach(unitIndex, pass)
    }

    fun updateRepeatEach(transform: (LoopState.RepeatEach) -> LoopState.RepeatEach) {
        (current as? LoopState.RepeatEach)?.let { current = transform(it) }
    }

    fun clear() {
        current = null
    }
}
