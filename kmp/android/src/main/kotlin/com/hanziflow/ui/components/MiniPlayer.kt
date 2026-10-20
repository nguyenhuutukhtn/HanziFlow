package com.hanziflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.ui.theme.Spacing

const val MiniPlayerTestTag = "mini_player"
const val MiniPlayerOpenTestTag = "mini_player_open"

/**
 * Persistent mini-player, docked above the bottom navigation on every tab.
 * Exactly two controls (replay current line + play/pause); tapping the title area
 * or swiping up opens Now Playing. Seek and loop live only in Now Playing.
 */
@Composable
fun MiniPlayer(
    title: String,
    trackLabel: String,
    playing: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onReplayLine: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val stateLabel = stringResource(if (playing) R.string.playing_state else R.string.paused_state)
    val description = stringResource(R.string.mini_player_description, title, stateLabel)
    val expandThreshold = with(LocalDensity.current) { 24.dp.toPx() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MiniPlayerTestTag)
            // Observe in the Initial pass: the title row's clickable and the control
            // buttons consume events in Main, so a parent Main-pass drag detector
            // never sees the gesture. This keeps tap-to-open and swipe-up independent.
            .pointerInput(expandThreshold) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalY = 0f
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null) {
                            pressed = false
                        } else {
                            totalY += change.positionChange().y
                            if (totalY < 0f) change.consume()
                            pressed = change.pressed
                        }
                    }
                    if (totalY < -expandThreshold) onExpand()
                }
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Thin, deliberately non-interactive progress line: seeking needs the
            // full bar in Now Playing to avoid mis-taps while walking.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(start = Spacing.lg, end = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .testTag(MiniPlayerOpenTestTag)
                        .clickable(onClick = onExpand)
                        .semantics { contentDescription = description }
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = trackLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                TextButton(onClick = onReplayLine) {
                    Text(stringResource(R.string.mini_player_replay))
                }
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(width = 56.dp, height = 48.dp),
                ) {
                    Icon(
                        painter = painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = stringResource(if (playing) R.string.pause else R.string.play),
                    )
                }
            }
        }
    }
}
