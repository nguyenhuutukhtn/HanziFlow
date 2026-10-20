package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue

/**
 * Skip plan for "Chỉ nghe tiếng Trung": every `vi-VN` cue plus the silence after it,
 * replaced by a short natural gap before the next `zh-CN` cue. Pure over one track's
 * cues; the service applies it from the position check.
 */
object ChineseOnlyPlan {
    const val GAP_MS = 400L

    /** A range to jump over: [fromMs, toMs). Never negative, never cuts into a Chinese cue. */
    data class SkipRange(val fromMs: Long, val toMs: Long)

    fun skipRanges(cues: List<TimelineCue>): List<SkipRange> {
        val sorted = cues.sortedBy { it.startMs }
        val ranges = mutableListOf<SkipRange>()
        var i = 0
        while (i < sorted.size) {
            val cue = sorted[i]
            if (cue.language != "vi-VN") {
                i++
                continue
            }
            var end = i
            while (end < sorted.size && sorted[end].language == "vi-VN") end++
            val from = cue.startMs
            val lastViEnd = sorted[end - 1].endMs
            val nextChinese = sorted.getOrNull(end)
            // Always cover the Vietnamese audio itself; only trim further for the ~400ms
            // gap when there's room to do so without cutting into the next Chinese cue.
            val to = if (nextChinese != null) maxOf(lastViEnd, nextChinese.startMs - GAP_MS) else lastViEnd
            if (to > from) ranges += SkipRange(from, to)
            i = end
        }
        return ranges
    }

    /** The skip range containing [positionMs], if any. */
    fun rangeFor(cues: List<TimelineCue>, positionMs: Long): SkipRange? =
        skipRanges(cues).firstOrNull { positionMs >= it.fromMs && positionMs < it.toMs }

    /** Where to jump if [positionMs] is inside a skip range; null if it isn't (or there is none). */
    fun skipTargetFor(cues: List<TimelineCue>, positionMs: Long): Long? = rangeFor(cues, positionMs)?.toMs

    fun isSkipped(cues: List<TimelineCue>, positionMs: Long): Boolean = rangeFor(cues, positionMs) != null

    /** Remaining audio time with every not-yet-reached skip range removed. */
    fun effectiveRemainingMs(cues: List<TimelineCue>, positionMs: Long, durationMs: Long): Long {
        val skippedAhead = skipRanges(cues).sumOf { range ->
            (range.toMs - range.fromMs.coerceAtLeast(positionMs)).coerceAtLeast(0)
        }
        return (durationMs - positionMs - skippedAhead).coerceAtLeast(0)
    }
}
