package com.hanziflow.ui.explain

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.NowPlayingFrame
import com.hanziflow.ui.nowPlayingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LineMenuTest {
    @get:Rule val compose = createComposeRule()

    private val zh = ReferenceLine("n1", "line", "zh-CN", "coach_zh", "我们听故事。", "wǒmen tīng gùshi.", "Mình nghe truyện.")
    private val vi = ReferenceLine("v1", "line", "vi-VN", "coach_vi", "Bây giờ mình nghe truyện.")

    private fun show(viewMode: TranscriptViewMode) {
        compose.setContent {
            NowPlayingFrame(nowPlayingState(viewMode, lines = listOf(zh, vi), cues = emptyList<TimelineCue>()))
        }
    }

    @Test
    fun `long press on a hidden Chinese line opens the menu with both items`() {
        show(TranscriptViewMode.HIDDEN)
        compose.onNodeWithText("Mình nghe truyện.").performTouchInput { longClick() }
        compose.onNodeWithText("Giải thích").assertExists()
        compose.onNodeWithText("Sao chép").assertExists()
    }

    @Test
    fun `long press on a Vietnamese narration row opens nothing`() {
        show(TranscriptViewMode.PINYIN_ONLY)
        compose.onNodeWithText("Bây giờ mình nghe truyện.").performTouchInput { longClick() }
        compose.onNodeWithText("Giải thích").assertDoesNotExist()
        compose.onNodeWithText("Sao chép").assertDoesNotExist()
    }
}
