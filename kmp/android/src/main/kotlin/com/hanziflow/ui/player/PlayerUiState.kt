package com.hanziflow.ui.player

import com.hanziflow.playback.SectionLoopInfo
import com.hanziflow.shared.Items
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.WordItem
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.shared.characters.CharacterIndex
import com.hanziflow.shared.characters.StrokePack

data class PlayerUiState(
    val connected: Boolean = false,
    val playing: Boolean = false,
    val lessonId: String? = null,
    val lessonTitle: String? = null,
    val track: String = "guided",
    val practiceAvailable: Boolean = false,
    val chineseOnlyAvailable: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
    val message: String? = null,
    val sectionLoop: SectionLoopInfo? = null,
    val chineseOnly: Boolean = false,
    val transcript: TranscriptUiState = TranscriptUiState(),
    val characterIndex: CharacterIndex? = null,
    val strokePack: StrokePack? = null,
)

data class TranscriptUiState(
    val lessonId: String? = null,
    val track: String = "guided",
    val lines: List<ReferenceLine> = emptyList(),
    val cues: List<TimelineCue> = emptyList(),
    val sections: List<TimelineSection> = emptyList(),
    val items: Items? = null,
    val viewMode: TranscriptViewMode = TranscriptViewMode.PINYIN_ONLY,
    val showVietnamese: Boolean = true,
    val loopedLineId: String? = null,
    val lookupWord: WordItem? = null,
    val lookupCharacter: String? = null,
    val lookupLineText: String? = null,
)
