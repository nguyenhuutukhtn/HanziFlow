package com.hanziflow.ui.explain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.ai.ChatMessage
import com.hanziflow.ai.ExplainError
import com.hanziflow.ai.ExplainThread
import com.hanziflow.ai.ExplainUiState
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.ui.theme.Spacing
import com.hanziflow.ui.theme.appTextStyles
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownDimens

const val ExplainInputTag = "explain_input"
const val ExplainSendTag = "explain_send"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplainSheet(
    line: ReferenceLine,
    state: ExplainUiState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onReexplain: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        ExplainSheetContent(line, state, onSend, onRetry, onReexplain, Modifier.fillMaxHeight(0.9f).imePadding())
    }
}

@Composable
fun ExplainSheetContent(
    line: ReferenceLine,
    state: ExplainUiState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onReexplain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The repository saves a question together with its answer, so show it optimistically while sending.
    var pending by remember { mutableStateOf("") }
    Column(modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Column {
            line.pinyin?.let { Text(it, style = MaterialTheme.appTextStyles.pinyin, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(line.text, style = MaterialTheme.appTextStyles.hanzi)
        }
        Box(Modifier.weight(1f, fill = true).fillMaxWidth()) {
            when (state) {
                ExplainUiState.NotConfigured -> Text(stringResource(R.string.explain_not_configured))
                ExplainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ExplainUiState.Failed -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(errorText(state.error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text(stringResource(R.string.explain_retry)) }
                }
                is ExplainUiState.Ready -> Messages(state, pending, onRetry, onReexplain)
            }
        }
        if (state is ExplainUiState.Ready) ChatInput(sending = state.sending, onSend = { pending = it; onSend(it) })
    }
}

@Composable
private fun Messages(state: ExplainUiState.Ready, pending: String, onRetry: () -> Unit, onReexplain: () -> Unit) {
    val thread: ExplainThread = state.thread
    // A plain scrolling Column, not LazyColumn: lazy items re-parse their Markdown when they scroll
    // back in, change height, and a fast fling to the end jitters up and down. Threads are short.
    val scrollState = rememberScrollState()
    LaunchedEffect(thread.messages.size, state.sending, state.error) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    // Prompt (messages[0]) is never shown; stale/error/sending rows follow the messages.
    Column(Modifier.fillMaxWidth().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        thread.messages.forEachIndexed { index, message ->
            when {
                index == 0 -> AssistChip(onClick = {}, label = { Text(stringResource(R.string.explain_prompt_chip)) })
                message.role == ChatMessage.USER -> UserBubble(message)
                else -> Column {
                    Markdown(
                        message.content,
                        typography = markdownTypography(
                            h1 = MaterialTheme.typography.titleLarge,
                            h2 = MaterialTheme.typography.titleMedium,
                            h3 = MaterialTheme.typography.titleSmall,
                        ),
                        dimens = markdownDimens(tableCellWidth = 96.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    message.model?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (thread.isStale) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.explain_stale), Modifier.weight(1f), color = MaterialTheme.colorScheme.tertiary)
                TextButton(onClick = onReexplain) { Text(stringResource(R.string.explain_reexplain)) }
            }
        }
        if (state.error != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(errorText(state.error), Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry, enabled = !state.sending) { Text(stringResource(R.string.explain_retry)) }
            }
        }
        if (state.sending) {
            UserBubble(ChatMessage(ChatMessage.USER, pending))
            CircularProgressIndicator(Modifier.padding(Spacing.sm))
        }
    }
}

@Composable
private fun errorText(e: ExplainError): String = when (e.kind) {
    ExplainError.Kind.QUOTA -> stringResource(R.string.explain_error_quota)
    ExplainError.Kind.NETWORK -> stringResource(R.string.explain_error_network)
    ExplainError.Kind.OTHER -> stringResource(R.string.explain_error_other, e.code?.toString() ?: "?")
}

@Composable
private fun UserBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = if (message.failed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.85f),
        ) { Text(message.content, Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
private fun ChatInput(sending: Boolean, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(Modifier.padding(bottom = Spacing.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.explain_input_hint)) },
            enabled = !sending,
            modifier = Modifier.weight(1f).testTag(ExplainInputTag),
        )
        Button(
            onClick = { onSend(text); text = "" },
            enabled = !sending && text.isNotBlank(),
            modifier = Modifier.testTag(ExplainSendTag),
        ) { Text(stringResource(R.string.explain_send)) }
    }
}
