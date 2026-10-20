package com.hanziflow.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.shared.Items
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.Token
import com.hanziflow.shared.transcript.LineRenderDecision
import com.hanziflow.shared.transcript.TokenRenderDecision
import com.hanziflow.shared.transcript.TranscriptCues
import com.hanziflow.shared.transcript.TranscriptRendering
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.appFilterChipColors
import com.hanziflow.ui.theme.appColors
import com.hanziflow.ui.theme.appTextStyles

private const val AUTO_SCROLL_PAUSE_MS = 5_000L

/**
 * One-dataset, several-views karaoke transcript. Pure rendering decisions come from
 * `shared/transcript`; this composable only owns scroll/highlight/gesture plumbing.
 * [showSwitcher] is false in Now Playing, where the view chips and Vietnamese switch
 * sit in the screen chrome above the transcript instead.
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
    showSwitcher: Boolean = true,
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
        if (showSwitcher) {
            TranscriptViewSwitcher(
                viewMode = viewMode,
                showVietnamese = showVietnamese,
                onViewModeChange = onViewModeChange,
                onVietnameseToggle = onVietnameseToggle,
            )
        }
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

/** Vietnamese chips + Vietnamese-meaning switch; shared by the panel and Now Playing chrome. */
@Composable
fun TranscriptViewSwitcher(
    viewMode: TranscriptViewMode,
    showVietnamese: Boolean,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                TranscriptViewMode.HIDDEN to R.string.view_hidden,
                TranscriptViewMode.PINYIN_ONLY to R.string.view_pinyin,
                TranscriptViewMode.HANZI_WITH_PINYIN to R.string.view_hanzi_pinyin,
                TranscriptViewMode.HANZI_ONLY to R.string.view_hanzi_only,
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = viewMode == mode,
                    onClick = { onViewModeChange(mode) },
                    label = { Text(stringResource(label)) },
                    colors = appFilterChipColors(),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.vietnamese_switch), style = MaterialTheme.typography.labelMedium)
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
    val primaryText = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val meaningColor = if (isActive) primaryText else MaterialTheme.appColors.meaning
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                decision.placeholder -> Text(
                    stringResource(R.string.model_answer_placeholder),
                    style = MaterialTheme.appTextStyles.vietnamese,
                    color = secondaryText,
                )
                decision.showPinyinLine -> Text(
                    line.pinyin ?: line.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = primaryText,
                )
                // Vietnamese reference lines have no tokens: in a Hanzi view they still
                // need their text, not an empty token row.
                decision.showHanzi && decision.tokens.isEmpty() -> Text(
                    line.text,
                    style = if (line.language.startsWith("vi")) {
                        MaterialTheme.appTextStyles.vietnamese
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = primaryText,
                )
                decision.showHanzi -> TokenRow(
                    tokens = decision.tokens,
                    hanziColor = primaryText,
                    pinyinColor = secondaryText,
                    onSeek = onSeek,
                    onLookupToken = onLookupToken,
                )
                else -> Box(
                    Modifier
                        .fillMaxWidth(0.72f)
                        .height(18.dp)
                        .background(
                            color = if (isActive) primaryText.copy(alpha = 0.28f) else secondaryText.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            }
            if (decision.showTranslation) {
                Text(
                    line.translation.orEmpty(),
                    style = MaterialTheme.appTextStyles.vietnamese,
                    color = meaningColor,
                )
            }
        }
        if (isActive && !decision.placeholder) {
            val loopDescription = stringResource(
                if (isLooped) R.string.loop_line_active else R.string.loop_line,
            )
            IconButton(
                onClick = onToggleLoop,
                modifier = Modifier.semantics { contentDescription = loopDescription },
            ) {
                Text(
                    if (isLooped) "↻1" else "↻",
                    color = if (isLooped) MaterialTheme.colorScheme.primary else secondaryText,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TokenRow(
    tokens: List<TokenRenderDecision>,
    hanziColor: Color,
    pinyinColor: Color,
    onSeek: () -> Unit,
    onLookupToken: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        tokens.forEach { decision ->
            TokenCell(
                token = decision.token,
                showPinyin = decision.showPinyin,
                hanziColor = hanziColor,
                pinyinColor = pinyinColor,
                onSeek = onSeek,
                onLookupToken = onLookupToken,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TokenCell(
    token: Token,
    showPinyin: Boolean,
    hanziColor: Color,
    pinyinColor: Color,
    onSeek: () -> Unit,
    onLookupToken: (String) -> Unit,
) {
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
            Text(
                token.pinyin.orEmpty(),
                style = MaterialTheme.appTextStyles.pinyin,
                color = pinyinColor,
            )
        }
        Text(
            token.hanzi,
            style = MaterialTheme.appTextStyles.hanzi,
            color = hanziColor,
        )
    }
}
