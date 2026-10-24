package com.hanziflow.ui.player

/** Clears metadata that only belongs to the previously playing lesson. */
internal fun PlayerUiState.forPlaybackLesson(lessonId: String?): PlayerUiState {
    if (this.lessonId == lessonId) return this
    return copy(
        lessonId = lessonId,
        lessonTitle = null,
        lessonFormat = "linear_v1",
        practiceAvailable = false,
        chineseOnlyAvailable = false,
    )
}
