package com.hanziflow.ui.player

import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.Timeline
import com.hanziflow.shared.modes.GapState
import com.hanziflow.shared.modes.gapAt
import com.hanziflow.shared.transcript.TranscriptCues

internal data class ModePresentation(
    val lines: List<ReferenceLine>,
    val activeGap: GapState?,
    val ngheRevealCueStartMs: Long?,
)

internal fun modePresentation(
    lessonFormat: String,
    track: String,
    timeline: Timeline,
    lines: List<ReferenceLine>,
    positionMs: Long,
    ngheRevealCueStartMs: Long?,
): ModePresentation {
    if (lessonFormat != "modes_v1") return ModePresentation(lines, null, ngheRevealCueStartMs)
    val activeGap = when (track) {
        "phan_xa", "noi" -> gapAt(timeline, positionMs)
        else -> null
    }
    if (track == "phan_xa") {
        val startedIds = timeline.cues.filter { it.startMs <= positionMs }.mapTo(mutableSetOf()) { it.lineId }
        return ModePresentation(lines.filter { it.id in startedIds }, activeGap, ngheRevealCueStartMs)
    }
    if (track != "nghe") return ModePresentation(lines, activeGap, ngheRevealCueStartMs)

    val latestStarted = timeline.cues.lastOrNull { it.startMs <= positionMs }
    val revealStart = ngheRevealCueStartMs?.takeUnless { latestStarted != null && latestStarted.startMs > it }
    val revealedIds = timeline.cues.firstOrNull { it.startMs == revealStart }
        ?.let(TranscriptCues::lineIdsFor)
        ?.toSet()
        .orEmpty()
    return ModePresentation(lines.filter { it.id in revealedIds }, activeGap, revealStart)
}
