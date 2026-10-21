package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue

/** Maps a position between two timelines that share the same published line ids. */
object TrackSwitch {
    fun positionFor(fromCues: List<TimelineCue>, toCues: List<TimelineCue>, positionMs: Long): Long {
        if (toCues.isEmpty()) return 0L
        val from = fromCues.sortedBy { it.startMs }
        val to = toCues.sortedBy { it.startMs }
        if (from.isEmpty()) return to.first().startMs
        val source = from.firstOrNull { positionMs in it.startMs until it.endMs }
            ?: if (positionMs < from.first().startMs) from.first() else from.last()
        val target = source?.let { cue ->
            to.firstOrNull { it.lineId == cue.lineId }
                ?: if (cue.language == "vi-VN") {
                    from.firstOrNull { it.startMs >= cue.endMs && it.language == "zh-CN" }
                        ?.let { next -> to.firstOrNull { it.lineId == next.lineId } }
                } else null
        }
        return target?.startMs ?: if (positionMs < from.first().startMs) {
            to.first().startMs
        } else {
            to.last().startMs
        }
    }
}
