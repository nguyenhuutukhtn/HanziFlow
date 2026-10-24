package com.hanziflow.ui.modes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.hanziflow.ui.theme.Spacing

const val ModePickerTestTag = "mode_picker"
const val ModePickerNotesTestTag = "mode_picker_notes"

@Composable
fun ModePickerScreen(
    state: ModePickerUiState,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenNotes: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(ModePickerTestTag),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { Text(state.title, style = MaterialTheme.typography.headlineMedium) }
        if (state.notesAvailable) item {
            OutlinedButton(onClick = onOpenNotes, modifier = Modifier.fillMaxWidth().testTag(ModePickerNotesTestTag)) { Text("Ghi chú") }
        }
        item { Text("Chọn cách học", style = MaterialTheme.typography.titleMedium) }
        items(state.modes, key = { it.id }) { mode ->
            Card(Modifier.fillMaxWidth().clickable { onSelect(mode.id) }) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(mode.label, style = MaterialTheme.typography.titleLarge)
                    Text(mode.whenToUse, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        formatModeDuration(mode.durationMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

internal fun formatModeDuration(durationMs: Long): String {
    val seconds = durationMs.coerceAtLeast(0) / 1_000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
