package com.hanziflow.playback

/** The line currently looping: [startMs, endMs) in media time. */
data class LoopState(val lineId: String, val startMs: Long, val endMs: Long)

/**
 * The single source of truth for the active line loop. [PlaybackService] is the only
 * writer; every controller (including one reconnecting after its UI was recreated)
 * must read [current] instead of remembering its own copy, so a newly attached
 * controller always sees whatever the service is actually doing.
 */
class LoopPublisher {
    var current: LoopState? = null
        private set

    fun start(lineId: String, startMs: Long, endMs: Long) {
        current = LoopState(lineId, startMs, endMs)
    }

    fun clear() {
        current = null
    }
}
