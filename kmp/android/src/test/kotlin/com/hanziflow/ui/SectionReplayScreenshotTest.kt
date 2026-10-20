package com.hanziflow.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.hanziflow.playback.SectionLoopInfo
import com.hanziflow.shared.transcript.TranscriptViewMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Section replay / Chinese-only visuals; split out of [UiScreenshotTest] for the line budget. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SectionReplayScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun now_playing_section_loop() = compose.captureScreen("now_playing_section_loop") {
        NowPlayingFrame(
            state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN, sections = sampleSections).copy(
                sectionLoop = SectionLoopInfo("story", "Nghe chuyện", pass = 2, stopAfterPass = false),
            ),
        )
    }

    @Test
    fun now_playing_chinese_only() = compose.captureScreen("now_playing_chinese_only") {
        NowPlayingFrame(
            state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN, sections = sampleSections).let {
                it.copy(chineseOnly = true, transcript = it.transcript.copy(showVietnamese = false))
            },
        )
    }

    @Test
    fun mini_player_section_loop() = compose.captureScreen("mini_player_section_loop") {
        MiniPlayerFrame(playing = true, sectionLoop = SectionLoopInfo("story", "Nghe chuyện", pass = 3, stopAfterPass = false))
    }
}
