package com.hanziflow.ui.player

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.hanziflow.shared.WordItem
import com.hanziflow.shared.characters.CharacterIndex
import com.hanziflow.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupSheet(
    word: WordItem,
    lineText: String?,
    onPlayClip: () -> Unit,
    onDismiss: () -> Unit,
    characterIndex: CharacterIndex? = null,
    onCharacter: (String) -> Unit = {},
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(word.hanzi, style = MaterialTheme.typography.headlineMedium)
            Text(word.pinyin, style = MaterialTheme.typography.titleMedium)
            Text(word.meaningVi, style = MaterialTheme.typography.bodyLarge)
            characterIndex?.charactersOf(word.hanzi)?.takeIf { it.isNotEmpty() }?.let { entries ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    entries.forEach { entry -> AssistChip(onClick = { onCharacter(entry.char) }, label = { Text(entry.char) }) }
                }
            }
            if (word.clip != null) Button(onClick = onPlayClip) { Text("▶ Phát âm") }
            TextButton(onClick = { clipboard.setText(AnnotatedString(word.hanzi)) }) { Text("Sao chép từ") }
            TextButton(onClick = { clipboard.setText(AnnotatedString(lineText ?: word.hanzi)) }) { Text("Sao chép câu") }
            TextButton(onClick = {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, word.hanzi)
                }, "Chia sẻ đến…"))
            }) { Text("Chia sẻ đến…") }
        }
    }
}
