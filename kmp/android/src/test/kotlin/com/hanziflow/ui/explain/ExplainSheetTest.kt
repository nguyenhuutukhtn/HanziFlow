package com.hanziflow.ui.explain

import android.app.Application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.hanziflow.ai.ChatMessage
import com.hanziflow.ai.ExplainError
import com.hanziflow.ai.ExplainTestEnv
import com.hanziflow.ai.ExplainThread
import com.hanziflow.ai.ExplainUiState
import com.hanziflow.ai.ExplainViewModel
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.NowPlayingFrame
import com.hanziflow.ui.nowPlayingState
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ai.ExplainTarget
import kotlinx.coroutines.runBlocking
import org.junit.After
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
class ExplainSheetTest {
    @get:Rule val compose = createComposeRule()
    private val env = ExplainTestEnv()
    private val chat = env.chat
    private val line = ReferenceLine("n1", "line", "zh-CN", "coach_zh", "我们听故事。", "wǒmen tīng gùshi.", "Mình nghe truyện.")

    @After fun close() { env.close() }

    private fun viewModel(configured: Boolean = true) = env.viewModel(configured)

    /** Player screen whose explain sheet is wired to a real ViewModel on a fake chat client. */
    private fun showPlayer(vm: ExplainViewModel) {
        compose.setContent {
            NowPlayingFrame(
                nowPlayingState(TranscriptViewMode.HIDDEN, lines = listOf(line), cues = emptyList()),
                explainSheet = { l, dismiss ->
                    val ui by vm.state.collectAsState()
                    LaunchedEffect(l.id) {
                        vm.open(ExplainTarget("lesson_001", 1, l))
                    }
                    ExplainSheet(l, ui, vm::send, vm::retry, vm::reexplain, dismiss)
                },
            )
        }
    }

    private fun openSheet() {
        compose.onNodeWithText("Mình nghe truyện.").performTouchInput { longClick() }
        compose.onNodeWithText("Giải thích").performClick()
    }

    @Test
    fun `explain shows the recorded answer, a follow-up appends, reopening makes no new call`() {
        showPlayer(viewModel())
        openSheet()
        compose.waitUntil(5_000) { has("lời giải thích") }
        compose.onNodeWithText("Giải thích câu này").assertExists()
        assertEquals(1, chat.calls)

        compose.onNodeWithTag(ExplainInputTag).performTextInput("Tại sao dùng 我们?")
        compose.onNodeWithTag(ExplainSendTag).performClick()
        compose.onNodeWithText("Tại sao dùng 我们?", useUnmergedTree = true).assertExists()
        compose.waitUntil(5_000) { has("Trả lời số 2") }
        assertEquals(2, chat.calls)

    }

    @Test
    fun `reopening a line with a saved thread shows it with no new client call`() {
        runBlocking { env.repository().explain(ExplainTarget("lesson_001", 1, line)).getOrThrow() }
        assertEquals(1, chat.calls)
        showPlayer(viewModel())
        openSheet()
        compose.waitUntil(5_000) { has("lời giải thích") }
        assertEquals(1, chat.calls)
    }

    private fun has(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `failed first explanation shows retry`() {
        chat.fail = true
        showPlayer(viewModel())
        openSheet()
        compose.waitUntil(5_000) { has("Thử lại") }
    }

    @Test
    fun `not configured shows the config hint`() {
        showPlayer(viewModel(configured = false))
        openSheet()
        compose.onNodeWithText("Chưa cấu hình AI (llm.* trong local.properties)").assertExists()
    }

    @Test
    fun `content states render without a sheet`() {
        val thread = ExplainThread("l", "n1", "我们听故事。", 1, listOf(
            ChatMessage("user", "PROMPT-SECRET", 1), ChatMessage("assistant", "Câu trả lời", 2),
        ), 2, isStale = true)
        compose.setContent {
            HanziFlowTheme { ExplainSheetContent(line, ExplainUiState.Ready(thread), {}, {}, {}) }
        }
        compose.onNodeWithText("PROMPT-SECRET").assertDoesNotExist()
        compose.onNodeWithText("Câu này đã thay đổi trong bài mới").assertExists()
        compose.onNodeWithText("Giải thích lại").assertExists()
        compose.onNodeWithText("我们听故事。").assertExists()
    }

    @Test
    fun `quota failure shows the friendly text and an answer shows its model`() {
        val thread = ExplainThread("l", "n1", "我们听故事。", 1, listOf(
            ChatMessage("user", "P", 1), ChatMessage("assistant", "Câu trả lời", 2, model = "gemini-test"),
        ), 2)
        compose.setContent {
            HanziFlowTheme {
                ExplainSheetContent(line, ExplainUiState.Ready(thread, error = ExplainError(ExplainError.Kind.QUOTA, 429)), {}, {}, {})
            }
        }
        compose.onNodeWithText("gemini-test").assertExists()
        compose.onNodeWithText("Hết lượt miễn phí của AI. Thử lại sau ít phút hoặc thêm nhà cung cấp khác.").assertExists()
    }

    @Test
    fun `failed first explanation with quota shows the quota text`() {
        compose.setContent {
            HanziFlowTheme { ExplainSheetContent(line, ExplainUiState.Failed(ExplainError(ExplainError.Kind.QUOTA, 429)), {}, {}, {}) }
        }
        compose.onNodeWithText("Hết lượt miễn phí của AI. Thử lại sau ít phút hoặc thêm nhà cung cấp khác.").assertExists()
    }
}
