package com.hanziflow.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanziflow.shared.Items
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.Token
import com.hanziflow.shared.transcript.LineRenderDecision
import com.hanziflow.shared.transcript.TokenRenderDecision
import com.hanziflow.shared.transcript.TranscriptCues
import com.hanziflow.shared.transcript.TranscriptRendering
import com.hanziflow.shared.transcript.TranscriptViewMode

private const val AUTO_SCROLL_PAUSE_MS = 5_000L

/**
 * One-dataset, several-views karaoke transcript. Pure rendering decisions come from
 * `shared/transcript`; this composable only owns scroll/highlight/gesture plumbing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranscriptPanel(
    lines: List<ReferenceLine>,
    cues: List<TimelineCue>,
    items: Items?,
    positionMs: Long,
    viewMode: TranscriptViewMode,
    showVietnamese: Boolean,
    loopedLineId: String?,
    onSeek: (String) -> Unit,
    onToggleLoop: (String) -> Unit,
    onLookup: (String) -> Unit,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCue = remember(cues, positionMs) { TranscriptCues.activeCue(cues, positionMs) }
    val activeLineIds = remember(activeCue) { activeCue?.let(TranscriptCues::lineIdsFor)?.toSet() ?: emptySet() }

    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    var scrollPausedUntil by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isDragged) {
        if (isDragged) scrollPausedUntil = System.currentTimeMillis() + AUTO_SCROLL_PAUSE_MS
    }
    LaunchedEffect(activeLineIds, lines) {
        if (activeLineIds.isEmpty()) return@LaunchedEffect
        if (System.currentTimeMillis() < scrollPausedUntil) return@LaunchedEffect
        val index = lines.indexOfFirst { it.id in activeLineIds }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(modifier) {
        ViewSwitcher(viewMode, showVietnamese, onViewModeChange, onVietnameseToggle)
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(lines, key = { _, line -> line.id }) { _, line ->
                val revealed = remember(line.id, line.type, positionMs) {
                    TranscriptCues.isModelAnswerRevealed(cues, line.id, positionMs)
                }
                val decision = remember(line, viewMode, showVietnamese, revealed) {
                    TranscriptRendering.decide(line, viewMode, showVietnamese, revealed)
                }
                if (decision.lineVisible) {
                    TranscriptLineRow(
                        line = line,
                        decision = decision,
                        isActive = line.id in activeLineIds,
                        isLooped = loopedLineId == line.id,
                        onSeek = { onSeek(line.id) },
                        onToggleLoop = { onToggleLoop(line.id) },
                        onLookupToken = { itemId -> items?.words?.any { it.id == itemId }?.let { onLookup(itemId) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewSwitcher(
    viewMode: TranscriptViewMode,
    showVietnamese: Boolean,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                TranscriptViewMode.HIDDEN to "Hidden",
                TranscriptViewMode.PINYIN_ONLY to "Pinyin",
                TranscriptViewMode.HANZI_WITH_PINYIN to "Hanzi+pinyin",
                TranscriptViewMode.HANZI_ONLY to "Hanzi",
            ).forEach { (mode, label) ->
                FilterChip(selected = viewMode == mode, onClick = { onViewModeChange(mode) }, label = { Text(label) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Vietnamese", style = MaterialTheme.typography.labelMedium)
            Switch(checked = showVietnamese, onCheckedChange = onVietnameseToggle)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TranscriptLineRow(
    line: ReferenceLine,
    decision: LineRenderDecision,
    isActive: Boolean,
    isLooped: Boolean,
    onSeek: () -> Unit,
    onToggleLoop: () -> Unit,
    onLookupToken: (String) -> Unit,
) {
    val rowBackground = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .let { base ->
                if (decision.placeholder || !decision.showHanzi) {
                    base.combinedClickable(onClick = onSeek)
                } else base
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(Modifier.weight(1f)) {
            when {
                decision.placeholder -> Text("• • •", color = MaterialTheme.colorScheme.onSurfaceVariant)
                decision.showPinyinLine -> Text(line.pinyin ?: line.text, style = MaterialTheme.typography.bodyLarge)
                decision.showHanzi -> TokenRow(decision.tokens, onSeek, onLookupToken)
                else -> Text(" ", style = MaterialTheme.typography.bodyLarge) // hidden view: no text, keeps row tappable
            }
            if (decision.showTranslation) {
                Text(
                    line.translation.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isActive && !decision.placeholder) {
            IconButton(onClick = onToggleLoop) {
                Text(
                    if (isLooped) "↻1" else "↻",
                    color = if (isLooped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TokenRow(tokens: List<TokenRenderDecision>, onSeek: () -> Unit, onLookupToken: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        tokens.forEach { decision ->
            TokenCell(decision.token, decision.showPinyin, onSeek, onLookupToken)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TokenCell(token: Token, showPinyin: Boolean, onSeek: () -> Unit, onLookupToken: (String) -> Unit) {
    Column(
        modifier = Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onSeek,
            onLongClick = { token.itemId?.let(onLookupToken) },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showPinyin) {
            Text(token.pinyin.orEmpty(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(token.hanzi, style = MaterialTheme.typography.bodyLarge)
    }
}
