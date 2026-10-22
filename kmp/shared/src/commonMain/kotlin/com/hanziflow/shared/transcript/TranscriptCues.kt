package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue

/**
 * Pure cue-lookup logic over a track's timeline cues (assumed sorted ascending by
 * start_ms, as published). No I/O, platform APIs, or player state here.
 */
object TranscriptCues {
    data class CuePlace(val speaking: Int?, val lastStarted: Int?, val next: Int?)

    /** Finds the speaking, last-started, and next cue with binary searches over start times. */
    fun placeAt(cues: List<TimelineCue>, positionMs: Long): CuePlace {
        if (cues.isEmpty()) return CuePlace(null, null, null)

        var low = 0
        var high = cues.size
        while (low < high) {
            val mid = (low + high) / 2
            if (cues[mid].startMs <= positionMs) low = mid + 1 else high = mid
        }
        val next = low.takeIf { it < cues.size }
        val lastStarted = (low - 1).takeIf { it >= 0 }
        val speaking = lastStarted?.takeIf { positionMs < cues[it].endMs }
        return CuePlace(speaking, lastStarted, next)
    }

    /**
     * Index of the cue active at [positionMs] (cue.startMs <= positionMs < cue.endMs),
     * found by binary search. Null in a gap between cues, before the first cue, or
     * after the last one.
     */
    fun activeCueIndex(cues: List<TimelineCue>, positionMs: Long): Int? {
        if (cues.isEmpty()) return null
        var low = 0
        var high = cues.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val cue = cues[mid]
            when {
                positionMs < cue.startMs -> high = mid - 1
                positionMs >= cue.endMs -> low = mid + 1
                else -> return mid
            }
        }
        return null
    }

    fun activeCue(cues: List<TimelineCue>, positionMs: Long): TimelineCue? =
        activeCueIndex(cues, positionMs)?.let(cues::get)

    /** Lines to highlight for a cue: its own line, plus every line in a grouped cue's line_ids. */
    fun lineIdsFor(cue: TimelineCue): List<String> = cue.lineIds ?: listOf(cue.lineId)

    private fun cuesForLine(cues: List<TimelineCue>, lineId: String): List<TimelineCue> =
        cues.filter { it.lineId == lineId || it.lineIds?.contains(lineId) == true }

    /** The earliest cue (by start_ms) that displays [lineId], or null if it never appears. */
    fun firstCueForLine(cues: List<TimelineCue>, lineId: String): TimelineCue? =
        cuesForLine(cues, lineId).minByOrNull { it.startMs }

    /**
     * Seek target for tapping a line: the first cue for that line at or after
     * [positionMs], else its first cue overall. Null if the line has no cue.
     */
    fun seekTargetMs(cues: List<TimelineCue>, lineId: String, positionMs: Long): Long? {
        val matches = cuesForLine(cues, lineId).sortedBy { it.startMs }
        if (matches.isEmpty()) return null
        return matches.firstOrNull { it.startMs >= positionMs }?.startMs ?: matches.first().startMs
    }

    /**
     * Whether a `model_answer` line has been revealed: true once playback has reached
     * its first cue's start. Before that it is placeholder-only, per the one
     * non-configurable transcript rule.
     */
    fun isModelAnswerRevealed(cues: List<TimelineCue>, lineId: String, positionMs: Long): Boolean {
        val firstCue = firstCueForLine(cues, lineId) ?: return false
        return positionMs >= firstCue.startMs
    }
}
