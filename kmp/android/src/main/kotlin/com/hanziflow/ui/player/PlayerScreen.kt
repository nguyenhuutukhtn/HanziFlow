package com.hanziflow.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.transcript.TranscriptViewMode

/**
 * Stateless now-playing card: state in, events out. [PlayerViewModel] is the only place
 * that talks to [com.hanziflow.playback.PlayerConnection] / Media3 types.
 */
@Composable
fun PlayerCard(
    state: PlayerUiState,
    onSwitchTrack: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeed: (Float) -> Unit,
    onCancelLoop: () -> Unit,
    onSeekLine: (String) -> Unit,
    onToggleLoop: (String) -> Unit,
    onLookup: (String) -> Unit,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
    onPlayLookupClip: () -> Unit,
    onDismissLookup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transcript = state.transcript
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Now playing: ${state.lessonTitle ?: state.lessonId}", style = MaterialTheme.typography.titleMedium)
            if (state.practiceAvailable) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.track == "guided", onClick = { onSwitchTrack("guided") }, label = { Text("Guided") })
                FilterChip(selected = state.track == "practice", onClick = { onSwitchTrack("practice") }, label = { Text("Practice") })
            }
            Slider(
                value = state.positionMs.toFloat().coerceAtMost(state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { onSeekTo(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onSeekBack) { Text("−10 s") }
                Button(onClick = onToggle) { Text(if (state.playing) "Pause" else "Play") }
                OutlinedButton(onClick = onSeekForward) { Text("+10 s") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.8f, 0.9f, 1f, 1.1f).forEach { speed ->
                    FilterChip(
                        selected = state.speed == speed,
                        onClick = { onSpeed(speed) },
                        label = { Text("${speed}×") },
                    )
                }
            }
            transcript.loopedLineId?.let { loopedLineId ->
                val loopedText = transcript.lines.firstOrNull { it.id == loopedLineId }?.text ?: loopedLineId
                AssistChip(
                    onClick = onCancelLoop,
                    label = { Text("Looping: $loopedText") },
                    trailingIcon = { Text("×", style = MaterialTheme.typography.titleMedium) },
                )
            }
            if (transcript.lessonId == state.lessonId && transcript.lines.isNotEmpty()) {
                TranscriptPanel(
                    lines = transcript.lines,
                    cues = transcript.cues,
                    items = transcript.items,
                    positionMs = state.positionMs,
                    viewMode = transcript.viewMode,
                    showVietnamese = transcript.showVietnamese,
                    loopedLineId = transcript.loopedLineId,
                    onSeek = onSeekLine,
                    onToggleLoop = onToggleLoop,
                    onLookup = onLookup,
                    onViewModeChange = onViewModeChange,
                    onVietnameseToggle = onVietnameseToggle,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                )
            }
        }
    }
    transcript.lookupWord?.let { word ->
        LookupSheet(word = word, onPlayClip = onPlayLookupClip, onDismiss = onDismissLookup)
    }
}

private fun fakeLine(id: String, text: String, pinyin: String) = ReferenceLine(
    id = id,
    type = "line",
    language = "zh",
    speaker = "coach_zh",
    text = text,
    pinyin = pinyin,
    translation = "Xin chào",
)

@Preview(showBackground = true)
@Composable
private fun PlayerCardPreview() {
    MaterialTheme {
        Surface {
            PlayerCard(
                state = PlayerUiState(
                    connected = true,
                    playing = true,
                    lessonId = "lesson_001",
                    lessonTitle = "Lesson 1 — mini-story",
                    track = "guided",
                    practiceAvailable = true,
                    positionMs = 12_000,
                    durationMs = 204_889,
                    speed = 1f,
                    transcript = TranscriptUiState(
                        lessonId = "lesson_001",
                        track = "guided",
                        lines = listOf(fakeLine("r1_food", "你好", "nǐ hǎo")),
                        viewMode = TranscriptViewMode.PINYIN_ONLY,
                        showVietnamese = true,
                    ),
                ),
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
