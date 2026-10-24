package com.hanziflow.ui.notes

import com.hanziflow.shared.NotePattern
import com.hanziflow.shared.NoteQuestion
import com.hanziflow.shared.WordItem

/** Everything on the "Ghi chú" screen, in reading order: patterns, questions, vocabulary. */
data class NotesUiState(
    val lessonId: String? = null,
    val title: String = "",
    val patterns: List<NotePattern> = emptyList(),
    val questions: List<NoteQuestion> = emptyList(),
    val words: List<WordItem> = emptyList(),
)
