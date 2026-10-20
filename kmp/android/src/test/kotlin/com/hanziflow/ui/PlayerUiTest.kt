package com.hanziflow.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.Token
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.MiniPlayerOpenTestTag
import com.hanziflow.ui.library.LibraryUiState
import com.hanziflow.ui.navigation.AppShell
import com.hanziflow.ui.player.NowPlayingScreen
import com.hanziflow.ui.player.NowPlayingTestTag
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.player.TranscriptUiState
import com.hanziflow.ui.theme.HanziFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlayerUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `tapping the mini-player opens Now Playing`() {
        val playerState = PlayerUiState(
            connected = true,
            playing = true,
            lessonId = "lesson_001",
            lessonTitle = "Đồ ăn mang về của đồng nghiệp",
            track = "guided",
            positionMs = 12_000,
            durationMs = 204_889,
        )
        compose.setContent {
            HanziFlowTheme {
                AppShell(
                    libraryState = LibraryUiState(),
                    playerState = playerState,
                    onEditBaseUrl = {},
                    onSync = {},
                    onDownload = {},
                    onCancel = {},
                    onDelete = {},
                    onPlayLesson = {},
                    onToggle = {},
                    onReplayLine = {},
                    onSwitchTrack = {},
                    onSeekTo = {},
                    onSeekBack = {},
                    onSeekForward = {},
                    onSpeed = {},
                    onCancelLoop = {},
                    onSeekLine = {},
                    onToggleLoop = {},
                    onLookup = {},
                    onViewModeChange = {},
                    onVietnameseToggle = {},
                    onPlayLookupClip = {},
                    onDismissLookup = {},
                )
            }
        }

        compose.onNodeWithTag(MiniPlayerOpenTestTag).performClick()
        compose.onNodeWithTag(NowPlayingTestTag).assertExists()
    }

    @Test
    fun `model_answer is a placeholder before its cue`() {
        val answer = ReferenceLine(
            id = "answer",
            type = "model_answer",
            language = "zh-CN",
            speaker = "coach_zh",
            text = "这是安娜的外卖。",
            pinyin = "zhè shì Ānnà de wàimài.",
            translation = "Đây là đồ ăn mang về của Anna.",
            tokens = listOf(
                Token("这", "zhè"),
                Token("是", "shì"),
                Token("安娜", "Ānnà"),
                Token("的", "de"),
                Token("外卖", "wàimài"),
                Token("。"),
            ),
        )
        var playerState by mutableStateOf(
            PlayerUiState(
                connected = true,
                lessonId = "lesson_001",
                lessonTitle = "Bài kiểm tra",
                track = "guided",
                positionMs = 1_000,
                durationMs = 10_000,
                transcript = TranscriptUiState(
                    lessonId = "lesson_001",
                    track = "guided",
                    lines = listOf(answer),
                    cues = listOf(TimelineCue("answer", 5_000, 8_000)),
                    viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                    showVietnamese = true,
                ),
            ),
        )
        compose.setContent {
            HanziFlowTheme {
                NowPlayingScreen(
                    state = playerState,
                    onCollapse = {},
                    onSwitchTrack = {},
                    onSeekTo = {},
                    onSeekBack = {},
                    onToggle = {},
                    onSeekForward = {},
                    onSpeed = {},
                    onCancelLoop = {},
                    onSeekLine = {},
                    onToggleLoop = {},
                    onLookup = {},
                    onViewModeChange = {},
                    onVietnameseToggle = {},
                    onPlayLookupClip = {},
                    onDismissLookup = {},
                )
            }
        }

        compose.onNodeWithText("Đến lượt bạn — nói trước khi nghe").assertExists()
        compose.onNodeWithText("这是安娜的外卖。").assertDoesNotExist()
    }
}
