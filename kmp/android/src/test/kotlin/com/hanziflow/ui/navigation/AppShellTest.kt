package com.hanziflow.ui.navigation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.hanziflow.ui.components.MiniPlayerTestTag
import com.hanziflow.ui.library.LibraryUiState
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.review.ReviewScreenTestTag
import com.hanziflow.ui.settings.SettingsScreenTestTag
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
class AppShellTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `mini-player appears when a lesson is loaded and survives tab switches`() {
        var playerState by mutableStateOf(PlayerUiState())
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

        // Nothing loaded: no mini-player chrome.
        compose.onNodeWithTag(MiniPlayerTestTag).assertDoesNotExist()

        // A lesson starts playing (e.g. after the user tapped Listen on a downloaded card).
        compose.runOnIdle {
            playerState = PlayerUiState(
                connected = true,
                playing = true,
                lessonId = "lesson_001",
                lessonTitle = "Đồ ăn mang về của đồng nghiệp",
                track = "guided",
                positionMs = 12_000,
                durationMs = 204_889,
            )
        }
        compose.onNodeWithTag(MiniPlayerTestTag).assertExists()

        compose.onNodeWithTag("nav_review").performClick()
        compose.onNodeWithTag(ReviewScreenTestTag).assertExists()
        compose.onNodeWithTag(MiniPlayerTestTag).assertExists()

        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag(SettingsScreenTestTag).assertExists()
        compose.onNodeWithTag(MiniPlayerTestTag).assertExists()

        compose.onNodeWithTag("nav_learn").performClick()
        compose.onNodeWithTag(MiniPlayerTestTag).assertExists()
    }
}
