package com.hanziflow.ui.navigation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hanziflow.ui.components.MiniPlayerTestTag
import com.hanziflow.ui.library.LibraryUiState
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.player.NowPlayingTestTag
import com.hanziflow.ui.modes.ModePickerTestTag
import com.hanziflow.ui.modes.modePickerState
import com.hanziflow.ui.review.ReviewScreenTestTag
import com.hanziflow.ui.settings.SettingsScreenTestTag
import com.hanziflow.ui.theme.HanziFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.hanziflow.data.LessonEntity
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.Clip
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.PackageFiles
import com.hanziflow.shared.Tracks

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

    @Test
    fun `lesson one opens the player directly`() {
        var linearPlays = 0
        val linear = lesson("lesson_001", "linear_v1")
        compose.setContent {
            HanziFlowTheme {
                AppShell(
                    libraryState = LibraryUiState(lessons = listOf(linear)), playerState = PlayerUiState(),
                    onEditBaseUrl = {}, onSync = {}, onDownload = {}, onCancel = {}, onDelete = {},
                    onPlayLesson = { linearPlays++ }, onToggle = {}, onReplayLine = {}, onSwitchTrack = {},
                    onSeekTo = {}, onSeekBack = {}, onSeekForward = {}, onSpeed = {}, onCancelLoop = {},
                    onSeekLine = {}, onToggleLoop = {}, onLookup = {}, onViewModeChange = {}, onVietnameseToggle = {},
                    onPlayLookupClip = {}, onDismissLookup = {},
                )
            }
        }
        compose.onNodeWithText("Nghe").performClick()
        compose.onNodeWithTag(NowPlayingTestTag).assertExists()
        org.junit.Assert.assertEquals(1, linearPlays)
    }

    @Test
    fun `modes lesson opens its picker`() {
        var openedModes = 0
        val modes = lesson("lesson_002", "modes_v1")
        compose.setContent {
            HanziFlowTheme {
                AppShell(
                    libraryState = LibraryUiState(lessons = listOf(modes)), playerState = PlayerUiState(),
                    onEditBaseUrl = {}, onSync = {}, onDownload = {}, onCancel = {}, onDelete = {},
                    onPlayLesson = {}, onToggle = {}, onReplayLine = {}, onSwitchTrack = {}, onSeekTo = {},
                    onSeekBack = {}, onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {},
                    onToggleLoop = {}, onLookup = {}, onViewModeChange = {}, onVietnameseToggle = {},
                    onPlayLookupClip = {}, onDismissLookup = {}, modePickerState = modePickerState(),
                    onOpenModePicker = { openedModes++ },
                )
            }
        }
        compose.onNodeWithText("Nghe").performClick()
        compose.onNodeWithTag(ModePickerTestTag).assertExists()
        org.junit.Assert.assertEquals(1, openedModes)
    }

    private fun lesson(id: String, format: String) = LessonEntity(
        id = id, title = id, level = "absolute_beginner", lessonOrder = 1, remoteVersion = 1,
        catalogJson = CourseJson.codec.encodeToString(
            CatalogLesson.serializer(),
            CatalogLesson(id, 1, 1, id, "absolute_beginner", "", format, Tracks(emptyMap()),
                PackageFiles(Clip("lesson.json", 1, ""), Clip("reference.json", 1, ""), Clip("items.json", 1, ""))),
        ),
        downloadState = "DOWNLOADED",
    )
}
