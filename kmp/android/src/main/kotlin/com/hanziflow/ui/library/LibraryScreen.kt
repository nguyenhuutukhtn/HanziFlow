package com.hanziflow.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.hanziflow.R
import com.hanziflow.data.LessonEntity
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ui.theme.Spacing

/**
 * Learn tab: the existing library wired into the new shell. Stateless — Room,
 * OkHttp and WorkManager stay in [LibraryViewModel]. Rich card states (passed,
 * update badges, download progress) arrive in G2b; this pass only re-skins the
 * current actions with the amber theme and Vietnamese copy.
 */
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
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            Text(stringResource(R.string.learn_title), style = MaterialTheme.typography.headlineMedium)
        }
        item {
            SyncControls(
                baseUrl = state.baseUrl,
                syncing = state.syncing,
                onEditBaseUrl = onEditBaseUrl,
                onSync = onSync,
            )
        }
        if (state.syncing) {
            item {
                Text(
                    stringResource(R.string.syncing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (state.message != null) {
            item {
                Text(
                    stringResource(R.string.sync_failure),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (state.lessons.isEmpty()) {
            item { EmptyLibrary(onSync = onSync) }
        }
        items(state.lessons, key = { it.id }) { lesson ->
            LessonCard(lesson, onDownload, onCancel, onDelete, onPlay)
        }
    }
}

@Composable
private fun SyncControls(
    baseUrl: String,
    syncing: Boolean,
    onEditBaseUrl: (String) -> Unit,
    onSync: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onEditBaseUrl,
            label = { Text(stringResource(R.string.catalog_url_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onSync, enabled = !syncing) {
            Text(stringResource(R.string.sync_now))
        }
    }
}

@Composable
private fun EmptyLibrary(onSync: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(stringResource(R.string.learn_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.learn_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.sm))
        Button(onClick = onSync) { Text(stringResource(R.string.sync_now)) }
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
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
            Text(
                lessonMeta(lesson),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            lesson.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                when (lesson.downloadState) {
                    "DOWNLOADING" -> OutlinedButton(onClick = { onCancel(lesson.id) }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    "DOWNLOADED" -> {
                        Button(onClick = { onPlay(lesson) }) { Text(stringResource(R.string.action_listen)) }
                        OutlinedButton(onClick = { onDelete(lesson.id) }) {
                            Text(stringResource(R.string.action_delete))
                        }
                    }
                    "UPDATE_AVAILABLE" -> {
                        Button(onClick = { onPlay(lesson) }) {
                            Text(stringResource(R.string.action_listen_old))
                        }
                        Button(onClick = { onDownload(lesson.id) }) {
                            Text(stringResource(R.string.action_update))
                        }
                        OutlinedButton(onClick = { onDelete(lesson.id) }) {
                            Text(stringResource(R.string.action_delete))
                        }
                    }
                    "FAILED", "CANCELLED" -> Button(onClick = { onDownload(lesson.id) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                    else -> Button(onClick = { onDownload(lesson.id) }) {
                        Text(stringResource(R.string.action_download))
                    }
                }
            }
        }
    }
}

@Composable
private fun lessonMeta(lesson: LessonEntity): String {
    val level = if (lesson.level == "absolute_beginner") {
        stringResource(R.string.level_absolute_beginner)
    } else {
        lesson.level
    }
    val status = when (lesson.downloadState) {
        "DOWNLOADING" -> stringResource(R.string.download_state_downloading)
        "DOWNLOADED" -> stringResource(R.string.download_state_downloaded)
        "UPDATE_AVAILABLE" -> stringResource(R.string.download_state_update)
        "FAILED" -> stringResource(R.string.download_state_failed)
        "CANCELLED" -> stringResource(R.string.download_state_cancelled)
        else -> stringResource(R.string.download_state_available)
    }
    return "$level · v${lesson.remoteVersion} · $status"
}

private fun fakeLesson(state: String) = LessonEntity(
    id = "lesson_001",
    title = "Đồ ăn mang về của đồng nghiệp",
    level = "absolute_beginner",
    lessonOrder = 1,
    remoteVersion = 1,
    downloadedVersion = if (state == "DOWNLOADED") 1 else null,
    catalogJson = "{}",
    downloadState = state,
)

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    HanziFlowTheme {
        LibraryScreen(
            state = LibraryUiState(
                lessons = listOf(fakeLesson("DOWNLOADED"), fakeLesson("AVAILABLE"), fakeLesson("DOWNLOADING")),
                baseUrl = "http://10.0.2.2:8000/",
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
