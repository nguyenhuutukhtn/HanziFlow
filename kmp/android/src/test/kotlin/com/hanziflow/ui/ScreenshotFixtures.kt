package com.hanziflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.Token
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.MiniPlayer
import com.hanziflow.ui.player.NowPlayingScreen
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.player.TranscriptUiState
import com.hanziflow.ui.theme.HanziFlowTheme

/** Fake-state fixtures shared by the Roborazzi screenshot tests (kept out of the test files for the line budget). */
fun ComposeContentTestRule.captureScreen(name: String, content: @Composable () -> Unit) {
    setContent { content() }
    waitForIdle()
    onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
}

@Composable
fun MiniPlayerFrame(
    playing: Boolean,
    sectionLoop: com.hanziflow.playback.SectionLoopInfo? = null,
) {
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
                sectionLoop = sectionLoop,
                onStopSectionLoopAfterPass = {},
                onNextSection = {},
            )
        }
    }
}

@Composable
fun NowPlayingFrame(state: PlayerUiState, darkTheme: Boolean = true) {
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
                onChineseOnlyToggle = {},
                onLoopSection = {},
                onStopSectionLoopAfterPass = {},
                onNextSection = {},
                onSeekToSection = {},
            )
        }
    }
}

fun nowPlayingState(
    viewMode: TranscriptViewMode,
    lines: List<ReferenceLine> = sampleLines,
    cues: List<TimelineCue> = sampleCues,
    sections: List<TimelineSection> = emptyList(),
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
        sections = sections,
        viewMode = viewMode,
        showVietnamese = true,
    ),
)

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

val sampleSections = listOf(
    TimelineSection("story", "Nghe chuyện", 0, 10_000),
    TimelineSection("qa", "Hỏi đáp", 10_000, 2_590_382),
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
