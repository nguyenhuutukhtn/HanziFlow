package com.hanziflow.shared.modes

import com.hanziflow.shared.Timeline

data class GapState(val kind: String, val remainingMs: Long, val totalMs: Long, val afterLineId: String)

fun gapAt(timeline: Timeline, positionMs: Long): GapState? {
    var low = 0; var high = timeline.gaps.size - 1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val gap = timeline.gaps[mid]
        when {
            positionMs < gap.startMs -> high = mid - 1
            positionMs >= gap.endMs -> low = mid + 1
            else -> return GapState(gap.kind, gap.endMs - positionMs, gap.endMs - gap.startMs, gap.afterLineId)
        }
    }
    return null
}
