package com.hanziflow.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoopPublisherTest {
    @Test
    fun reconnectingControllerSeesTheExistingLoop() {
        val publisher = LoopPublisher()
        publisher.start(lineId = "s1_food", startMs = 1000, endMs = 2000)

        // A controller connecting fresh (e.g. the UI was recreated while the service
        // kept running) must read the same state as one that was already connected -
        // neither remembers anything of its own, both just read the publisher.
        val alreadyConnected = publisher.current
        val reconnecting = publisher.current

        assertEquals(LoopState("s1_food", 1000, 2000), alreadyConnected)
        assertEquals(alreadyConnected, reconnecting)
    }

    @Test
    fun aNewConnectionNeverSeesAClearedLoop() {
        val publisher = LoopPublisher()
        publisher.start(lineId = "s1_food", startMs = 1000, endMs = 2000)
        publisher.clear()

        assertNull(publisher.current)
    }

    @Test
    fun startingAnotherLoopReplacesThePrevious() {
        val publisher = LoopPublisher()
        publisher.start(lineId = "s1_food", startMs = 1000, endMs = 2000)
        publisher.start(lineId = "s1_who", startMs = 5000, endMs = 6000)

        assertEquals(LoopState("s1_who", 5000, 6000), publisher.current)
    }
}
