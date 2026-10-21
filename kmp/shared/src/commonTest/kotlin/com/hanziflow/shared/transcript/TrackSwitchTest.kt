package com.hanziflow.shared.transcript

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackSwitchTest {
    private val guided = CourseJson.timeline(fixtureText("timeline_guided.json"))
    private val chinese = CourseJson.timeline(fixtureText("timeline_guided_zh.json"))

    @Test
    fun switchesBothDirectionsAtTheSameLine() {
        val cue = guided.cues.first { it.language == "zh-CN" }
        assertEquals(cue.lineId, chinese.cues.first { it.lineId == cue.lineId }.lineId)
        assertEquals(
            chinese.cues.first { it.lineId == cue.lineId }.startMs,
            TrackSwitch.positionFor(guided.cues, chinese.cues, cue.startMs + 1),
        )
        assertEquals(
            cue.startMs,
            TrackSwitch.positionFor(chinese.cues, guided.cues, chinese.cues.first { it.lineId == cue.lineId }.startMs + 1),
        )
    }

    @Test
    fun vietnameseCueMapsToTheNextChineseLine() {
        val vi = guided.cues.first { it.language == "vi-VN" }
        val nextChinese = guided.cues.first { it.startMs >= vi.endMs && it.language == "zh-CN" }
        assertEquals(
            chinese.cues.first { it.lineId == nextChinese.lineId }.startMs,
            TrackSwitch.positionFor(guided.cues, chinese.cues, vi.startMs + 1),
        )
    }

    @Test
    fun positionInGapMapsToTheNextMappedCue() {
        val from = listOf(
            TimelineCue("one", 100, 200),
            TimelineCue("two", 300, 400),
        )
        val to = listOf(
            TimelineCue("one", 10, 20),
            TimelineCue("two", 30, 40),
        )

        assertEquals(30, TrackSwitch.positionFor(from, to, 250))
    }

    @Test
    fun gapAfterUnmappedVietnameseCueMapsToTheNextChineseCue() {
        val from = listOf(
            TimelineCue("vi", 100, 200, language = "vi-VN"),
            TimelineCue("zh", 300, 400, language = "zh-CN"),
        )
        val to = listOf(TimelineCue("zh", 30, 40, language = "zh-CN"))

        assertEquals(30, TrackSwitch.positionFor(from, to, 250))
    }

    @Test
    fun missingSourceLineWalksForwardBeforeWalkingBackward() {
        val from = listOf(
            TimelineCue("before", 100, 200),
            TimelineCue("guided_only", 300, 400),
            TimelineCue("after", 500, 600),
        )
        val to = listOf(
            TimelineCue("before", 10, 20),
            TimelineCue("after", 30, 40),
        )

        assertEquals(30, TrackSwitch.positionFor(from, to, 350))
    }

    @Test
    fun realLessonGapMapsToTheNextSharedLine() {
        val (left, right) = guided.cues.zipWithNext().first { (current, next) ->
            current.endMs < next.startMs && chinese.cues.any { it.lineId == next.lineId }
        }
        val expected = chinese.cues.first { it.lineId == right.lineId }.startMs

        assertEquals(expected, TrackSwitch.positionFor(guided.cues, chinese.cues, left.endMs + 1))
    }

    @Test
    fun beforeFirstAndAfterLastStayInsideTheTargetTimeline() {
        val tinyFrom = listOf(TimelineCue("first", 100, 200), TimelineCue("last", 300, 400))
        val tinyTo = listOf(TimelineCue("first", 10, 20), TimelineCue("last", 30, 40))
        assertEquals(10, TrackSwitch.positionFor(tinyFrom, tinyTo, 0))
        assertEquals(30, TrackSwitch.positionFor(tinyFrom, tinyTo, 999))
    }

    @Test
    fun afterLastCueUsesNearestMappedCueAndNeverTheTargetLastCueByDefault() {
        val from = listOf(
            TimelineCue("first", 100, 200),
            TimelineCue("missing", 300, 400),
        )
        val to = listOf(
            TimelineCue("first", 10, 20),
            TimelineCue("other", 30, 40),
        )

        assertEquals(10, TrackSwitch.positionFor(from, to, 999))
    }
}
