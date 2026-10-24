package com.hanziflow.ui.player

import com.hanziflow.data.CourseRepository
import com.hanziflow.data.LessonEntity
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.Items
import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.Timeline
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

internal data class LoadedTranscript(
    val lines: List<ReferenceLine>,
    val timeline: Timeline,
    val items: Items?,
    val lessonFormat: String,
)

internal suspend fun CourseRepository.loadTranscript(lessonId: String, track: String): LoadedTranscript? {
    val referenceFile = referenceFile(lessonId) ?: return null
    val timelineFile = timelineFile(lessonId, track) ?: return null
    val reference = CourseJson.reference(referenceFile.readText())
    val lines = if (track == "guided_zh") reference.tracks.guided.orEmpty() else reference.tracks.forName(track).orEmpty()
    val timeline = CourseJson.timeline(timelineFile.readText())
    val items = itemsFile(lessonId)?.let { CourseJson.items(it.readText()) }
    val lessonFormat = catalogLesson(lessonId)?.format ?: "linear_v1"
    return LoadedTranscript(lines, timeline, items, lessonFormat)
}

internal suspend fun lessonTitle(lessons: StateFlow<List<LessonEntity>>, lessonId: String): String? =
    lessons.value.firstOrNull { it.id == lessonId }?.title
        ?: withTimeoutOrNull(2_000) {
            lessons.first { list -> list.any { it.id == lessonId } }
                .first { it.id == lessonId }
                .title
        }
