package com.hanziflow.shared.transcript

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChineseOnlyPlanTest {
    private val cues = listOf(
        TimelineCue("zh1", 0, 1_000, language = "zh-CN"),
        TimelineCue("vi1", 1_000, 2_000, language = "vi-VN"),
        TimelineCue("vi2", 2_000, 3_000, language = "vi-VN"),
        TimelineCue("zh2", 3_500, 4_500, language = "zh-CN"),
        TimelineCue("zh3", 5_000, 6_000, language = "zh-CN"),
        TimelineCue("vi3", 6_000, 7_000, language = "vi-VN"),
    )

    @Test
    fun consecutiveVietnameseCuesMergeIntoOneRangeEndingBeforeTheNextChineseCue() {
        val ranges = ChineseOnlyPlan.skipRanges(cues)
        assertEquals(
            listOf(
                ChineseOnlyPlan.SkipRange(1_000, 3_500 - ChineseOnlyPlan.GAP_MS),
                ChineseOnlyPlan.SkipRange(6_000, 7_000),
            ),
            ranges,
        )
    }

    @Test
    fun gapsBetweenChineseCuesAreUntouched() {
        assertNull(ChineseOnlyPlan.skipTargetFor(cues, 4_700))
        assertTrue(!ChineseOnlyPlan.isSkipped(cues, 4_700))
    }

    @Test
    fun skipTargetJumpsToTheRangeEnd() {
        assertEquals(3_500 - ChineseOnlyPlan.GAP_MS, ChineseOnlyPlan.skipTargetFor(cues, 1_500))
        assertEquals(7_000, ChineseOnlyPlan.skipTargetFor(cues, 6_500))
        assertNull(ChineseOnlyPlan.skipTargetFor(cues, 0))
    }

    @Test
    fun effectiveRemainingSubtractsOnlyNotYetReachedSkipRanges() {
        val full = ChineseOnlyPlan.effectiveRemainingMs(cues, 0, 7_000)
        val afterFirstSkip = ChineseOnlyPlan.effectiveRemainingMs(cues, 3_500 - ChineseOnlyPlan.GAP_MS, 7_000)
        assertTrue(full < 7_000)
        assertTrue(afterFirstSkip > full - (3_500 - ChineseOnlyPlan.GAP_MS))
    }

    @Test
    fun realLessonTimeline_noSkipRangeOverlapsAChineseCueAndEveryVietnameseCueIsCovered() {
        val timeline = CourseJson.timeline(fixtureText("timeline_guided.json"))
        val ranges = ChineseOnlyPlan.skipRanges(timeline.cues)

        val chineseCues = timeline.cues.filter { it.language == "zh-CN" }
        for (chinese in chineseCues) {
            assertTrue(
                ranges.none { it.fromMs < chinese.endMs && chinese.startMs < it.toMs },
                "skip range overlaps zh-CN cue ${chinese.lineId}",
            )
        }

        val vietnameseCues = timeline.cues.filter { it.language == "vi-VN" }
        for (vi in vietnameseCues) {
            assertTrue(
                ranges.any { it.fromMs <= vi.startMs && vi.endMs <= it.toMs },
                "vi-VN cue ${vi.lineId} is not inside any skip range",
            )
        }

        val effective = ChineseOnlyPlan.effectiveRemainingMs(timeline.cues, 0, timeline.durationMs)
        assertTrue(effective < timeline.durationMs)
    }
}
