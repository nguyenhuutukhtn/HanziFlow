package com.hanziflow.ui.player

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
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
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copyLine: (String) -> Unit = { hanzi ->
        clipboard.setText(AnnotatedString(hanzi))
        Toast.makeText(context, "Đã sao chép", Toast.LENGTH_SHORT).show()
    }
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
            itemsIndexed(lines, key = { _, line -> line.id }) { index, line ->
                val revealed = remember(line.id, line.type, positionMs) {
                    TranscriptCues.isModelAnswerRevealed(cues, line.id, positionMs)
                }
                val nextLine = lines.getOrNull(index + 1)
                val decision = remember(line, nextLine, viewMode, showVietnamese, revealed) {
                    TranscriptRendering.decide(line, viewMode, showVietnamese, revealed, nextLine)
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
                        onCopyLine = { copyLine(line.text) },
                        useRawHanziFallback = items != null && items.words.isEmpty(),
                    )
                }
            }
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
    onCopyLine: () -> Unit,
    useRawHanziFallback: Boolean,
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
            .combinedClickable(onClick = onSeek, onLongClick = if (
                decision.placeholder || (!decision.showHanzi && !decision.showPinyinLine)
            ) null else onCopyLine),
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
                // A package can contain reference tokens while its items file has no
                // word data. Render the source line directly so Hanzi never disappears.
                decision.showHanzi && (decision.tokens.isEmpty() || useRawHanziFallback) -> {
                    if (decision.tokens.any { it.showPinyin }) Text(
                        line.pinyin.orEmpty(), style = MaterialTheme.appTextStyles.pinyin, color = secondaryText,
                    )
                    Text(line.text, style = if (line.language.startsWith("vi")) {
                        MaterialTheme.appTextStyles.vietnamese
                    } else MaterialTheme.appTextStyles.hanzi, color = primaryText)
                }
                decision.showHanzi -> TokenRow(
                    tokens = decision.tokens,
                    hanziColor = primaryText,
                    pinyinColor = secondaryText,
                    onSeek = onSeek,
                    onLookupToken = onLookupToken,
                    onCopyLine = onCopyLine,
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
    onCopyLine: () -> Unit,
) {
    FlowRow(
        // Bottom alignment keeps punctuation (no pinyin column) on the Hanzi
        // baseline instead of floating up into the ruby row. 4dp between columns
        // stops adjacent pinyin strings from touching.
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        itemVerticalAlignment = Alignment.Bottom,
    ) {
        tokens.forEach { decision ->
            TokenCell(
                token = decision.token,
                showPinyin = decision.showPinyin,
                hanziColor = hanziColor,
                pinyinColor = pinyinColor,
                onSeek = onSeek,
                onLookupToken = onLookupToken,
                onCopyLine = onCopyLine,
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
    onCopyLine: () -> Unit,
) {
    Column(
        modifier = Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { token.itemId?.let(onLookupToken) ?: onSeek() },
            onLongClick = onCopyLine,
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
