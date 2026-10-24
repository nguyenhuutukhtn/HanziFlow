package com.hanziflow.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.theme.HanziFlowTheme

private fun previewLine(id: String, text: String, pinyin: String, translation: String) = ReferenceLine(
    id, "line", "zh", "coach_zh", text, pinyin, translation,
)

@Preview(showBackground = true)
@Composable
private fun NowPlayingPreview() {
    HanziFlowTheme {
        NowPlayingScreen(
            state = PlayerUiState(
                connected = true,
                playing = true,
                lessonId = "lesson_001",
                lessonTitle = "Đồ ăn mang về của đồng nghiệp",
                positionMs = 42_000,
                durationMs = 204_889,
                transcript = TranscriptUiState(
                    lessonId = "lesson_001",
                    lines = listOf(
                        previewLine("r1", "你好", "nǐ hǎo", "Xin chào"),
                        previewLine("r2", "我叫安娜。你呢？", "wǒ jiào ānnà. nǐ ne?", "Tôi tên là Anna. Bạn thì?"),
                        previewLine("r3", "很高兴认识你。", "hěn gāoxìng rènshi nǐ.", "Rất vui được gặp bạn。"),
                    ),
                    viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                ),
            ),
            onCollapse = {}, onSwitchTrack = {}, onSeekTo = {}, onSeekBack = {}, onToggle = {},
            onSeekForward = {}, onSpeed = {}, onCancelLoop = {}, onSeekLine = {}, onToggleLoop = {},
            onLookup = {}, onViewModeChange = {}, onVietnameseToggle = {}, onPlayLookupClip = {},
            onDismissLookup = {},
        )
    }
}
