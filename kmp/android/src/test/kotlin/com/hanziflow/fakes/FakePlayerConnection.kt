package com.hanziflow.fakes

import com.hanziflow.playback.PlaybackState
import com.hanziflow.playback.PlayerConnection
import com.hanziflow.playback.SectionLoopInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Lets a ViewModel test drive the service's published state without Media3 or a device. */
class FakePlayerConnection : PlayerConnection {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state

    val playCalls = mutableListOf<List<String>>()
    var toggleCalls = 0
    var seekBackCalls = 0
    var seekForwardCalls = 0
    val seekToCalls = mutableListOf<Long>()
    val speedCalls = mutableListOf<Float>()
    val loopSegmentCalls = mutableListOf<Triple<String, Long, Long>>()
    val loopSectionCalls = mutableListOf<SectionLoopInfo>()
    var stopSectionLoopAfterPassCalls = 0
    var nextSectionCalls = 0
    var clearLoopCalls = 0
    val switchTrackCalls = mutableListOf<String>()
    val playClipCalls = mutableListOf<String>()

    fun emit(state: PlaybackState) { _state.value = state }

    override fun play(lessonId: String, track: String, uri: String, title: String) {
        playCalls += listOf(lessonId, track, uri, title)
        _state.value = _state.value.copy(connected = true, playing = true, lessonId = lessonId, track = track)
    }

    override fun toggle() { toggleCalls++ }
    override fun seekBack() { seekBackCalls++ }
    override fun seekForward() { seekForwardCalls++ }
    override fun seekTo(positionMs: Long) { seekToCalls += positionMs }
    override fun setSpeed(speed: Float) { speedCalls += speed }

    override fun loopSegment(lineId: String, startMs: Long, endMs: Long) {
        loopSegmentCalls += Triple(lineId, startMs, endMs)
        _state.value = _state.value.copy(loopedLineId = lineId, sectionLoop = null)
    }

    override fun loopSection(sectionId: String, title: String, startMs: Long, endMs: Long) {
        val info = SectionLoopInfo(sectionId, title, pass = 1, stopAfterPass = false)
        loopSectionCalls += info
        _state.value = _state.value.copy(loopedLineId = null, sectionLoop = info)
    }

    override fun stopSectionLoopAfterPass() {
        stopSectionLoopAfterPassCalls++
        _state.value.sectionLoop?.let { current ->
            _state.value = _state.value.copy(sectionLoop = current.copy(stopAfterPass = true))
        }
    }

    override fun nextSection() {
        nextSectionCalls++
        _state.value = _state.value.copy(sectionLoop = null)
    }

    override fun clearLoop() {
        clearLoopCalls++
        _state.value = _state.value.copy(loopedLineId = null, sectionLoop = null)
    }

    override fun switchTrack(track: String) {
        switchTrackCalls += track
        _state.value = _state.value.copy(track = track)
    }

    override fun playClip(uri: String) { playClipCalls += uri }
}
