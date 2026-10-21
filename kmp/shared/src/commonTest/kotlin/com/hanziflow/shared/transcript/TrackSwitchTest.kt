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
    fun beforeFirstAndAfterLastStayInsideTheTargetTimeline() {
        val tinyFrom = listOf(TimelineCue("first", 100, 200), TimelineCue("last", 300, 400))
        val tinyTo = listOf(TimelineCue("first", 10, 20), TimelineCue("last", 30, 40))
        assertEquals(10, TrackSwitch.positionFor(tinyFrom, tinyTo, 0))
        assertEquals(30, TrackSwitch.positionFor(tinyFrom, tinyTo, 999))
    }
}
