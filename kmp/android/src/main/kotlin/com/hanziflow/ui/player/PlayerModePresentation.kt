package com.hanziflow.ui.player

import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.Timeline
import com.hanziflow.shared.modes.GapState
import com.hanziflow.shared.modes.gapAt

internal data class ModePresentation(
    val lines: List<ReferenceLine>,
    val activeGap: GapState?,
)

internal fun modePresentation(
    lessonFormat: String,
    track: String,
    timeline: Timeline,
    lines: List<ReferenceLine>,
    positionMs: Long,
    ngheTranscriptVisible: Boolean,
): ModePresentation {
    if (lessonFormat != "modes_v1") return ModePresentation(lines, null)
    val activeGap = when (track) {
        "phan_xa", "noi" -> gapAt(timeline, positionMs)
        else -> null
    }
    if (track == "phan_xa") {
        val startedIds = timeline.cues.filter { it.startMs <= positionMs }.mapTo(mutableSetOf()) { it.lineId }
        return ModePresentation(lines.filter { it.id in startedIds }, activeGap)
    }
    if (track != "nghe") return ModePresentation(lines, activeGap)
    return ModePresentation(if (ngheTranscriptVisible) lines else emptyList(), activeGap)
}
