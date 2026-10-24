package com.hanziflow.ui.explain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hanziflow.ai.ExplainViewModel
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.ui.player.PlayerUiState

/** Hilt-backed sheet for one transcript line; used by the app shell (previews/tests use [ExplainSheet]). */
@Composable
fun ExplainSheetHost(
    player: PlayerUiState,
    line: ReferenceLine,
    onDismiss: () -> Unit,
    viewModel: ExplainViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(line.id, player.lessonId) {
        val lessonId = player.lessonId ?: return@LaunchedEffect
        val version = player.transcript.items?.lessonVersion ?: 0
        viewModel.open(ExplainTargets.build(lessonId, version, player.transcript.lines, line))
    }
    ExplainSheet(line, ui, viewModel::send, viewModel::retry, viewModel::reexplain, onDismiss)
}
