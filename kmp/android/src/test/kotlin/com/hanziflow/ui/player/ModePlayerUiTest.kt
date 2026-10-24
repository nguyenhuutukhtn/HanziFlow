package com.hanziflow.ui.player

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.hanziflow.shared.modes.GapState
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
class ModePlayerUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `answer gap shows countdown ring and pause action`() {
        compose.setContent {
            NowPlayingFrame(
                nowPlayingState(TranscriptViewMode.PINYIN_ONLY).copy(
                    lessonId = "lesson_002",
                    track = "phan_xa",
                    lessonFormat = "modes_v1",
                    activeGap = GapState("answer", 2_294, 3_420, "phan_xa_q2"),
                ),
            )
        }

        compose.onNodeWithTag(GapCountdownTestTag).assertExists()
        compose.onNodeWithText("Cần thêm thời gian").assertExists()
    }

    @Test
    fun `gap controls are absent outside a mode gap`() {
        compose.setContent { NowPlayingFrame(nowPlayingState(TranscriptViewMode.PINYIN_ONLY)) }

        compose.onNodeWithTag(GapCountdownTestTag).assertDoesNotExist()
        compose.onNodeWithText("Cần thêm thời gian").assertDoesNotExist()
    }

    @Test
    fun `nghe shows pure-listening reveal action instead of transcript controls`() {
        var state by mutableStateOf(nowPlayingState(TranscriptViewMode.PINYIN_ONLY, lines = emptyList()).copy(
            lessonId = "lesson_002", track = "nghe", lessonFormat = "modes_v1",
        ))
        compose.setContent { NowPlayingFrame(state) }

        compose.onNodeWithText("Nghe thuần").assertExists()
        compose.onNodeWithText("Hiện transcript").assertExists()
        compose.onNodeWithText("Pinyin").assertDoesNotExist()

        compose.runOnIdle { state = state.copy(ngheTranscriptVisible = true) }
        compose.onNodeWithText("Ẩn transcript").assertExists()
    }

    @Test
    fun `repeat chip is limited to noi and phan xa modes`() {
        var state by mutableStateOf(
            nowPlayingState(TranscriptViewMode.PINYIN_ONLY).copy(
                lessonId = "lesson_002",
                track = "noi",
                lessonFormat = "modes_v1",
            ),
        )
        compose.setContent { NowPlayingFrame(state) }
        compose.onNodeWithText("Lặp ×2").assertExists()

        compose.runOnIdle { state = state.copy(track = "phan_xa") }
        compose.onNodeWithText("Lặp ×2").assertExists()
        compose.runOnIdle { state = state.copy(track = "hieu") }
        compose.onNodeWithText("Lặp ×2").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(track = "nghe") }
        compose.onNodeWithText("Lặp ×2").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(track = "noi", lessonFormat = "linear_v1") }
        compose.onNodeWithText("Lặp ×2").assertDoesNotExist()
    }
}
