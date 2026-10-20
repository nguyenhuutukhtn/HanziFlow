package com.hanziflow.ui

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.hanziflow.shared.transcript.TranscriptViewMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Fake-state visuals, recorded into kmp/android/build/outputs/roborazzi until the
 * user approves the screens (then move `roborazzi.outputDir` to src/test/screenshots).
 * Section replay / Chinese-only states live in [SectionReplayScreenshotTest].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun mini_player_playing() = compose.captureScreen("mini_player_playing") {
        MiniPlayerFrame(playing = true)
    }

    @Test
    fun mini_player_paused() = compose.captureScreen("mini_player_paused") {
        MiniPlayerFrame(playing = false)
    }

    @Test
    fun now_playing_hidden() = compose.captureScreen("now_playing_hidden") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HIDDEN))
    }

    @Test
    fun now_playing_pinyin() = compose.captureScreen("now_playing_pinyin") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.PINYIN_ONLY))
    }

    @Test
    fun now_playing_hanzi_pinyin() = compose.captureScreen("now_playing_hanzi_pinyin") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN))
    }

    @Test
    fun now_playing_hanzi_only() = compose.captureScreen("now_playing_hanzi_only") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_ONLY))
    }

    @Test
    fun now_playing_hanzi_pinyin_light() = compose.captureScreen("now_playing_hanzi_pinyin_light") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN), darkTheme = false)
    }

    @Test
    fun now_playing_hanzi_pinyin_font_scale_2() = compose.captureScreen("now_playing_hanzi_pinyin_font_scale_2") {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
            NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN))
        }
    }

    @Test
    fun punctuation_and_long_vietnamese() = compose.captureScreen("now_playing_punctuation_long_vietnamese") {
        NowPlayingFrame(
            state = nowPlayingState(
                viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                lines = punctuationLines,
                cues = punctuationCues,
                positionMs = 1_200,
            ),
        )
    }
}
