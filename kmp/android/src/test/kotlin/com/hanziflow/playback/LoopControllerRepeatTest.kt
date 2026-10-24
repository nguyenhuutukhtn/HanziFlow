package com.hanziflow.playback

import com.hanziflow.shared.modes.RepeatUnit
import com.hanziflow.shared.TimelineSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopControllerRepeatTest {
    private val units = (0 until 6).map { index ->
        RepeatUnit(startMs = index * 1_000L, endMs = (index + 1) * 1_000L, lineId = "line_$index")
    }

    @Test
    fun `each unit repeats once then continues`() {
        val controller = LoopController()
        controller.startRepeatEach(units, positionMs = 1_200)

        assertEquals(1_000L, controller.onPositionReached(2_000))
        assertNull(controller.onPositionReached(2_000))
    }

    @Test
    fun `manual seek resets the new unit to its first pass`() {
        val controller = LoopController()
        controller.startRepeatEach(units, positionMs = 1_200)
        controller.onPositionReached(2_000)

        controller.onSeek(4_200)

        assertEquals(4_000L, controller.onPositionReached(5_000))
        assertNull(controller.onPositionReached(5_000))
    }

    @Test
    fun `clearing a manual line loop restores enabled repeat each`() {
        val controller = LoopController()
        controller.startRepeatEach(units, positionMs = 1_200)
        controller.startLineLoop("manual", 1_000, 1_500)
        assertTrue(controller.current is LoopState.Line)

        controller.clearManualLoop(positionMs = 1_200)

        assertTrue(controller.current is LoopState.RepeatEach)
        assertEquals(1_000L, controller.onPositionReached(2_000))
    }

    @Test
    fun `finishing a manual section loop restores enabled repeat each`() {
        val controller = LoopController()
        controller.startRepeatEach(units, positionMs = 1_200)
        controller.startSectionLoop(TimelineSection("section", "Section", 1_000, 3_000))
        controller.stopSectionLoopAfterPass()

        assertNull(controller.onPositionReached(3_000))

        assertTrue(controller.current is LoopState.RepeatEach)
    }
}
