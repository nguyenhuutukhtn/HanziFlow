package com.hanziflow.shared.modes

import com.hanziflow.shared.Timeline

data class RepeatUnit(val startMs: Long, val endMs: Long, val lineId: String)

fun repeatUnits(timeline: Timeline): List<RepeatUnit> = timeline.cues.mapIndexed { index, cue ->
    RepeatUnit(
        startMs = cue.startMs,
        endMs = timeline.cues.getOrNull(index + 1)?.startMs ?: timeline.durationMs,
        lineId = cue.lineId,
    )
}
