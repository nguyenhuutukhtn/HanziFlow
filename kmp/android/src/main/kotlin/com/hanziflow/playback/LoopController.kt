package com.hanziflow.playback

import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.modes.RepeatUnit
import com.hanziflow.shared.transcript.SectionLoop
import com.hanziflow.shared.transcript.SectionNavigation

/**
 * Owns the single active loop (line or section) and the position-check logic that
 * repeats or releases it. [PlaybackService] polls [onPositionReached] on the same
 * tick it checks loop boundaries; this class never touches ExoPlayer itself.
 */
class LoopController {
    private val publisher = LoopPublisher()
    private var repeatUnits: List<RepeatUnit> = emptyList()
    val current: LoopState? get() = publisher.current
    val repeatEachEnabled: Boolean get() = repeatUnits.isNotEmpty()

    fun startLineLoop(lineId: String, startMs: Long, endMs: Long) = publisher.startLine(lineId, startMs, endMs)

    fun startSectionLoop(section: TimelineSection) =
        publisher.startSection(section.id, section.title, section.startMs, section.endMs)

    fun startRepeatEach(units: List<RepeatUnit>, positionMs: Long) {
        repeatUnits = units
        if (units.isEmpty()) publisher.clear() else restoreRepeat(positionMs)
    }

    fun disableRepeatEach() {
        repeatUnits = emptyList()
        if (current is LoopState.RepeatEach) publisher.clear()
    }

    /** "Bỏ lặp": finish the current pass, then continue. No-op without an active section loop. */
    fun stopSectionLoopAfterPass() {
        publisher.updateSection { it.copy(stopAfterPass = true) }
    }

    fun clear() {
        repeatUnits = emptyList()
        publisher.clear()
    }

    fun clearManualLoop(positionMs: Long) {
        if (current !is LoopState.Line && current !is LoopState.Section) return
        if (repeatUnits.isEmpty()) publisher.clear() else restoreRepeat(positionMs)
    }

    fun onSeek(positionMs: Long) {
        if (repeatUnits.isEmpty()) publisher.clear() else restoreRepeat(positionMs)
    }

    /** Seek target when the loop needs to repeat; null otherwise. Clears a finished section loop as a side effect. */
    fun onPositionReached(positionMs: Long): Long? {
        return when (val loop = current) {
            null -> null
            is LoopState.Line -> if (positionMs >= loop.endMs) loop.startMs else null
            is LoopState.Section -> {
                if (positionMs < loop.endMs) return null
                when (loop.toSectionLoop().onReachedEnd()) {
                    is SectionLoop.Outcome.Repeat -> {
                        publisher.updateSection { it.copy(pass = it.pass + 1) }
                        loop.startMs
                    }
                    SectionLoop.Outcome.Continue -> {
                        clearManualLoop(loop.endMs)
                        null
                    }
                }
            }
            is LoopState.RepeatEach -> {
                val unit = repeatUnits.getOrNull(loop.unitIndex) ?: return null
                if (positionMs < unit.endMs) return null
                if (loop.pass == 1) {
                    publisher.updateRepeatEach { it.copy(pass = 2) }
                    unit.startMs
                } else {
                    repeatUnits.getOrNull(loop.unitIndex + 1)?.let {
                        publisher.startRepeatEach(loop.unitIndex + 1)
                    }
                    null
                }
            }
        }
    }

    /** "Sang phần sau": clear the section loop now and jump to the next section's start (or this section's end, if last). */
    fun nextSectionNow(sections: List<TimelineSection>): Long? {
        val loop = current as? LoopState.Section ?: return null
        val target = SectionNavigation.nextSectionStart(sections, loop.startMs) ?: loop.endMs
        clearManualLoop(target)
        return target
    }

    private fun LoopState.Section.toSectionLoop() = SectionLoop(sectionId, startMs, endMs, pass, stopAfterPass)

    private fun restoreRepeat(positionMs: Long) {
        val index = repeatUnits.indexOfLast { it.startMs <= positionMs }
            .coerceAtLeast(0)
            .coerceAtMost(repeatUnits.lastIndex)
        publisher.startRepeatEach(index)
    }
}
