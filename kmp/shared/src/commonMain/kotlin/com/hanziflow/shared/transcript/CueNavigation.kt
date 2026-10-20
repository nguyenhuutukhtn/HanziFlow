package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue

/**
 * Headset / transport cue navigation over one track's timeline. Cues are assumed
 * sorted ascending by `start_ms`, as published. Pure logic so JVM and JS can both
 * use the same rules; the Android service owns loading the timeline and seeking.
 */
object CueNavigation {
    /** "Previous" inside this window from the cue start jumps to the previous cue. */
    const val PREVIOUS_REPLAY_WINDOW_MS = 1_500L

    /**
     * Start of the next transcript cue after [positionMs]. Inside a cue this is the
     * following cue; in a gap it is the next cue that starts later. Null when there
     * is no next cue (or no timeline at all).
     */
    fun nextCueStartMs(cues: List<TimelineCue>, positionMs: Long): Long? {
        if (cues.isEmpty()) return null
        val activeIndex = TranscriptCues.activeCueIndex(cues, positionMs)
        if (activeIndex != null) return cues.getOrNull(activeIndex + 1)?.startMs
        return cues.firstOrNull { it.startMs > positionMs }?.startMs
    }

    /**
     * Start of the current cue, or the previous cue when playback is still within
     * [PREVIOUS_REPLAY_WINDOW_MS] of the current cue's start. In a gap this is the
     * last cue that started before [positionMs]. Null only without a timeline.
     */
    fun previousCueStartMs(cues: List<TimelineCue>, positionMs: Long): Long? {
        if (cues.isEmpty()) return null
        val activeIndex = TranscriptCues.activeCueIndex(cues, positionMs)
        if (activeIndex != null) {
            val current = cues[activeIndex]
            val withinReplayWindow = positionMs - current.startMs < PREVIOUS_REPLAY_WINDOW_MS
            return if (withinReplayWindow) {
                cues.getOrNull(activeIndex - 1)?.startMs ?: current.startMs
            } else {
                current.startMs
            }
        }
        return cues.lastOrNull { it.startMs < positionMs }?.startMs ?: cues.first().startMs
    }
}
