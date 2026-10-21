package com.hanziflow.ui.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanziflow.shared.characters.CharacterEntry
import com.hanziflow.shared.characters.CharacterIndex
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCard(entry: CharacterEntry, index: CharacterIndex, onComponent: (String) -> Unit = {}) {
    Column(Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(entry.char, style = MaterialTheme.typography.displayLarge)
        Text(entry.pinyin, style = MaterialTheme.typography.titleLarge)
        Text("Hán-Việt: ${entry.hanViet}")
        Text(entry.viMeaning, style = MaterialTheme.typography.bodyLarge)
        entry.hskLevel?.let { FilterChip(selected = false, onClick = {}, label = { Text("HSK $it") }) }
        val components = index.componentsOf(entry.char)
        if (components.isNotEmpty()) {
            Text("Bộ phận", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                components.forEach { component ->
                    AssistChip(onClick = { onComponent(component.char) }, label = {
                        Text("${component.char} · ${component.nameVi} · ${role(component.role)}")
                    })
                }
            }
        }
        if (entry.lessonWords.isNotEmpty()) {
            Text("Từ trong bài", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) { entry.lessonWords.forEach { AssistChip(onClick = {}, label = { Text(it) }) } }
        }
        if (entry.phoneticFamily.isNotEmpty()) {
            Text("Cùng bộ phận biểu âm · chỉ để tham khảo", style = MaterialTheme.typography.titleMedium)
            Text(entry.phoneticFamily.take(4).joinToString(" · "))
        }
    }
}

private fun role(value: String) = when (value) { "semantic" -> "nghĩa"; "phonetic" -> "âm"; else -> "bộ phận" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListSheet(index: CharacterIndex, onCharacter: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            item { Text("Chữ của bài", style = MaterialTheme.typography.headlineSmall) }
            items(index.allCharacters(), key = { it.char }) { entry ->
                TextButton(onClick = { onCharacter(entry.char) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("${entry.char}  ${entry.pinyin} · ${entry.hanViet}", style = MaterialTheme.typography.titleMedium)
                        Text(entry.viMeaning)
                    }
                }
            }
            item { Text("Bộ phận cần biết", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = Spacing.md)) }
            items(index.allComponents(), key = { it.char }) { component ->
                TextButton(onClick = { onCharacter(component.char) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${component.char} · ${component.nameVi}")
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CharacterCardPreview() {
    HanziFlowTheme { CharacterCard(CharacterEntry("我", "wǒ", "ngã", 1, "⿰扌戈", viMeaning = "tôi, mình"), CharacterIndex(com.hanziflow.shared.characters.LessonCharacters("preview"))) }
}
