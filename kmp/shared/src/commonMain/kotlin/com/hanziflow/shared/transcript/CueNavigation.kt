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
     * is no next cue (or no timeline at all). [skipTarget] (e.g. Chinese-only) can
     * redirect a candidate that lands inside a skipped range; return null from it to
     * keep the candidate as-is.
     */
    fun nextCueStartMs(
        cues: List<TimelineCue>,
        positionMs: Long,
        skipTarget: (Long) -> Long? = { null },
    ): Long? {
        if (cues.isEmpty()) return null
        val activeIndex = TranscriptCues.activeCueIndex(cues, positionMs)
        val candidate = if (activeIndex != null) {
            cues.getOrNull(activeIndex + 1)?.startMs
        } else {
            cues.firstOrNull { it.startMs > positionMs }?.startMs
        }
        return candidate?.let { skipTarget(it) ?: it }
    }

    /**
     * Start of the current cue, or the previous cue when playback is still within
     * [PREVIOUS_REPLAY_WINDOW_MS] of the current cue's start. In a gap this is the
     * last cue that started before [positionMs]. Null only without a timeline.
     */
    fun previousCueStartMs(
        cues: List<TimelineCue>,
        positionMs: Long,
        skipTarget: (Long) -> Long? = { null },
    ): Long? {
        if (cues.isEmpty()) return null
        val activeIndex = TranscriptCues.activeCueIndex(cues, positionMs)
        val candidate = if (activeIndex != null) {
            val current = cues[activeIndex]
            val withinReplayWindow = positionMs - current.startMs < PREVIOUS_REPLAY_WINDOW_MS
            if (withinReplayWindow) {
                cues.getOrNull(activeIndex - 1)?.startMs ?: current.startMs
            } else {
                current.startMs
            }
        } else {
            cues.lastOrNull { it.startMs < positionMs }?.startMs ?: cues.first().startMs
        }
        return skipTarget(candidate) ?: candidate
    }
}
