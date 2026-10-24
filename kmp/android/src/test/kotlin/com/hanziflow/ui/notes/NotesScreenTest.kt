package com.hanziflow.ui.notes

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.modes.ModePickerNotesTestTag
import com.hanziflow.ui.modes.ModePickerScreen
import com.hanziflow.ui.modes.modePickerState
import com.hanziflow.ui.nowPlayingState
import com.hanziflow.ui.player.NotesButtonTestTag
import com.hanziflow.ui.player.NowPlayingScreen
import com.hanziflow.ui.theme.HanziFlowTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

internal val contractRoot = File("../shared/src/commonTest/resources/contract")

/** Lesson 2 notes built from the real contract fixtures (notes_modes.json + items_modes.json). */
internal fun lesson2NotesState(): NotesUiState {
    val notes = CourseJson.notes(File(contractRoot, "notes_modes.json").readText())
    val items = CourseJson.items(File(contractRoot, "items_modes.json").readText())
    return NotesUiState("lesson_002", "你在哪儿？", notes.patterns, notes.questions, items.words)
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NotesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `sections appear in order patterns then questions then vocabulary with everything visible`() {
        val state = lesson2NotesState()
        compose.setContent { HanziFlowTheme { NotesScreen(state) } }

        // Index 0 = title, 1 = "Mẫu câu", 4 = "Câu hỏi" (after 3 patterns), then its questions, then vocabulary.
        compose.onNodeWithText("Mẫu câu").assertIsDisplayed()
        compose.onNodeWithText("在 + nơi chốn").assertIsDisplayed()
        compose.onNodeWithText(state.patterns.first().explainVi).assertIsDisplayed()
        compose.onNodeWithText("wǒ zài bā lóu.").assertIsDisplayed()
        compose.onNodeWithText("Tôi ở tầng tám.").assertIsDisplayed()
        val questionsIndex = 1 + state.patterns.size + 1
        compose.onNodeWithTag(NotesTestTag).performScrollToIndex(questionsIndex)
        compose.onNodeWithText("Câu hỏi").assertIsDisplayed()
        val vocabIndex = questionsIndex + state.questions.size + 1
        compose.onNodeWithTag(NotesTestTag).performScrollToIndex(vocabIndex)
        compose.onNodeWithText("Từ vựng (88)").assertIsDisplayed()
        compose.onNodeWithText(state.words.first().meaningVi).assertIsDisplayed()
    }

    @Test
    fun `long pressing an example copies its hanzi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.clearPrimaryClip()
        compose.setContent { HanziFlowTheme { NotesScreen(lesson2NotesState()) } }

        compose.onNodeWithText("我在八楼。").performTouchInput { longClick() }

        assertEquals("我在八楼。", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
    }

    @Test
    fun `player header shows Ghi chu only when notes exist`() {
        var withNotes by mutableStateOf(false)
        var opened = 0
        compose.setContent {
            HanziFlowTheme {
                NowPlayingScreen(
                    state = nowPlayingState(TranscriptViewMode.PINYIN_ONLY).copy(notesAvailable = withNotes),
                    onCollapse = {}, onSwitchTrack = {}, onSeekTo = {}, onSeekBack = {}, onToggle = {},
                    onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {}, onToggleLoop = {},
                    onLookup = {}, onViewModeChange = {}, onVietnameseToggle = {}, onPlayLookupClip = {},
                    onDismissLookup = {}, onOpenNotes = { opened++ },
                )
            }
        }
        compose.onNodeWithTag(NotesButtonTestTag).assertDoesNotExist()

        compose.runOnIdle { withNotes = true }
        compose.onNodeWithTag(NotesButtonTestTag).performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `mode picker shows Ghi chu only when notes exist`() {
        var opened = 0
        var withNotes by mutableStateOf(false)
        compose.setContent {
            HanziFlowTheme {
                ModePickerScreen(modePickerState().copy(notesAvailable = withNotes), onSelect = {}, onOpenNotes = { opened++ })
            }
        }
        compose.onNodeWithTag(ModePickerNotesTestTag).assertDoesNotExist()

        compose.runOnIdle { withNotes = true }
        compose.onNodeWithTag(ModePickerNotesTestTag).performClick()
        assertEquals(1, opened)
    }

    @Test
    fun `lesson 2 Hieu tapping a word opens lookup with its meaning`() {
        val reference = CourseJson.reference(File(contractRoot, "reference_modes.json").readText())
        val line = reference.tracks.forName("hieu").orEmpty().first { it.id == "hieu_h4" }
        val items = CourseJson.items(File(contractRoot, "items_modes.json").readText())
        val base = nowPlayingState(TranscriptViewMode.HANZI_ONLY, lines = listOf(line), cues = emptyList())
        var state by mutableStateOf(
            base.copy(
                lessonId = "lesson_002", lessonFormat = "modes_v1", track = "hieu",
                transcript = base.transcript.copy(lessonId = "lesson_002", track = "hieu", items = items),
            ),
        )
        compose.setContent {
            HanziFlowTheme {
                NowPlayingScreen(
                    state = state,
                    onCollapse = {}, onSwitchTrack = {}, onSeekTo = {}, onSeekBack = {}, onToggle = {},
                    onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {}, onToggleLoop = {},
                    onLookup = { id ->
                        val word = items.words.first { it.id == id }
                        state = state.copy(transcript = state.transcript.copy(lookupWord = word, lookupLineText = line.text))
                    },
                    onViewModeChange = {}, onVietnameseToggle = {}, onPlayLookupClip = {}, onDismissLookup = {},
                )
            }
        }

        compose.onNodeWithText("họ Vương").assertDoesNotExist()
        compose.onNodeWithText("王").performClick()
        compose.onNodeWithText("họ Vương").assertIsDisplayed()
    }
}
