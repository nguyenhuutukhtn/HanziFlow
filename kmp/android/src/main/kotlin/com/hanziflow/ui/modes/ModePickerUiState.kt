package com.hanziflow.ui.modes

data class ModePickerUiState(
    val lessonId: String? = null,
    val title: String = "",
    val modes: List<ModePickerItem> = emptyList(),
    val notesAvailable: Boolean = false,
)

data class ModePickerItem(
    val id: String,
    val label: String,
    val whenToUse: String,
    val durationMs: Long,
)
