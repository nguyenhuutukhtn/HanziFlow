package com.hanziflow.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.hanziflow.R
import com.hanziflow.playback.SectionLoopInfo
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.SectionNavigation
import com.hanziflow.ui.theme.Spacing

/**
 * "▤ <section title> · n/N ▾" on the left (opens the section list), "↻ Lặp phần này"
 * on the right. Hidden when the timeline has no sections (old packages).
 */
@Composable
fun SectionRow(
    sections: List<TimelineSection>,
    positionMs: Long,
    onOpenSectionList: () -> Unit,
    onLoopSection: () -> Unit,
) {
    if (sections.isEmpty()) return
    val current = SectionNavigation.sectionAt(sections, positionMs) ?: return
    val index = sections.indexOf(current) + 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.section_row, current.title, index, sections.size) + " ▾",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onOpenSectionList),
        )
        TextButton(onClick = onLoopSection) {
            Text(stringResource(R.string.loop_section))
        }
    }
}

/** Replaces the line-loop chip while a section is looping; only one loop is active at a time. */
@Composable
fun SectionLoopChip(sectionLoop: SectionLoopInfo, onStopAfterPass: () -> Unit, onNextSection: () -> Unit) {
    AssistChip(
        onClick = onNextSection,
        label = {
            Text(
                "↻ " + if (sectionLoop.stopAfterPass) {
                    stringResource(R.string.loop_section_active_last_pass, sectionLoop.title)
                } else {
                    stringResource(R.string.loop_section_active, sectionLoop.title, sectionLoop.pass)
                },
            )
        },
        trailingIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                TextButton(onClick = onStopAfterPass, enabled = !sectionLoop.stopAfterPass) {
                    Text(stringResource(R.string.stop_section_loop_after_pass))
                }
                TextButton(onClick = onNextSection) {
                    Text(stringResource(R.string.next_section))
                }
            }
        },
        modifier = Modifier.padding(horizontal = Spacing.md),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionListSheet(sections: List<TimelineSection>, currentSectionId: String?, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.section_list_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(Spacing.lg),
        )
        LazyColumn {
            items(sections) { section ->
                val durationLabel = formatTime(section.endMs - section.startMs)
                val selectedDescription = if (section.id == currentSectionId) ", đang nghe" else ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(section.id) }
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        .semantics { contentDescription = "${section.title}$selectedDescription" },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        section.title,
                        style = if (section.id == currentSectionId) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (section.id == currentSectionId) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(durationLabel, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
