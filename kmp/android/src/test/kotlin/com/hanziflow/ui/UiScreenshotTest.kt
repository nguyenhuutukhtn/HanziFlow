package com.hanziflow.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.Token
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.MiniPlayer
import com.hanziflow.ui.player.NowPlayingScreen
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.player.TranscriptUiState
import com.hanziflow.ui.theme.HanziFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Fake-state visuals, recorded into kmp/android/build/outputs/roborazzi until the
 * user approves the screens (then move `roborazzi.outputDir` to src/test/screenshots).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun mini_player_playing() = capture("mini_player_playing") {
        MiniPlayerFrame(playing = true)
    }

    @Test
    fun mini_player_paused() = capture("mini_player_paused") {
        MiniPlayerFrame(playing = false)
    }

    @Test
    fun now_playing_hidden() = capture("now_playing_hidden") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HIDDEN))
    }

    @Test
    fun now_playing_pinyin() = capture("now_playing_pinyin") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.PINYIN_ONLY))
    }

    @Test
    fun now_playing_hanzi_pinyin() = capture("now_playing_hanzi_pinyin") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN))
    }

    @Test
    fun now_playing_hanzi_only() = capture("now_playing_hanzi_only") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_ONLY))
    }

    @Test
    fun now_playing_hanzi_pinyin_light() = capture("now_playing_hanzi_pinyin_light") {
        NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN), darkTheme = false)
    }

    @Test
    fun now_playing_hanzi_pinyin_font_scale_2() = capture("now_playing_hanzi_pinyin_font_scale_2") {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
            NowPlayingFrame(state = nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN))
        }
    }

    @Test
    fun punctuation_and_long_vietnamese() = capture("now_playing_punctuation_long_vietnamese") {
        NowPlayingFrame(
            state = nowPlayingState(
                viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                lines = punctuationLines,
                cues = punctuationCues,
                positionMs = 1_200,
            ),
        )
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        compose.setContent { content() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Composable
    private fun MiniPlayerFrame(playing: Boolean) {
        HanziFlowTheme {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Spacer(Modifier.weight(1f))
                MiniPlayer(
                    title = "Đồ ăn mang về của đồng nghiệp",
                    trackLabel = "Học có hướng dẫn",
                    playing = playing,
                    positionMs = if (playing) 754_000 else 128_000,
                    durationMs = 2_590_382,
                    onToggle = {},
                    onReplayLine = {},
                    onExpand = {},
                )
            }
        }
    }

    @Composable
    private fun NowPlayingFrame(state: PlayerUiState, darkTheme: Boolean = true) {
        HanziFlowTheme(darkTheme = darkTheme) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                NowPlayingScreen(
                    state = state,
                    onCollapse = {},
                    onSwitchTrack = {},
                    onSeekTo = {},
                    onSeekBack = {},
                    onToggle = {},
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
    }

    private fun nowPlayingState(
        viewMode: TranscriptViewMode,
        lines: List<ReferenceLine> = sampleLines,
        cues: List<TimelineCue> = sampleCues,
        positionMs: Long = 1_200,
    ) = PlayerUiState(
        connected = true,
        playing = true,
        lessonId = "lesson_001",
        lessonTitle = "Đồ ăn mang về của đồng nghiệp",
        track = "guided",
        practiceAvailable = false,
        positionMs = positionMs,
        durationMs = 2_590_382,
        speed = 1f,
        transcript = TranscriptUiState(
            lessonId = "lesson_001",
            track = "guided",
            lines = lines,
            cues = cues,
            viewMode = viewMode,
            showVietnamese = true,
        ),
    )

    private companion object {
        val sampleLines = listOf(
            ReferenceLine(
                id = "s1",
                type = "line",
                language = "zh-CN",
                speaker = "coach_zh",
                text = "这是外卖。你也要吗？",
                pinyin = "zhè shì wàimài. nǐ yě yào ma?",
                translation = "Đây là đồ ăn mang về. Bạn cũng muốn à?",
                tokens = listOf(
                    Token("这", "zhè"),
                    Token("是", "shì"),
                    Token("外卖", "wàimài"),
                    Token("。"),
                    Token("你", "nǐ"),
                    Token("也", "yě"),
                    Token("要", "yào"),
                    Token("吗", "ma"),
                    Token("？"),
                ),
            ),
            ReferenceLine(
                id = "s1_vi",
                type = "line",
                language = "vi-VN",
                speaker = "coach",
                text = "Đây là đồ ăn mang về. Bạn cũng muốn à?",
                tokens = null,
            ),
            ReferenceLine(
                id = "s2",
                type = "line",
                language = "zh-CN",
                speaker = "anna",
                text = "我要一份。谢谢！",
                pinyin = "wǒ yào yí fèn. xièxie!",
                translation = "Mình muốn một phần. Cảm ơn!",
                tokens = listOf(
                    Token("我", "wǒ"),
                    Token("要", "yào"),
                    Token("一", "yí"),
                    Token("份", "fèn"),
                    Token("。"),
                    Token("谢谢", "xièxie"),
                    Token("！"),
                ),
            ),
            ReferenceLine(
                id = "s3",
                type = "narration",
                language = "zh-CN",
                speaker = "coach_zh",
                text = "我们继续听。",
                pinyin = "wǒmen jìxù tīng.",
                translation = "Chúng ta tiếp tục nghe.",
                tokens = listOf(
                    Token("我们", "wǒmen"),
                    Token("继续", "jìxù"),
                    Token("听", "tīng"),
                    Token("。"),
                ),
            ),
        )
        val sampleCues = listOf(
            TimelineCue("s1", 0, 6_000),
            TimelineCue("s1_vi", 6_500, 9_000),
            TimelineCue("s2", 10_000, 13_000),
            TimelineCue("s3", 14_000, 17_000),
        )

        val punctuationLines = listOf(
            ReferenceLine(
                id = "long_line",
                type = "line",
                language = "zh-CN",
                speaker = "coach_zh",
                text = "这是外卖，你也要吗？我们先听，然后再说。",
                pinyin = "zhè shì wàimài, nǐ yě yào ma? wǒmen xiān tīng, ránhòu zài shuō.",
                translation = "Đây là đồ ăn mang về, bạn cũng muốn ăn cùng không? " +
                    "Chúng ta hãy nghe trước, rồi sau đó hãy nói lại thật chậm và rõ ràng.",
                tokens = listOf(
                    Token("这", "zhè"),
                    Token("是", "shì"),
                    Token("外卖", "wàimài"),
                    Token("，"),
                    Token("你", "nǐ"),
                    Token("也", "yě"),
                    Token("要", "yào"),
                    Token("吗", "ma"),
                    Token("？"),
                    Token("我们", "wǒmen"),
                    Token("先", "xiān"),
                    Token("听", "tīng"),
                    Token("，"),
                    Token("然后", "ránhòu"),
                    Token("再", "zài"),
                    Token("说", "shuō"),
                    Token("。"),
                ),
            ),
        )
        val punctuationCues = listOf(TimelineCue("long_line", 0, 12_000))
    }
}
