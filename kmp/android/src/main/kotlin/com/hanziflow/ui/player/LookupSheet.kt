package com.hanziflow.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanziflow.shared.WordItem

/** Long-press-a-token lookup: Hanzi, pinyin, Vietnamese meaning, and a clip play button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupSheet(word: WordItem, onPlayClip: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(word.hanzi, style = MaterialTheme.typography.headlineMedium)
            Text(word.pinyin, style = MaterialTheme.typography.titleMedium)
            Text(word.meaningVi, style = MaterialTheme.typography.bodyLarge)
            if (word.clip != null) {
                Button(onClick = onPlayClip) { Text("Play") }
            }
        }
    }
}
