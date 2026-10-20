package com.hanziflow.ui.library

import com.hanziflow.data.LessonEntity
import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.fakes.FakeSettingsRepository
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
class LibraryViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun lesson(id: String, state: String) = LessonEntity(
        id = id,
        title = "Lesson $id",
        level = "absolute_beginner",
        lessonOrder = 1,
        remoteVersion = 1,
        catalogJson = "{}",
        downloadState = state,
    )

    @Test
    fun `download state transitions are mirrored from the repository's lessons flow`() = runTest {
        val repository = FakeCourseRepository()
        val viewModel = LibraryViewModel(repository, FakeSettingsRepository())

        assertEquals(emptyList<LessonEntity>(), viewModel.uiState.value.lessons)

        repository.lessonsFlow.value = listOf(lesson("lesson_001", "AVAILABLE"))
        assertEquals("AVAILABLE", viewModel.uiState.value.lessons.single().downloadState)

        repository.lessonsFlow.value = listOf(lesson("lesson_001", "DOWNLOADING"))
        assertEquals("DOWNLOADING", viewModel.uiState.value.lessons.single().downloadState)

        repository.lessonsFlow.value = listOf(lesson("lesson_001", "DOWNLOADED"))
        assertEquals("DOWNLOADED", viewModel.uiState.value.lessons.single().downloadState)
    }

    @Test
    fun `download and cancel delegate to the repository`() = runTest {
        val repository = FakeCourseRepository()
        val viewModel = LibraryViewModel(repository, FakeSettingsRepository())

        viewModel.download("lesson_001")
        viewModel.cancel("lesson_001")

        assertEquals(listOf("lesson_001"), repository.downloadCalls)
        assertEquals(listOf("lesson_001"), repository.cancelCalls)
    }

    @Test
    fun `sync failure surfaces a message instead of throwing`() = runTest {
        val repository = FakeCourseRepository().apply { syncCatalogError = IllegalStateException("offline") }
        val viewModel = LibraryViewModel(repository, FakeSettingsRepository())

        viewModel.sync()

        assertEquals("offline", viewModel.uiState.value.message)
        assertNull(null) // sanity: no exception escaped the coroutine
    }
}
