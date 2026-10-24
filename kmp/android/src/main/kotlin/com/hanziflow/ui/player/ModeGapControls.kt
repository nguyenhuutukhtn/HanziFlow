package com.hanziflow.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hanziflow.shared.modes.GapState
import kotlin.math.ceil

const val GapCountdownTestTag = "gap_countdown"

@Composable
fun ModeGapControls(gap: GapState, onPause: () -> Unit, modifier: Modifier = Modifier) {
    val progress = (gap.remainingMs.toFloat() / gap.totalMs.coerceAtLeast(1)).coerceIn(0f, 1f)
    val seconds = ceil(gap.remainingMs / 1_000.0).toInt()
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(64.dp).testTag(GapCountdownTestTag), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(64.dp))
                Text("$seconds", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onPause) { Text("Cần thêm thời gian") }
        }
    }
}

@Composable
fun NgheModeControls(onReveal: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Nghe thuần", style = MaterialTheme.typography.titleSmall)
            Button(onClick = onReveal) { Text("Hiện câu") }
        }
    }
}

@Composable
fun RepeatEachChip(enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = enabled,
        onClick = onToggle,
        label = { Text("Lặp ×2") },
        modifier = modifier,
    )
}
