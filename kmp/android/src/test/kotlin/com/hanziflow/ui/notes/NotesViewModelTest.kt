package com.hanziflow.ui.notes

import com.hanziflow.data.LessonEntity
import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.fakes.FakePlayerConnection
import com.hanziflow.fakes.FakeSettingsRepository
import com.hanziflow.playback.PlaybackState
import com.hanziflow.shared.CourseJson
import com.hanziflow.ui.modes.ModePickerViewModel
import com.hanziflow.ui.player.PlayerViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val root = File("../shared/src/commonTest/resources/contract")

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun repository(withNotes: Boolean = true) = FakeCourseRepository().apply {
        val catalog = CourseJson.catalog(File(root, "catalog_modes.json").readText())
        val lesson = catalog.lessons.single { it.id == "lesson_002" }
        catalogLessons = mapOf("lesson_002" to lesson)
        lessonsFlow.value = listOf(LessonEntity("lesson_002", lesson.title, lesson.level, lesson.order, lesson.version, catalogJson = "{}"))
        itemsFiles = mapOf("lesson_002" to File(root, "items_modes.json"), "lesson_001" to File(root, "items.json"))
        referenceFiles = mapOf("lesson_002" to File(root, "reference_modes.json"))
        timelineFiles = mapOf(("lesson_002" to "phan_xa") to File(root, "timeline_phan_xa.json"))
        if (withNotes) notesFiles = mapOf("lesson_002" to File(root, "notes_modes.json"), "lesson_001" to File(root, "notes.json"))
    }

    @Test
    fun `lesson 2 notes list 3 patterns, every question and every items word`() = runTest(dispatcher) {
        val viewModel = NotesViewModel(repository())
        val expectedQuestions = CourseJson.notes(File(root, "notes_modes.json").readText()).questions.size
        val expectedWords = CourseJson.items(File(root, "items_modes.json").readText()).words.size

        viewModel.load("lesson_002")
        advanceUntilIdle()
        // File IO runs on Dispatchers.IO; wait for it.
        waitUntil { viewModel.uiState.value.lessonId != null }

        val state = viewModel.uiState.value
        assertEquals(3, state.patterns.size)
        assertEquals(expectedQuestions, state.questions.size)
        assertEquals(expectedWords, state.words.size)
        assertEquals(88, state.words.size)
    }

    @Test
    fun `lesson 1 notes list 7 patterns`() = runTest(dispatcher) {
        val viewModel = NotesViewModel(repository())
        viewModel.load("lesson_001")
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.lessonId != null }
        assertEquals(7, viewModel.uiState.value.patterns.size)
    }

    @Test
    fun `no notes file means no button on picker or player and an empty notes state`() = runTest(dispatcher) {
        val repo = repository(withNotes = false)
        val picker = ModePickerViewModel(repo)
        picker.load("lesson_002")
        advanceUntilIdle()
        assertFalse(picker.uiState.value.notesAvailable)

        val connection = FakePlayerConnection()
        val player = PlayerViewModel(repo, FakeSettingsRepository(), connection).also { it.backgroundDispatcher = dispatcher }
        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "phan_xa"))
        advanceUntilIdle()
        assertFalse(player.uiState.value.notesAvailable)

        val notes = NotesViewModel(repo)
        notes.load("lesson_002")
        advanceUntilIdle()
        Thread.sleep(100)
        advanceUntilIdle()
        assertEquals(NotesUiState(), notes.uiState.value)
    }

    @Test
    fun `notes present enables picker and player buttons`() = runTest(dispatcher) {
        val repo = repository()
        val picker = ModePickerViewModel(repo)
        picker.load("lesson_002")
        advanceUntilIdle()
        assertTrue(picker.uiState.value.notesAvailable)

        val connection = FakePlayerConnection()
        val player = PlayerViewModel(repo, FakeSettingsRepository(), connection).also { it.backgroundDispatcher = dispatcher }
        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "phan_xa"))
        advanceUntilIdle()
        assertTrue(player.uiState.value.notesAvailable)
    }

    @Test
    fun `opening notes while playing does not pause or touch playback`() = runTest(dispatcher) {
        val repo = repository()
        val connection = FakePlayerConnection()
        val player = PlayerViewModel(repo, FakeSettingsRepository(), connection).also { it.backgroundDispatcher = dispatcher }
        connection.emit(PlaybackState(connected = true, playing = true, lessonId = "lesson_002", track = "phan_xa", positionMs = 5_000))
        advanceUntilIdle()

        val notes = NotesViewModel(repo)
        notes.load("lesson_002")
        advanceUntilIdle()
        waitUntil { notes.uiState.value.lessonId != null }

        assertEquals(0, connection.pauseCalls)
        assertEquals(0, connection.toggleCalls)
        assertTrue(connection.seekToCalls.isEmpty())
        assertTrue(player.uiState.value.playing)
    }

    private fun TestScope.waitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!condition() && System.currentTimeMillis() < deadline) { Thread.sleep(10); advanceUntilIdle() }
        assertTrue("timed out", condition())
    }
}
