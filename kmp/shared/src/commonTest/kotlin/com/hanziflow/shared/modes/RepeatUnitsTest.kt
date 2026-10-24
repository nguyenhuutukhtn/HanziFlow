package com.hanziflow.shared.modes

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertEquals

class RepeatUnitsTest {
    @Test
    fun buildsCueAndFollowingGapUnitsFromRealNoiTimeline() {
        val timeline = CourseJson.timeline(fixtureText("timeline_noi.json"))
        val units = repeatUnits(timeline)

        assertEquals(timeline.cues.size, units.size)
        assertEquals("noi_s1", units.first().lineId)
        assertEquals(timeline.cues[1].startMs, units.first().endMs)
        assertEquals(timeline.durationMs, units.last().endMs)
    }
}
