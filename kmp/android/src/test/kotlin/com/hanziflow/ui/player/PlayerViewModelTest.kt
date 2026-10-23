package com.hanziflow.ui.player

import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.fakes.FakePlayerConnection
import com.hanziflow.fakes.FakeProgressRepository
import com.hanziflow.fakes.FakeSettingsRepository
import com.hanziflow.playback.PlaybackState
import com.hanziflow.playback.SectionLoopInfo
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.Clip
import com.hanziflow.shared.PackageFiles
import com.hanziflow.shared.Track
import com.hanziflow.shared.Tracks
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
            """{"schema_version":1,"lesson_id":"$lessonId","lesson_version":1,"track":"$track","duration_ms":5000,
               |"cues":[{"line_id":"r1_food","start_ms":1000,"end_ms":3000}],
               |"sections":[{"id":"story","title":"Nghe chuyện","start_ms":0,"end_ms":5000}]}
            """.trimMargin(),
        )
        val items = tempJson(
            "items",
            """{"schema_version":1,"lesson_id":"$lessonId","lesson_version":1,"words":[]}""",
        )
        return FakeCourseRepository().apply {
            lessonsFlow.value = listOf(
                com.hanziflow.data.LessonEntity(lessonId, "Lesson", "A1", 1, 1, catalogJson = "{}"),
            )
            referenceFiles = mapOf(lessonId to reference)
            timelineFiles = mapOf((lessonId to track) to timeline)
            trackFiles = mapOf((lessonId to "guided_zh") to tempJson("guided_zh", "{}"))
            itemsFiles = mapOf(lessonId to items)
            catalogLessons = mapOf(
                lessonId to CatalogLesson(
                    lessonId, 1, 1, "Lesson", "A1", "", "linear_v1",
                    Tracks(mapOf(
                        "guided" to Track("guided.mp3", 1, 5_000, "", Clip("timeline_guided.json", 1, "")),
                        "guided_zh" to Track("guided_zh.mp3", 1, 4_000, "", Clip("timeline_guided_zh.json", 1, "")),
                    )),
                    PackageFiles(Clip("lesson.json", 1, ""), Clip("reference.json", 1, ""), Clip("items.json", 1, "")),
                ),
            )
        }
    }

    @Test
    fun `character index stays null when repository has no character file`() = runTest {
        val repository = FakeCourseRepository().apply {
            lessonsFlow.value = listOf(
                com.hanziflow.data.LessonEntity(
                    id = "lesson_001",
                    title = "Lesson",
                    level = "A1",
                    lessonOrder = 1,
                    remoteVersion = 1,
                    catalogJson = "{}",
                ),
            )
        }
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)
        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001"))
        Thread.sleep(50)
        assertNull(viewModel.uiState.value.characterIndex)
    }

    @Test
    fun `character index loads a fixture and preserves word order`() = runTest {
        val repository = FakeCourseRepository().apply {
            charactersFiles = mapOf("lesson_001" to tempJson("characters", """{"lesson_id":"lesson_001","characters":[{"char":"我","pinyin":"wǒ","han_viet":"ngã","vi_meaning":"tôi, mình"},{"char":"们","pinyin":"men","han_viet":"môn","vi_meaning":"chúng tôi"}],"components_prereq":[]}"""))
        }
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)
        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001"))
        val deadline = System.currentTimeMillis() + 2_000
        while (viewModel.uiState.value.characterIndex == null && System.currentTimeMillis() < deadline) Thread.sleep(10)
        val index = viewModel.uiState.value.characterIndex
        assertEquals(listOf("我", "们"), index?.charactersOf("我们")?.map { it.char })
    }

    @Test
    fun `stroke pack failure keeps the character index and hides strokes`() = runTest {
        val repository = FakeCourseRepository().apply {
            charactersFiles = mapOf("lesson_001" to tempJson("characters", """{"lesson_id":"lesson_001","characters":[{"char":"我","pinyin":"wǒ","han_viet":"ngã","vi_meaning":"tôi"}],"components_prereq":[]}"""))
            strokePackError = IllegalStateException("offline")
        }
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001"))
        val deadline = System.currentTimeMillis() + 2_000
        while (viewModel.uiState.value.characterIndex == null && System.currentTimeMillis() < deadline) Thread.sleep(10)

        assertEquals("我", viewModel.uiState.value.characterIndex?.entry("我")?.char)
        assertNull(viewModel.uiState.value.strokePack)
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

    @Test
    fun `mode track resumes its own position and clears it at the end`() = runTest {
        val repository = repositoryWithTranscript("lesson_002", "noi")
        val connection = FakePlayerConnection()
        val progress = FakeProgressRepository().also { it.savePosition("lesson_002", "noi", 12_000) }
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), progress, connection)
        val lesson = repository.lessonsFlow.value.single()

        repository.trackFiles = mapOf("lesson_002" to "noi" to tempJson("noi", "audio"))
        viewModel.play(lesson, "noi")
        Thread.sleep(50)

        assertEquals("noi", connection.playCalls.single()[1])
        assertEquals(listOf(12_000L), connection.seekToCalls)
        assertEquals(listOf("lesson_002" to "noi"), repository.timelineFileCalls)

        connection.emit(connection.state.value.copy(positionMs = 5_000, durationMs = 5_000))
        Thread.sleep(50)
        assertEquals(0L, progress.stored("lesson_002", "noi"))
    }

    @Test
    fun `chinese-only toggle forwards to the connection, forces Vietnamese off, and persists per track`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val settings = FakeSettingsRepository()
        val viewModel = PlayerViewModel(repository, settings, connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001", track = "guided", positionMs = 0))
        awaitTranscriptLoaded(viewModel)
        Thread.sleep(50)

        viewModel.setChineseOnly(true)

        assertEquals(listOf("guided", "guided_zh"), connection.switchTrackCalls)
        assertEquals(false, viewModel.uiState.value.transcript.showVietnamese)
        Thread.sleep(50)
        assertEquals(true, settings.chineseOnlyEnabled("lesson_001", "guided"))
    }

    @Test
    fun `section loop commands forward to the connection for the current section`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        awaitTranscriptLoaded(viewModel)

        viewModel.loopCurrentSection()
        assertEquals(SectionLoopInfo("story", "Nghe chuyện", pass = 1, stopAfterPass = false), connection.loopSectionCalls.single())

        viewModel.stopSectionLoopAfterPass()
        assertEquals(1, connection.stopSectionLoopAfterPassCalls)

        viewModel.nextSection()
        assertEquals(1, connection.nextSectionCalls)
    }

    @Test
    fun `seeking to a section by id seeks to its start`() = runTest {
        val repository = repositoryWithTranscript("lesson_001")
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001", track = "guided", positionMs = 1_500))
        awaitTranscriptLoaded(viewModel)

        viewModel.seekToSection("story")
        assertEquals(listOf(0L), connection.seekToCalls)
    }
}
