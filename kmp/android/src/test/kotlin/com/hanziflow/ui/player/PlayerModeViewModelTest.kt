package com.hanziflow.ui.player

import com.hanziflow.data.LessonEntity
import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.fakes.FakePlayerConnection
import com.hanziflow.fakes.FakeSettingsRepository
import com.hanziflow.playback.PlaybackState
import com.hanziflow.shared.CourseJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerModeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val fixtureRoot = File("../shared/src/commonTest/resources/contract")

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun modeRepository(track: String): FakeCourseRepository {
        val catalog = CourseJson.catalog(File(fixtureRoot, "catalog_modes.json").readText())
        val lesson = catalog.lessons.single { it.id == "lesson_002" }
        return FakeCourseRepository().apply {
            lessonsFlow.value = listOf(
                LessonEntity(
                    id = "lesson_002",
                    title = lesson.title,
                    level = lesson.level,
                    lessonOrder = lesson.order,
                    remoteVersion = lesson.version,
                    catalogJson = "{}",
                ),
            )
            catalogLessons = mapOf("lesson_002" to lesson)
            referenceFiles = mapOf("lesson_002" to File(fixtureRoot, "reference_modes.json"))
            timelineFiles = mapOf(("lesson_002" to track) to File(fixtureRoot, "timeline_$track.json"))
        }
    }

    private fun viewModel(
        repository: FakeCourseRepository,
        connection: FakePlayerConnection,
        settings: FakeSettingsRepository = FakeSettingsRepository(),
    ) =
        PlayerViewModel(repository, settings, connection).also {
            it.backgroundDispatcher = dispatcher
        }

    private fun ngheRepository(): FakeCourseRepository {
        val repository = modeRepository("phan_xa")
        val reference = File(fixtureRoot, "reference_modes.json")
        val ngheLines = CourseJson.reference(reference.readText()).tracks.forName("nghe").orEmpty()
        val cues = ngheLines.take(5).mapIndexed { index, line ->
            """{"line_id":"${line.id}","start_ms":${index * 2000},"end_ms":${index * 2000 + 1500}}"""
        }.joinToString(",")
        val timeline = File.createTempFile("timeline_nghe", ".json").apply {
            writeText(
                """{"schema_version":1,"lesson_id":"lesson_002","lesson_version":2,"track":"nghe","duration_ms":10000,"cues":[$cues]}""",
            )
        }
        repository.referenceFiles = mapOf("lesson_002" to reference)
        repository.timelineFiles = repository.timelineFiles + (("lesson_002" to "nghe") to timeline)
        return repository
    }

    @Test
    fun `phan xa hides unstarted answer and exposes tick-driven answer gap`() = runTest(dispatcher) {
        val connection = FakePlayerConnection()
        val viewModel = viewModel(modeRepository("phan_xa"), connection)

        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "phan_xa", positionMs = 5_000))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.transcript.lines.any { it.id == "phan_xa_q2_a" })
        assertEquals("answer", viewModel.uiState.value.activeGap?.kind)
        assertEquals(2_294L, viewModel.uiState.value.activeGap?.remainingMs)

        connection.emit(connection.state.value.copy(positionMs = 2_000))
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.activeGap)

        connection.emit(connection.state.value.copy(positionMs = 7_400))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.transcript.lines.any { it.id == "phan_xa_q2_a" })
    }

    @Test
    fun `noi exposes repeat gap without filtering transcript`() = runTest(dispatcher) {
        val repository = modeRepository("noi")
        val timeline = CourseJson.timeline(repository.timelineFiles.getValue("lesson_002" to "noi").readText())
        val firstGap = timeline.gaps.first()
        val connection = FakePlayerConnection()
        val viewModel = viewModel(repository, connection)

        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "noi", positionMs = firstGap.startMs))
        advanceUntilIdle()

        assertEquals("repeat", viewModel.uiState.value.activeGap?.kind)
        assertEquals(43, viewModel.uiState.value.transcript.lines.size)
        viewModel.pauseForGap()
        assertEquals(1, connection.pauseCalls)
    }

    @Test
    fun `nghe toggles the whole transcript and keeps it visible across cues`() = runTest(dispatcher) {
        val repository = ngheRepository().apply {
            trackFiles = mapOf(("lesson_002" to "nghe") to File.createTempFile("nghe", ".mp3"))
        }
        val connection = FakePlayerConnection()
        val viewModel = viewModel(repository, connection)

        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "nghe", positionMs = 500))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.transcript.lines.isEmpty())

        viewModel.revealCurrentNgheCue()
        val allLineIds = CourseJson.reference(File(fixtureRoot, "reference_modes.json").readText())
            .tracks.forName("nghe").orEmpty().map { it.id }
        assertEquals(allLineIds, viewModel.uiState.value.transcript.lines.map { it.id })
        assertTrue(viewModel.uiState.value.ngheTranscriptVisible)

        connection.emit(connection.state.value.copy(positionMs = 7_000))
        advanceUntilIdle()
        assertEquals(allLineIds, viewModel.uiState.value.transcript.lines.map { it.id })

        viewModel.play(repository.lessonsFlow.value.single(), "nghe")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.transcript.lines.isEmpty())

        viewModel.revealCurrentNgheCue()
        assertEquals(allLineIds, viewModel.uiState.value.transcript.lines.map { it.id })
        viewModel.revealCurrentNgheCue()
        assertTrue(viewModel.uiState.value.transcript.lines.isEmpty())

        viewModel.revealCurrentNgheCue()
        connection.emit(connection.state.value.copy(track = "phan_xa", positionMs = 0))
        advanceUntilIdle()
        connection.emit(connection.state.value.copy(track = "nghe", positionMs = 500))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.transcript.lines.isEmpty())
        assertFalse(viewModel.uiState.value.ngheTranscriptVisible)
    }

    @Test
    fun `repeat each toggle persists per lesson and track across viewmodels`() = runTest(dispatcher) {
        val settings = FakeSettingsRepository()
        val firstConnection = FakePlayerConnection()
        val first = viewModel(modeRepository("noi"), firstConnection, settings)
        firstConnection.emit(PlaybackState(connected = true, lessonId = "lesson_002", track = "noi"))
        advanceUntilIdle()

        first.toggleRepeatEach()
        advanceUntilIdle()
        assertTrue(first.uiState.value.repeatEachEnabled)

        val secondConnection = FakePlayerConnection()
        val second = viewModel(modeRepository("noi"), secondConnection, settings)
        secondConnection.emit(PlaybackState(connected = true, lessonId = "lesson_002", track = "noi"))
        advanceUntilIdle()

        assertEquals(true, secondConnection.repeatEachCalls.last())
        assertTrue(second.uiState.value.repeatEachEnabled)
    }
}
