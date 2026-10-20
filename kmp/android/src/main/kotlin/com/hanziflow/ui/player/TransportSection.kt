package com.hanziflow.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.shared.transcript.ChineseOnlyPlan
import com.hanziflow.ui.components.appFilterChipColors
import com.hanziflow.ui.components.trackDisplayName
import com.hanziflow.ui.theme.Spacing

/** Track switch, seek bar, speed chips and primary transport controls. Split out of NowPlayingScreen (line budget). */
@Composable
fun TransportSection(
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
            modifier = Modifier.horizontalScroll(rememberScrollState()),
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

/** Ticks drawn under the thumb at each section start (except the first); markers only, no labels. */
@Composable
private fun SeekBar(state: PlayerUiState, onSeekTo: (Long) -> Unit) {
    val max = state.durationMs.coerceAtLeast(1).toFloat()
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val sliderValue = if (scrubbing) scrubValue else state.positionMs.toFloat().coerceIn(0f, max)
    Column {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val trackWidth = maxWidth
            if (state.transcript.sections.size > 1 && state.durationMs > 0) {
                val markerColor = MaterialTheme.colorScheme.onSurfaceVariant
                state.transcript.sections.drop(1).forEach { section ->
                    val fraction = (section.startMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = trackWidth * fraction)
                            .size(width = 2.dp, height = 8.dp)
                            .background(markerColor),
                    )
                }
            }
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
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatTime(if (scrubbing) scrubValue.toLong() else state.positionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val remaining = if (state.chineseOnly) {
                val effective = ChineseOnlyPlan.effectiveRemainingMs(
                    state.transcript.cues,
                    state.positionMs,
                    state.durationMs,
                )
                stringResource(R.string.effective_remaining_time, formatTime(effective))
            } else {
                formatTime(state.durationMs)
            }
            Text(
                remaining,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0) / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
