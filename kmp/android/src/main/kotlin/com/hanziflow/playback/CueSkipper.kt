package com.hanziflow.playback

import com.hanziflow.data.CourseRepository
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.CueNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Headset / transport cue navigation and section lookups, driven from the timeline
 * the service reads itself. One file read per lesson/track; dropped on transition.
 */
class CueSkipper(private val courseRepository: CourseRepository) {
    private var cache: Cache? = null

    fun onMediaItemChanged() {
        cache = null
    }

    suspend fun cuesFor(lessonId: String, track: String) = load(lessonId, track).cues

    suspend fun sectionsFor(lessonId: String, track: String): List<TimelineSection> = load(lessonId, track).sections

    suspend fun nextTarget(lessonId: String, track: String, positionMs: Long): Long? {
        val cues = cuesFor(lessonId, track)
        return CueNavigation.nextCueStartMs(cues, positionMs)
    }

    suspend fun previousTarget(lessonId: String, track: String, positionMs: Long): Long? {
        val cues = cuesFor(lessonId, track)
        return CueNavigation.previousCueStartMs(cues, positionMs)
    }

    private suspend fun load(lessonId: String, track: String): Cache {
        cache?.takeIf { it.lessonId == lessonId && it.track == track }?.let { return it }
        return withContext(Dispatchers.IO) {
            val timeline = courseRepository.timelineFile(lessonId, track)
                ?.let { file -> runCatching { CourseJson.timeline(file.readText()) }.getOrNull() }
            Cache(lessonId, track, timeline?.cues.orEmpty(), timeline?.sections.orEmpty()).also { cache = it }
        }
    }

    private data class Cache(
        val lessonId: String,
        val track: String,
        val cues: List<TimelineCue>,
        val sections: List<TimelineSection>,
    )
}
