package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue

/** Maps a position between two timelines that share the same published line ids. */
object TrackSwitch {
    fun positionFor(fromCues: List<TimelineCue>, toCues: List<TimelineCue>, positionMs: Long): Long {
        if (toCues.isEmpty()) return 0L
        val from = fromCues.sortedBy { it.startMs }
        val to = toCues.sortedBy { it.startMs }
        if (from.isEmpty()) return to.first().startMs
        val targetByLineId = to.associateBy { it.lineId }
        val sourceIndex = from.indexOfFirst { positionMs in it.startMs until it.endMs }
            .takeIf { it >= 0 }
            ?: from.indexOfFirst { it.startMs > positionMs }.takeIf { it >= 0 }
            ?: from.lastIndex

        from.subList(sourceIndex, from.size)
            .firstNotNullOfOrNull { targetByLineId[it.lineId] }
            ?.let { return it.startMs }

        from.subList(0, sourceIndex)
            .asReversed()
            .firstNotNullOfOrNull { targetByLineId[it.lineId] }
            ?.let { return it.startMs }

        return to.first().startMs
    }
}
