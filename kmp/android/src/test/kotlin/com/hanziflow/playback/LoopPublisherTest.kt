package com.hanziflow.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopPublisherTest {
    @Test
    fun reconnectingControllerSeesTheExistingLineLoop() {
        val publisher = LoopPublisher()
        publisher.startLine(lineId = "s1_food", startMs = 1000, endMs = 2000)

        // A controller connecting fresh (e.g. the UI was recreated while the service
        // kept running) must read the same state as one that was already connected -
        // neither remembers anything of its own, both just read the publisher.
        val alreadyConnected = publisher.current
        val reconnecting = publisher.current

        assertEquals(LoopState.Line("s1_food", 1000, 2000), alreadyConnected)
        assertEquals(alreadyConnected, reconnecting)
    }

    @Test
    fun aNewConnectionNeverSeesAClearedLoop() {
        val publisher = LoopPublisher()
        publisher.startLine(lineId = "s1_food", startMs = 1000, endMs = 2000)
        publisher.clear()

        assertNull(publisher.current)
    }

    @Test
    fun startingAnotherLineLoopReplacesThePrevious() {
        val publisher = LoopPublisher()
        publisher.startLine(lineId = "s1_food", startMs = 1000, endMs = 2000)
        publisher.startLine(lineId = "s1_who", startMs = 5000, endMs = 6000)

        assertEquals(LoopState.Line("s1_who", 5000, 6000), publisher.current)
    }

    @Test
    fun sectionLoopStartsAtPass1NotStopping() {
        val publisher = LoopPublisher()
        publisher.startSection("story", "Nghe chuyện", 0, 10_000)

        assertEquals(LoopState.Section("story", "Nghe chuyện", 0, 10_000, pass = 1, stopAfterPass = false), publisher.current)
    }

    @Test
    fun updateSectionMutatesOnlyAnActiveSectionLoop() {
        val publisher = LoopPublisher()
        publisher.updateSection { it.copy(pass = 9) }
        assertNull(publisher.current)

        publisher.startSection("story", "Nghe chuyện", 0, 10_000)
        publisher.updateSection { it.copy(pass = it.pass + 1, stopAfterPass = true) }

        val loop = publisher.current as LoopState.Section
        assertEquals(2, loop.pass)
        assertTrue(loop.stopAfterPass)
    }

    @Test
    fun startingALineLoopReplacesAnActiveSectionLoopAndViceVersa() {
        val publisher = LoopPublisher()
        publisher.startSection("story", "Nghe chuyện", 0, 10_000)
        publisher.startLine("s1_food", 1000, 2000)
        assertEquals(LoopState.Line("s1_food", 1000, 2000), publisher.current)

        publisher.startSection("qa", "Hỏi đáp", 10_000, 20_000)
        assertTrue(publisher.current is LoopState.Section)
    }
}
