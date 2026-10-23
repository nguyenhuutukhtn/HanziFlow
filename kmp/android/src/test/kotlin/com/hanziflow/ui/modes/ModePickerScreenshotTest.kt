package com.hanziflow.ui.modes

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
class ModePickerScreenshotTest {
    @get:Rule val compose = createComposeRule()

    @Test fun picker_dark() = capture("mode_picker_dark", true)
    @Test fun picker_light() = capture("mode_picker_light", false)

    private fun capture(name: String, dark: Boolean) {
        compose.setContent {
            HanziFlowTheme(darkTheme = dark) {
                Surface(color = MaterialTheme.colorScheme.background) { ModePickerScreen(modePickerState(), onSelect = {}) }
            }
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
