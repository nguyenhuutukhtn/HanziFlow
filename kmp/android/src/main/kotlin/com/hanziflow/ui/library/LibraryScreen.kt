package com.hanziflow.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanziflow.data.LessonEntity

/** Stateless: state in, events out. Everything that touches Room/OkHttp/WorkManager lives in [LibraryViewModel]. */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEditBaseUrl: (String) -> Unit,
    onSync: () -> Unit,
    onDownload: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPlay: (LessonEntity) -> Unit,
    modifier: Modifier = Modifier,
    header: LazyListScope.() -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("HanziFlow", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = onEditBaseUrl,
                label = { Text("Catalog base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSync) { Text("Sync catalog") }
            state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        header()
        item { Text("Library", style = MaterialTheme.typography.titleLarge) }
        items(state.lessons, key = { it.id }) { lesson ->
            LessonCard(lesson, onDownload, onCancel, onDelete, onPlay)
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonEntity,
    onDownload: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPlay: (LessonEntity) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
            Text("${lesson.level} · v${lesson.remoteVersion} · ${lesson.downloadState}")
            lesson.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (lesson.downloadState) {
                    "DOWNLOADING" -> OutlinedButton(onClick = { onCancel(lesson.id) }) { Text("Cancel") }
                    "DOWNLOADED" -> {
                        Button(onClick = { onPlay(lesson) }) { Text("Play") }
                        OutlinedButton(onClick = { onDelete(lesson.id) }) { Text("Delete") }
                    }
                    "UPDATE_AVAILABLE" -> {
                        Button(onClick = { onPlay(lesson) }) { Text("Play old") }
                        Button(onClick = { onDownload(lesson.id) }) { Text("Update") }
                    }
                    else -> Button(onClick = { onDownload(lesson.id) }) {
                        Text(if (lesson.downloadState == "FAILED" || lesson.downloadState == "CANCELLED") "Retry" else "Download")
                    }
                }
            }
        }
    }
}

private fun fakeLesson(state: String) = LessonEntity(
    id = "lesson_001",
    title = "Lesson 1 — mini-story",
    level = "absolute_beginner",
    lessonOrder = 1,
    remoteVersion = 2,
    downloadedVersion = if (state == "DOWNLOADED") 2 else null,
    catalogJson = "{}",
    downloadState = state,
)

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    MaterialTheme {
        Surface {
            LibraryScreen(
                state = LibraryUiState(
                    lessons = listOf(fakeLesson("DOWNLOADED"), fakeLesson("AVAILABLE"), fakeLesson("DOWNLOADING")),
                    baseUrl = "http://10.0.2.2:8000/",
                    message = "Catalog updated",
                ),
                onEditBaseUrl = {},
                onSync = {},
                onDownload = {},
                onCancel = {},
                onDelete = {},
                onPlay = {},
            )
        }
    }
}
