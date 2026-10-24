package com.hanziflow.ui.explain

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.hanziflow.ai.ChatMessage
import com.hanziflow.ai.ExplainThread
import com.hanziflow.ai.ExplainUiState
import com.hanziflow.shared.ReferenceLine
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
class ExplainScreenshotTest {
    @get:Rule val compose = createComposeRule()
    private val line = ReferenceLine("n1", "line", "zh-CN", "coach_zh", "大厅在楼里面，很大。", "dàtīng zài lóu lǐmiàn, hěn dà.", "Sảnh ở trong tòa nhà, rất lớn.")
    private val answer = """
## Ý nghĩa
Câu này nói **sảnh** nằm *trong tòa nhà*.

- `在` chỉ vị trí
- `里面` là "bên trong"

| Từ | Pinyin | Nghĩa |
|----|--------|-------|
| 大厅 | dàtīng | sảnh |
| 楼 | lóu | tòa nhà |
| 很 | hěn | rất |
""".trimIndent()

    private fun thread(vararg extra: String) = ExplainThread(
        "lesson_002", "n1", line.text, 4,
        listOf(ChatMessage("user", "PROMPT", 1), ChatMessage("assistant", answer, 2)) +
            extra.mapIndexed { i, t -> ChatMessage(if (i % 2 == 0) "user" else "assistant", t, 3L + i) },
        9,
    )

    private fun shot(name: String, dark: Boolean, vararg extra: String) {
        compose.setContent {
            HanziFlowTheme(darkTheme = dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    ExplainSheetContent(line, ExplainUiState.Ready(thread(*extra)), {}, {}, {})
                }
            }
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test fun explain_answer_light() = shot("explain_answer_light", false)
    @Test fun explain_answer_dark() = shot("explain_answer_dark", true)
    @Test fun explain_chat_dark() = shot("explain_chat_dark", true, "Tại sao dùng 在?", "Vì 在 đứng trước nơi chốn để chỉ vị trí.")
}
