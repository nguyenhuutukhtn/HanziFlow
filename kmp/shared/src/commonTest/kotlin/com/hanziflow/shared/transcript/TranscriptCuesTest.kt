package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TranscriptCuesTest {
    private val cues = listOf(
        TimelineCue(lineId = "a", startMs = 0, endMs = 1000),
        TimelineCue(lineId = "b", startMs = 1000, endMs = 2000),
        // gap between 2000 and 2500
        TimelineCue(lineId = "c", startMs = 2500, endMs = 3000),
        TimelineCue(lineId = "group", startMs = 3000, endMs = 4000, lineIds = listOf("d", "e"), granularity = "segment"),
    )

    @Test
    fun activeCue_atStartBoundary_isInclusive() {
        assertEquals("a", TranscriptCues.activeCue(cues, 0)?.lineId)
        assertEquals("b", TranscriptCues.activeCue(cues, 1000)?.lineId)
    }

    @Test
    fun activeCue_atEndBoundary_isExclusive() {
        // 999 still belongs to "a"; 1000 has already moved to "b"
        assertEquals("a", TranscriptCues.activeCue(cues, 999)?.lineId)
        assertEquals("b", TranscriptCues.activeCue(cues, 1999)?.lineId)
    }

    @Test
    fun activeCue_inGapBetweenCues_isNull() {
        assertNull(TranscriptCues.activeCue(cues, 2000))
        assertNull(TranscriptCues.activeCue(cues, 2250))
        assertNull(TranscriptCues.activeCue(cues, 2499))
    }

    @Test
    fun activeCue_beforeFirstOrAfterLast_isNull() {
        assertNull(TranscriptCues.activeCue(cues, -1))
        assertNull(TranscriptCues.activeCue(emptyList(), 0))
        assertNull(TranscriptCues.activeCue(cues, 4000))
        assertNull(TranscriptCues.activeCue(cues, 10_000))
    }

    @Test
    fun activeCue_groupedCue_highlightsAllLineIds() {
        val cue = TranscriptCues.activeCue(cues, 3500)
        assertEquals(listOf("d", "e"), cue?.let(TranscriptCues::lineIdsFor))
    }

    @Test
    fun lineIdsFor_ungroupedCue_isJustItsOwnLine() {
        val cue = TranscriptCues.activeCue(cues, 500)!!
        assertEquals(listOf("a"), TranscriptCues.lineIdsFor(cue))
    }

    @Test
    fun seekTarget_findsFirstCueAtOrAfterPosition() {
        val repeated = listOf(
            TimelineCue(lineId = "x", startMs = 100, endMs = 200),
            TimelineCue(lineId = "x", startMs = 5000, endMs = 5200),
        )
        assertEquals(5000, TranscriptCues.seekTargetMs(repeated, "x", positionMs = 300))
    }

    @Test
    fun seekTarget_pastAllCues_wrapsToFirst() {
        val repeated = listOf(
            TimelineCue(lineId = "x", startMs = 100, endMs = 200),
            TimelineCue(lineId = "x", startMs = 5000, endMs = 5200),
        )
        assertEquals(100, TranscriptCues.seekTargetMs(repeated, "x", positionMs = 6000))
    }

    @Test
    fun seekTarget_matchesGroupedLineIds() {
        assertEquals(3000, TranscriptCues.seekTargetMs(cues, "e", positionMs = 0))
    }

    @Test
    fun seekTarget_unknownLine_isNull() {
        assertNull(TranscriptCues.seekTargetMs(cues, "nope", positionMs = 0))
    }

    @Test
    fun modelAnswer_notYetReached_isHidden() {
        assertEquals(false, TranscriptCues.isModelAnswerRevealed(cues, "c", positionMs = 0))
        assertEquals(false, TranscriptCues.isModelAnswerRevealed(cues, "c", positionMs = 2499))
    }

    @Test
    fun modelAnswer_onOrAfterItsFirstCue_isRevealed() {
        assertEquals(true, TranscriptCues.isModelAnswerRevealed(cues, "c", positionMs = 2500))
        assertEquals(true, TranscriptCues.isModelAnswerRevealed(cues, "c", positionMs = 9000))
    }

    @Test
    fun modelAnswer_unknownLine_isNeverRevealed() {
        assertEquals(false, TranscriptCues.isModelAnswerRevealed(cues, "nope", positionMs = 9000))
    }
}
