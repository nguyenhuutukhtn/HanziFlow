package com.hanziflow.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.transcript.SectionNavigation
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ui.theme.Spacing
import androidx.compose.material3.ExperimentalMaterial3Api
import com.hanziflow.ui.characters.CharacterCard
import com.hanziflow.ui.characters.CharacterListSheet
import com.hanziflow.shared.characters.CharacterEntry

const val NowPlayingTestTag = "now_playing"

/**
 * Full-screen Now Playing route. Stateless; [PlayerViewModel] owns all command logic.
 * The transcript is the hero: it takes the remaining height above a compact, thumb-
 * reachable transport strip. Section row/markers/loop and Chinese-only live here too.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    onCollapse: () -> Unit,
    onSwitchTrack: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeed: (Float) -> Unit,
    onCancelLoop: () -> Unit,
    onSeekLine: (String) -> Unit,
    onToggleLoop: (String) -> Unit,
    onLookup: (String) -> Unit,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
    onPlayLookupClip: () -> Unit,
    onDismissLookup: () -> Unit,
    modifier: Modifier = Modifier,
    onChineseOnlyToggle: (Boolean) -> Unit = {},
    onLoopSection: () -> Unit = {},
    onStopSectionLoopAfterPass: () -> Unit = {},
    onNextSection: () -> Unit = {},
    onSeekToSection: (String) -> Unit = {},
    onCharacter: (String) -> Unit = {},
    onDismissCharacter: () -> Unit = {},
) {
    var sectionSheetOpen by remember { mutableStateOf(false) }
    var characterListOpen by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(NowPlayingTestTag),
    ) {
        NowPlayingHeader(
            title = state.lessonTitle ?: state.lessonId.orEmpty(),
            onCollapse = onCollapse,
            showCharacters = state.characterIndex != null,
            onOpenCharacters = { characterListOpen = true },
        )
        TranscriptViewSwitcher(
            viewMode = state.transcript.viewMode,
            showVietnamese = state.transcript.showVietnamese,
            onViewModeChange = onViewModeChange,
            onVietnameseToggle = onVietnameseToggle,
            vietnameseEnabled = !state.chineseOnly,
            trailingSwitch = {
                val chineseOnlySupported = state.chineseOnlyAvailable && state.track != "practice"
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.chinese_only_switch), style = MaterialTheme.typography.labelMedium)
                    Switch(
                        checked = state.chineseOnly,
                        onCheckedChange = onChineseOnlyToggle,
                        enabled = chineseOnlySupported,
                    )
                }
            },
        )
        SectionRow(
            sections = state.transcript.sections,
            positionMs = state.positionMs,
            onOpenSectionList = { sectionSheetOpen = true },
            onLoopSection = onLoopSection,
        )
        when {
            state.sectionLoop != null -> SectionLoopChip(
                sectionLoop = state.sectionLoop,
                onStopAfterPass = onStopSectionLoopAfterPass,
                onNextSection = onNextSection,
            )
            state.transcript.loopedLineId != null -> {
                val loopedLineId = state.transcript.loopedLineId
                val loopedText = state.transcript.lines.firstOrNull { it.id == loopedLineId }?.text ?: loopedLineId
                AssistChip(
                    onClick = onCancelLoop,
                    label = { Text(stringResource(R.string.loop_active, loopedText)) },
                    trailingIcon = {
                        val stopLoop = stringResource(R.string.stop_loop)
                        Text("×", modifier = Modifier.semantics { contentDescription = stopLoop })
                    },
                    modifier = Modifier.padding(horizontal = Spacing.md),
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.transcript.lines.isNotEmpty()) {
                TranscriptPanel(
                    lines = state.transcript.lines,
                    cues = state.transcript.cues,
                    items = state.transcript.items,
                    positionMs = state.positionMs,
                    viewMode = state.transcript.viewMode,
                    showVietnamese = state.transcript.showVietnamese,
                    loopedLineId = state.transcript.loopedLineId,
                    onSeek = onSeekLine,
                    onToggleLoop = onToggleLoop,
                    onLookup = onLookup,
                    onViewModeChange = onViewModeChange,
                    onVietnameseToggle = onVietnameseToggle,
                    showSwitcher = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TransportSection(
            state = state,
            onSwitchTrack = onSwitchTrack,
            onSeekTo = onSeekTo,
            onSeekBack = onSeekBack,
            onToggle = onToggle,
            onSeekForward = onSeekForward,
            onSpeed = onSpeed,
        )
    }
    state.transcript.lookupWord?.let { word ->
        LookupSheet(word = word, lineText = state.transcript.lookupLineText, onPlayClip = onPlayLookupClip, onDismiss = onDismissLookup, characterIndex = state.characterIndex, onCharacter = onCharacter)
    }
    state.transcript.lookupCharacter?.let { char ->
        state.characterIndex?.entry(char)?.let { entry ->
            ModalBottomSheet(onDismissRequest = onDismissCharacter) { CharacterCard(entry, state.characterIndex, onComponent = onCharacter) }
        } ?: state.characterIndex?.component(char)?.let { component ->
            ModalBottomSheet(onDismissRequest = onDismissCharacter) {
                CharacterCard(
                    CharacterEntry(component.char, "", "", viMeaning = component.nameVi),
                    state.characterIndex,
                    onComponent = onCharacter,
                )
            }
        }
    }
    if (characterListOpen) {
        state.characterIndex?.let { index ->
            CharacterListSheet(index = index, onCharacter = onCharacter, onDismiss = { characterListOpen = false })
        }
    }
    if (sectionSheetOpen) {
        val currentSectionId = SectionNavigation.sectionAt(state.transcript.sections, state.positionMs)?.id
        SectionListSheet(
            sections = state.transcript.sections,
            currentSectionId = currentSectionId,
            onSelect = { id ->
                onSeekToSection(id)
                sectionSheetOpen = false
            },
            onDismiss = { sectionSheetOpen = false },
        )
    }
}
@Composable
private fun NowPlayingHeader(title: String, onCollapse: () -> Unit, showCharacters: Boolean, onOpenCharacters: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xs, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = stringResource(R.string.collapse_now_playing),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.more_actions),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (showCharacters) DropdownMenuItem(
                    text = { Text("Chữ của bài") },
                    onClick = { menuOpen = false; onOpenCharacters() },
                )
                // Test entry arrives with the test flow (G2c); disabled rather than hidden
                // so the overflow shape is already final.
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.take_test)) },
                    onClick = { menuOpen = false },
                    enabled = false,
                )
            }
        }
    }
}
private fun fakeLine(id: String, text: String, pinyin: String, translation: String) = ReferenceLine(
    id = id,
    type = "line",
    language = "zh",
    speaker = "coach_zh",
    text = text,
    pinyin = pinyin,
    translation = translation,
)

@Preview(showBackground = true)
@Composable
private fun NowPlayingPreview() {
    HanziFlowTheme {
        NowPlayingScreen(
            state = PlayerUiState(
                connected = true,
                playing = true,
                lessonId = "lesson_001",
                lessonTitle = "Đồ ăn mang về của đồng nghiệp",
                track = "guided",
                practiceAvailable = false,
                positionMs = 42_000,
                durationMs = 204_889,
                speed = 1f,
                transcript = TranscriptUiState(
                    lessonId = "lesson_001",
                    track = "guided",
                    lines = listOf(
                        fakeLine("r1", "你好", "nǐ hǎo", "Xin chào"),
                        fakeLine("r2", "我叫安娜。你呢？", "wǒ jiào ānnà. nǐ ne?", "Tôi tên là Anna. Bạn thì?"),
                        fakeLine("r3", "很高兴认识你。", "hěn gāoxìng rènshi nǐ.", "Rất vui được gặp bạn."),
                    ),
                    viewMode = TranscriptViewMode.HANZI_WITH_PINYIN,
                    showVietnamese = true,
                ),
            ),
            onCollapse = {},
            onSwitchTrack = {},
            onSeekTo = {},
            onSeekBack = {},
            onToggle = {},
            onSeekForward = {},
            onSpeed = {},
            onCancelLoop = {},
            onSeekLine = {},
            onToggleLoop = {},
            onLookup = {},
            onViewModeChange = {},
            onVietnameseToggle = {},
            onPlayLookupClip = {},
            onDismissLookup = {},
        )
    }
}
