package com.hanziflow.ui.modes

import com.hanziflow.fakes.FakeCourseRepository
import com.hanziflow.shared.CourseJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModePickerViewModelTest {
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `real modes catalog lists shared mode labels descriptions and durations in order`() = runTest {
        val catalog = CourseJson.catalog(File("../shared/src/commonTest/resources/contract/catalog_modes.json").readText())
        val lesson = catalog.lessons.single { it.id == "lesson_002" }
        val viewModel = ModePickerViewModel(FakeCourseRepository().apply { catalogLessons = mapOf(lesson.id to lesson) })

        viewModel.load(lesson.id)

        assertEquals(listOf("hieu", "tu_vung", "nghe", "phan_xa", "noi"), viewModel.uiState.value.modes.map { it.id })
        assertEquals(listOf("Hiểu", "Từ vựng", "Nghe", "Phản xạ", "Nói"), viewModel.uiState.value.modes.map { it.label })
        assertEquals(
            listOf(
                "Nghe lần đầu: có giải thích tiếng Việt sau mỗi câu.",
                "Giải thích từ, mẫu câu và dạng câu hỏi bằng tiếng Trung.",
                "Nghe thuần tiếng Trung — khi đi bộ, lái xe, làm việc nhà.",
                "Tự trả lời câu hỏi trong khoảng lặng, rồi nghe đáp án.",
                "Nói theo từng cụm, từ ngắn đến dài.",
            ),
            viewModel.uiState.value.modes.map { it.whenToUse },
        )
        assertEquals(listOf(539_689L, 579_381L, 310_875L, 456_957L, 345_633L), viewModel.uiState.value.modes.map { it.durationMs })
    }
}
