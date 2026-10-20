package com.hanziflow.ui.library

import com.hanziflow.data.LessonEntity

data class LibraryUiState(
    val lessons: List<LessonEntity> = emptyList(),
    val baseUrl: String = "",
    val message: String? = null,
)
