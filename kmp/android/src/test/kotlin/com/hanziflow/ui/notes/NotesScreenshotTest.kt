package com.hanziflow.ui.notes

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.NowPlayingFrame
import com.hanziflow.ui.nowPlayingState
import com.hanziflow.ui.theme.HanziFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NotesScreenshotTest {
    @get:Rule val compose = createComposeRule()

    @Test fun notes_lesson2_dark() = notes("notes_lesson2_dark", true)
    @Test fun notes_lesson2_light() = notes("notes_lesson2_light", false)

    @Test
    fun player_header_with_notes_button() {
        compose.setContent {
            NowPlayingFrame(nowPlayingState(TranscriptViewMode.PINYIN_ONLY).copy(notesAvailable = true))
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/player_header_notes.png")
    }

    private fun notes(name: String, dark: Boolean) {
        compose.setContent {
            HanziFlowTheme(darkTheme = dark) {
                Surface(color = MaterialTheme.colorScheme.background) { NotesScreen(lesson2NotesState()) }
            }
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
