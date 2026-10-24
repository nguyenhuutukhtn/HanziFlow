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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerLessonMetaTest {
    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `lesson metadata follows switches in both directions`() = runTest {
        val catalog = CourseJson.catalog(
            File("../shared/src/commonTest/resources/contract/catalog_modes.json").readText(),
        )
        val lessons = catalog.lessons.map { lesson ->
            LessonEntity(
                lesson.id, lesson.title, lesson.level, lesson.order,
                lesson.version, catalogJson = "{}",
            )
        }
        val repository = FakeCourseRepository().apply { lessonsFlow.value = lessons }
        val connection = FakePlayerConnection()
        val viewModel = PlayerViewModel(repository, FakeSettingsRepository(), connection)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001"))
        advanceUntilIdle()
        assertEquals("Đồ ăn mang về của đồng nghiệp", viewModel.uiState.value.lessonTitle)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_002"))
        advanceUntilIdle()
        assertEquals("你在哪儿？", viewModel.uiState.value.lessonTitle)

        connection.emit(PlaybackState(connected = true, lessonId = "lesson_001"))
        advanceUntilIdle()
        assertEquals("Đồ ăn mang về của đồng nghiệp", viewModel.uiState.value.lessonTitle)
    }
}
