package com.hanziflow.ui.modes

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.hanziflow.ui.theme.HanziFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ModePickerScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cards_show_mode_info_and_open_selected_track() {
        var selected: String? = null
        compose.setContent {
            HanziFlowTheme { ModePickerScreen(modePickerState(), onSelect = { selected = it }) }
        }

        compose.onNodeWithText("Hiểu").assertExists()
        compose.onNodeWithText("8:59").assertExists()
        compose.onNodeWithTag(ModePickerTestTag).performScrollToIndex(3)
        compose.onNodeWithText("Nói").performClick()
        assertEquals("noi", selected)
    }
}

internal fun modePickerState() = ModePickerUiState(
    lessonId = "lesson_002",
    title = "你在哪儿？",
    modes = listOf(
        ModePickerItem("hieu", "Hiểu", "Nghe lần đầu: có giải thích tiếng Việt sau mỗi câu.", 539_689),
        ModePickerItem("nghe", "Nghe", "Nghe thuần tiếng Trung — khi đi bộ, lái xe, làm việc nhà.", 310_875),
        ModePickerItem("phan_xa", "Phản xạ", "Tự trả lời câu hỏi trong khoảng lặng, rồi nghe đáp án.", 456_957),
        ModePickerItem("noi", "Nói", "Nói theo từng cụm, từ ngắn đến dài.", 345_633),
    ),
)
