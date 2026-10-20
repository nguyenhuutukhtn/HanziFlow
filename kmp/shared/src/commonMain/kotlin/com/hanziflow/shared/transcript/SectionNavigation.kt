package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineSection

/** Pure lookups over a track's `timeline.sections` (assumed sorted, contiguous, covering the track). */
object SectionNavigation {
    fun sectionAt(sections: List<TimelineSection>, positionMs: Long): TimelineSection? =
        sections.firstOrNull { positionMs >= it.startMs && positionMs < it.endMs }
            ?: sections.lastOrNull { positionMs >= it.startMs }

    /** Start of the first section after [positionMs]; null on (or past) the last section. */
    fun nextSectionStart(sections: List<TimelineSection>, positionMs: Long): Long? =
        sections.firstOrNull { it.startMs > positionMs }?.startMs

    fun sectionStart(sections: List<TimelineSection>, id: String): Long? =
        sections.firstOrNull { it.id == id }?.startMs
}

/** A section repeat in progress: pass 1..[MAX_PASSES], "Bỏ lặp" sets [stopAfterPass]. */
data class SectionLoop(
    val sectionId: String,
    val startMs: Long,
    val endMs: Long,
    val pass: Int,
    val stopAfterPass: Boolean,
) {
    init {
        require(pass in 1..MAX_PASSES) { "pass must be in 1..$MAX_PASSES, was $pass" }
    }

    sealed interface Outcome {
        data class Repeat(val seekToMs: Long) : Outcome
        data object Continue : Outcome
    }

    /** Called when playback reaches [endMs]: repeat this section, or let it continue into the next. */
    fun onReachedEnd(): Outcome =
        if (stopAfterPass || pass >= MAX_PASSES) Outcome.Continue else Outcome.Repeat(startMs)

    companion object {
        const val MAX_PASSES = 5
    }
}
