package com.hanziflow.ui.player

import com.hanziflow.shared.Items
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.WordItem
import com.hanziflow.shared.transcript.TranscriptViewMode

data class PlayerUiState(
    val connected: Boolean = false,
    val playing: Boolean = false,
    val lessonId: String? = null,
    val lessonTitle: String? = null,
    val track: String = "guided",
    val practiceAvailable: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
    val message: String? = null,
    val transcript: TranscriptUiState = TranscriptUiState(),
)

data class TranscriptUiState(
    val lessonId: String? = null,
    val track: String = "guided",
    val lines: List<ReferenceLine> = emptyList(),
    val cues: List<TimelineCue> = emptyList(),
    val items: Items? = null,
    val viewMode: TranscriptViewMode = TranscriptViewMode.PINYIN_ONLY,
    val showVietnamese: Boolean = true,
    val loopedLineId: String? = null,
    val lookupWord: WordItem? = null,
)
