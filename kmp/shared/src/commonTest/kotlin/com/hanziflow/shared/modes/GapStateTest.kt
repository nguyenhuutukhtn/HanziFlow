package com.hanziflow.shared.modes

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GapStateTest {
    private val timeline = CourseJson.timeline(fixtureText("timeline_phan_xa.json"))

    @Test
    fun findsAnswerGapInRealPhanXaTimeline() {
        assertEquals(
            GapState(
                kind = "answer",
                remainingMs = 2_294,
                totalMs = 3_420,
                afterLineId = "phan_xa_q2",
            ),
            gapAt(timeline, 5_000),
        )
        assertNull(gapAt(timeline, 2_000))
        assertNull(gapAt(timeline, 7_294))
    }
}
