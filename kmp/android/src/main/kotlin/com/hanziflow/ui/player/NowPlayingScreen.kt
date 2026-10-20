package com.hanziflow.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.appFilterChipColors
import com.hanziflow.ui.components.trackDisplayName
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ui.theme.Spacing

const val NowPlayingTestTag = "now_playing"

/**
 * Full-screen Now Playing route. Stateless; [PlayerViewModel] owns all command logic,
 * including "Nghe lại câu" and loop state. The transcript is the hero: it takes the
 * remaining height above a compact, thumb-reachable transport strip.
 */
@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    onCollapse: () -> Unit,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(NowPlayingTestTag),
    ) {
        NowPlayingHeader(
            title = state.lessonTitle ?: state.lessonId.orEmpty(),
            onCollapse = onCollapse,
        )
        TranscriptViewSwitcher(
            viewMode = state.transcript.viewMode,
            showVietnamese = state.transcript.showVietnamese,
            onViewModeChange = onViewModeChange,
            onVietnameseToggle = onVietnameseToggle,
        )
        state.transcript.loopedLineId?.let { loopedLineId ->
            val loopedText = state.transcript.lines.firstOrNull { it.id == loopedLineId }?.text ?: loopedLineId
            AssistChip(
                onClick = onCancelLoop,
                label = { Text(stringResource(R.string.loop_active, loopedText)) },
                trailingIcon = {
                    val stopLoop = stringResource(R.string.stop_loop)
                    Text(
                        "×",
                        modifier = Modifier.semantics { contentDescription = stopLoop },
                    )
                },
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.transcript.lines.isNotEmpty()) {
                TranscriptPanel(
                    lines = state.transcript.lines,
                    cues = state.transcript.cues,
                    items = state.transcript.items,
                    positionMs = state.positionMs,
                    viewMode = state.transcript.viewMode,
                    showVietnamese = state.transcript.showVietnamese,
                    loopedLineId = state.transcript.loopedLineId,
                    onSeek = onSeekLine,
                    onToggleLoop = onToggleLoop,
                    onLookup = onLookup,
                    onViewModeChange = onViewModeChange,
                    onVietnameseToggle = onVietnameseToggle,
                    showSwitcher = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TransportSection(
            state = state,
            onSwitchTrack = onSwitchTrack,
            onSeekTo = onSeekTo,
            onSeekBack = onSeekBack,
            onToggle = onToggle,
            onSeekForward = onSeekForward,
            onSpeed = onSpeed,
        )
    }
    state.transcript.lookupWord?.let { word ->
        LookupSheet(word = word, onPlayClip = onPlayLookupClip, onDismiss = onDismissLookup)
    }
}

@Composable
private fun NowPlayingHeader(title: String, onCollapse: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = stringResource(R.string.collapse_now_playing),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.more_actions),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                // Test entry arrives with the test flow (G2c); disabled rather than hidden
                // so the overflow shape is already final.
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.take_test)) },
                    onClick = { menuOpen = false },
                    enabled = false,
                )
            }
        }
    }
}

@Composable
private fun TransportSection(
    state: PlayerUiState,
    onSwitchTrack: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeed: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (state.practiceAvailable) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = state.track == "guided",
                    onClick = { onSwitchTrack("guided") },
                    label = { Text(trackDisplayName("guided")) },
                    colors = appFilterChipColors(),
                )
                FilterChip(
                    selected = state.track == "practice",
                    onClick = { onSwitchTrack("practice") },
                    label = { Text(trackDisplayName("practice")) },
                    colors = appFilterChipColors(),
                )
            }
        } else {
            Text(
                trackDisplayName(state.track),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SeekBar(state = state, onSeekTo = onSeekTo)

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.speed_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            listOf(0.8f, 0.9f, 1f, 1.1f).forEach { speed ->
                FilterChip(
                    selected = state.speed == speed,
                    onClick = { onSpeed(speed) },
                    label = { Text("${speed}×") },
                    colors = appFilterChipColors(),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val seekBackDescription = stringResource(R.string.seek_back_10)
            val seekForwardDescription = stringResource(R.string.seek_forward_10)
            OutlinedButton(
                onClick = onSeekBack,
                modifier = Modifier.semantics { contentDescription = seekBackDescription },
            ) {
                Text("−10s")
            }
            FilledIconButton(onClick = onToggle, modifier = Modifier.size(56.dp)) {
                Icon(
                    painter = painterResource(if (state.playing) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = stringResource(if (state.playing) R.string.pause else R.string.play),
                    modifier = Modifier.size(28.dp),
                )
            }
            OutlinedButton(
                onClick = onSeekForward,
                modifier = Modifier.semantics { contentDescription = seekForwardDescription },
            ) {
                Text("+10s")
            }
        }
    }
}

@Composable
private fun SeekBar(state: PlayerUiState, onSeekTo: (Long) -> Unit) {
    val max = state.durationMs.coerceAtLeast(1).toFloat()
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val sliderValue = if (scrubbing) scrubValue else state.positionMs.toFloat().coerceIn(0f, max)
    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                scrubbing = true
                scrubValue = it
            },
            onValueChangeFinished = {
                scrubbing = false
                onSeekTo(scrubValue.toLong())
            },
            valueRange = 0f..max,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatTime(if (scrubbing) scrubValue.toLong() else state.positionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatTime(state.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0) / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun fakeLine(id: String, text: String, pinyin: String, translation: String) = ReferenceLine(
    id = id,
    type = "line",
    language = "zh",
    speaker = "coach_zh",
    text = text,
    pinyin = pinyin,
    translation = translation,
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
                track = "guided",
                practiceAvailable = false,
                positionMs = 42_000,
                durationMs = 204_889,
                speed = 1f,
                transcript = TranscriptUiState(
                    lessonId = "lesson_001",
                    track = "guided",
                    lines = listOf(
                        fakeLine("r1", "你好", "nǐ hǎo", "Xin chào"),
                        fakeLine("r2", "我叫安娜。你呢？", "wǒ jiào ānnà. nǐ ne?", "Tôi tên là Anna. Bạn thì?"),
                        fakeLine("r3", "很高兴认识你。", "hěn gāoxìng rènshi nǐ.", "Rất vui được gặp bạn."),
                    ),
                    viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                    showVietnamese = true,
                ),
            ),
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
