package com.hanziflow.ui.player

import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.fakes.FakePlayerConnection
import com.hanziflow.fakes.FakeSettingsRepository
import com.hanziflow.playback.PlaybackState
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun tempJson(name: String, content: String): File =
        File.createTempFile(name, ".json").apply { writeText(content) }

    /** Blocks the real test thread (not virtual time) until [lines] load from the fake repository's IO. */
    private fun awaitTranscriptLoaded(viewModel: PlayerViewModel, timeoutMs: Long = 2_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (viewModel.uiState.value.transcript.lines.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    private fun repositoryWithTranscript(lessonId: String, track: String = "guided"): FakeCourseRepository {
        val reference = tempJson(
            "reference",
            """{"tracks":{"guided":[{"id":"r1_food","type":"line","language":"zh","speaker":"coach_zh","text":"你好","pinyin":"nǐ hǎo"}]}}""",
        )
        val timeline = tempJson(
            "timeline",
            """{"schema_version":1,"lesson_id":"$lessonId","lesson_version":1,"track":"$track","duration_ms":5000,"cues":[{"line_id":"r1_food","start_ms":1000,"end_ms":3000}]}""",
        )
        val items = tempJson(
            "items",
            """{"schema_version":1,"lesson_id":"$lessonId","lesson_version":1,"words":[]}""",
        )
        return FakeCourseRepository().apply {
            referenceFiles = mapOf(lessonId to reference)
            timelineFiles = mapOf((lessonId to track) to timeline)
            itemsFiles = mapOf(lessonId to items)
        }
    }

    @Test
    fun `transcript loads and the active line's cue drives seeking`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        awaitTranscriptLoaded(viewModel)

        val transcript = viewModel.uiState.value.transcript
        assertEquals(1, transcript.lines.size)
        assertEquals("r1_food", transcript.lines.single().id)
        assertEquals(1, transcript.cues.size)

        // Already inside the active cue (1000..3000ms): seeking the same line is a no-op seek to its start.
        viewModel.seekToLine("r1_food")
        assertEquals(listOf(1000L), connection.seekToCalls)
    }

    @Test
    fun `replay current line seeks to the active cue start`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        awaitTranscriptLoaded(viewModel)

        // Current cue is 1000..3000ms; replay is a one-shot seek to its start.
        viewModel.replayCurrentLine()
        assertEquals(listOf(1000L), connection.seekToCalls)

        connection.emit(connection.state.value.copy(positionMs = 2_700))
        viewModel.replayCurrentLine()
        assertEquals(listOf(1000L, 1000L), connection.seekToCalls)
    }

    @Test
    fun `replay current line is a no-op without a timeline`() = runTest {
        val repository = FakeCourseRepository()
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        // Give ensureTranscriptLoaded a moment to publish the (empty) transcript state.
        Thread.sleep(50)

        viewModel.replayCurrentLine()
        assertEquals(emptyList<Long>(), connection.seekToCalls)
    }

    @Test
    fun `loop state is mirrored from the connection, never set locally`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        awaitTranscriptLoaded(viewModel)

        assertNull(viewModel.uiState.value.transcript.loopedLineId)

        viewModel.toggleLoopForLine("r1_food")
        assertEquals(listOf(Triple("r1_food", 1000L, 3000L)), connection.loopSegmentCalls)
        // The ViewModel never sets loopedLineId itself: the fake connection publishes it back,
        // same as the real service does via session extras.
        connection.emit(connection.state.value.copy(loopedLineId = "r1_food"))
        assertEquals("r1_food", viewModel.uiState.value.transcript.loopedLineId)

        viewModel.cancelLoop()
        assertEquals(1, connection.clearLoopCalls)
    }

    @Test
    fun `download state transitions surface a message when the track isn't downloaded`() = runTest {
        val repository = FakeCourseRepository()
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        val lesson = com.hanziflow.data.LessonEntity(
            id = "lesson_001",
            title = "Lesson 1",
            level = "absolute_beginner",
            lessonOrder = 1,
            remoteVersion = 1,
            catalogJson = "{}",
        )
        viewModel.play(lesson)

        assertEquals("Download this lesson first", viewModel.uiState.value.message)
        assertEquals(emptyList<List<String>>(), connection.playCalls)
    }
}
