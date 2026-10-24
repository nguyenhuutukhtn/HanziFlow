package com.hanziflow.ui.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hanziflow.R
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.appFilterChipColors

@Composable
fun TranscriptViewSwitcher(viewMode: TranscriptViewMode, showVietnamese: Boolean, onViewModeChange: (TranscriptViewMode) -> Unit, onVietnameseToggle: (Boolean) -> Unit, modifier: Modifier = Modifier, vietnameseEnabled: Boolean = true, trailingSwitch: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(TranscriptViewMode.HIDDEN to R.string.view_hidden, TranscriptViewMode.PINYIN_ONLY to R.string.view_pinyin, TranscriptViewMode.HANZI_WITH_PINYIN to R.string.view_hanzi_pinyin, TranscriptViewMode.HANZI_ONLY to R.string.view_hanzi_only).forEach { (mode, label) -> FilterChip(selected = viewMode == mode, onClick = { onViewModeChange(mode) }, label = { Text(stringResource(label)) }, colors = appFilterChipColors()) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(stringResource(R.string.vietnamese_switch), style = MaterialTheme.typography.labelMedium); Switch(showVietnamese, onVietnameseToggle, enabled = vietnameseEnabled) }; trailingSwitch() }
    }
}
