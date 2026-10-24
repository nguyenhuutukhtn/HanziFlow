package com.hanziflow.ui

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.Items
import com.hanziflow.shared.WordItem
import com.hanziflow.shared.transcript.TranscriptViewMode
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
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TranscriptInteractionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `lesson 2 Nghe reveal shows hanzi even with no item data`() {
        val fixture = File("../shared/src/commonTest/resources/contract/reference_modes.json")
        val line = CourseJson.reference(fixture.readText()).tracks.forName("nghe").orEmpty().first { it.id == "nghe_l1" }
        val base = nowPlayingState(TranscriptViewMode.PINYIN_ONLY, lines = listOf(line))
        compose.setContent { HanziFlowTheme {
            NowPlayingScreen(
                state = base.copy(
                    lessonId = "lesson_002", lessonTitle = "你在哪儿？", lessonFormat = "modes_v1", track = "nghe",
                    ngheTranscriptVisible = true,
                    transcript = base.transcript.copy(
                        lessonId = "lesson_002", track = "nghe",
                        items = Items(schemaVersion = 1, lessonId = "lesson_002", lessonVersion = 2),
                    ),
                ),
                onCollapse = {}, onSwitchTrack = {}, onSeekTo = {}, onSeekBack = {}, onToggle = {},
                onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {}, onToggleLoop = {},
                onLookup = {}, onViewModeChange = {}, onVietnameseToggle = {}, onPlayLookupClip = {},
                onDismissLookup = {},
            )
        } }

        compose.onNodeWithText("我们听故事。").assertExists()
    }

    @Test
    fun `long pressing a lesson 1 token copies only the full line hanzi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.clearPrimaryClip()
        compose.setContent { HanziFlowTheme { NowPlayingFrame(nowPlayingState(TranscriptViewMode.HANZI_ONLY)) } }

        compose.onNodeWithText("这").performTouchInput { longClick() }
        compose.onNodeWithText("Sao chép").performClick()

        assertEquals("这是外卖。你也要吗？", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        assertEquals("Đã sao chép", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `tapping a lesson 1 word still opens lookup`() {
        var lookedUp: String? = null
        val line = sampleLines.first().copy(tokens = sampleLines.first().tokens?.mapIndexed { index, token ->
            if (index == 0) token.copy(itemId = "word_zhe") else token
        })
        val base = nowPlayingState(TranscriptViewMode.HANZI_ONLY, lines = listOf(line))
        compose.setContent { HanziFlowTheme {
            NowPlayingScreen(
                state = base.copy(transcript = base.transcript.copy(items = Items(
                    schemaVersion = 1, lessonId = "lesson_001", lessonVersion = 1,
                    words = listOf(WordItem("word_zhe", "这", "zhè", emptyList(), "này")),
                ))),
                onCollapse = {}, onSwitchTrack = {}, onSeekTo = {}, onSeekBack = {}, onToggle = {},
                onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {}, onToggleLoop = {},
                onLookup = { lookedUp = it }, onViewModeChange = {}, onVietnameseToggle = {},
                onPlayLookupClip = {}, onDismissLookup = {},
            )
        } }

        compose.onNodeWithText("这").performClick()
        compose.runOnIdle { assertEquals("word_zhe", lookedUp) }
    }
}
