package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineCue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CueNavigationTest {
    private val cues = listOf(
        TimelineCue("a", 0, 1_000),
        TimelineCue("b", 2_000, 5_000),
        TimelineCue("c", 6_000, 7_000),
    )

    @Test
    fun next_insideCue_goesToFollowingCue() {
        assertEquals(2_000L, CueNavigation.nextCueStartMs(cues, 500))
        assertEquals(6_000L, CueNavigation.nextCueStartMs(cues, 3_000))
    }

    @Test
    fun next_inGap_goesToTheNextCueThatStartsLater() {
        assertEquals(2_000L, CueNavigation.nextCueStartMs(cues, 1_500))
        assertEquals(6_000L, CueNavigation.nextCueStartMs(cues, 5_500))
    }

    @Test
    fun next_atCueStart_skipsToTheFollowingCue() {
        assertEquals(6_000L, CueNavigation.nextCueStartMs(cues, 2_000))
    }

    @Test
    fun next_afterLastCue_isNull() {
        assertNull(CueNavigation.nextCueStartMs(cues, 7_000))
        assertNull(CueNavigation.nextCueStartMs(emptyList(), 0))
    }

    @Test
    fun previous_afterReplayWindow_replaysCurrentCue() {
        assertEquals(2_000L, CueNavigation.previousCueStartMs(cues, 3_600))
    }

    @Test
    fun previous_withinReplayWindow_goesToPreviousCue() {
        assertEquals(0L, CueNavigation.previousCueStartMs(cues, 2_200))
        assertEquals(2_000L, CueNavigation.previousCueStartMs(cues, 6_400))
    }

    @Test
    fun previous_onFirstCueWithinReplayWindow_staysAtItsStart() {
        assertEquals(0L, CueNavigation.previousCueStartMs(cues, 300))
    }

    @Test
    fun previous_inGap_goesToLastCueThatStartedBefore() {
        assertEquals(0L, CueNavigation.previousCueStartMs(cues, 1_500))
        assertEquals(2_000L, CueNavigation.previousCueStartMs(cues, 5_500))
    }

    @Test
    fun previous_withoutTimeline_isNull() {
        assertNull(CueNavigation.previousCueStartMs(emptyList(), 1_000))
    }

}
