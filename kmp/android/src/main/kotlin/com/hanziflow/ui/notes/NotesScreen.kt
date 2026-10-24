package com.hanziflow.ui.notes

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import com.hanziflow.shared.NoteExample
import com.hanziflow.shared.WordItem
import com.hanziflow.ui.theme.Spacing
import com.hanziflow.ui.theme.appColors
import com.hanziflow.ui.theme.appTextStyles

const val NotesTestTag = "notes"

/**
 * Read-it-all-at-once notes: Mẫu câu, Câu hỏi, Từ vựng. No reveals or steps; hanzi large,
 * pinyin and Vietnamese under it, long-press copies the hanzi.
 */
@Composable
fun NotesScreen(state: NotesUiState, modifier: Modifier = Modifier) {
    val copy = rememberCopyHanzi()
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(NotesTestTag),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { Text("Ghi chú · " + state.title, style = MaterialTheme.typography.headlineSmall) }
        if (state.patterns.isNotEmpty()) {
            item { SectionTitle("Mẫu câu") }
            items(state.patterns, key = { "p_" + it.id }) { pattern ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(pattern.pattern, style = MaterialTheme.typography.titleLarge)
                        Text(pattern.explainVi, style = MaterialTheme.typography.bodyMedium)
                        pattern.examples.forEach { ExampleBlock(it, copy) }
                    }
                }
            }
        }
        if (state.questions.isNotEmpty()) {
            item { SectionTitle("Câu hỏi") }
            items(state.questions, key = { "q_" + it.id }) { question ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        ExampleBlock(NoteExample(question.hanzi, question.pinyin, question.meaningVi), copy)
                        Text(question.howToAnswerVi, style = MaterialTheme.typography.bodyMedium)
                        question.examples.forEach { ExampleBlock(it, copy) }
                    }
                }
            }
        }
        if (state.words.isNotEmpty()) {
            item { SectionTitle("Từ vựng (${state.words.size})") }
            items(state.words, key = { "w_" + it.id }) { word -> WordRow(word, copy) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Column {
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(Modifier.padding(top = Spacing.xs))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExampleBlock(example: NoteExample, onCopy: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { onCopy(example.hanzi) })) {
        Text(example.hanzi, style = MaterialTheme.appTextStyles.hanzi)
        Text(example.pinyin, style = MaterialTheme.appTextStyles.pinyin, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(example.meaningVi, style = MaterialTheme.appTextStyles.vietnamese, color = MaterialTheme.appColors.meaning)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WordRow(word: WordItem, onCopy: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { onCopy(word.hanzi) })) {
        Text(word.hanzi, style = MaterialTheme.appTextStyles.hanzi)
        Text(word.pinyin, style = MaterialTheme.appTextStyles.pinyin, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(word.meaningVi, style = MaterialTheme.appTextStyles.vietnamese, color = MaterialTheme.appColors.meaning)
    }
}

@Composable
private fun rememberCopyHanzi(): (String) -> Unit {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    return { hanzi ->
        clipboard.setText(AnnotatedString(hanzi))
        Toast.makeText(context, "Đã sao chép", Toast.LENGTH_SHORT).show()
    }
}
