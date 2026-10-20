package com.hanziflow.playback

import com.hanziflow.data.CourseRepository
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineCue
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.ChineseOnlyPlan
import com.hanziflow.shared.transcript.CueNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Headset / transport cue navigation, section lookups and Chinese-only skipping, all
 * driven from the timeline the service reads itself (so this works with the screen
 * off and the UI process gone). One file read per lesson/track; dropped on transition.
 */
class CueSkipper(private val courseRepository: CourseRepository) {
    private var cache: Cache? = null
    var chineseOnlyEnabled: Boolean = false
        private set

    fun onMediaItemChanged() {
        cache = null
        chineseOnlyEnabled = false
    }

    fun setChineseOnly(enabled: Boolean) {
        chineseOnlyEnabled = enabled
    }

    suspend fun cuesFor(lessonId: String, track: String): List<TimelineCue> = load(lessonId, track).cues

    suspend fun sectionsFor(lessonId: String, track: String): List<TimelineSection> = load(lessonId, track).sections

    suspend fun nextTarget(lessonId: String, track: String, positionMs: Long): Long? {
        val cues = cuesFor(lessonId, track)
        return CueNavigation.nextCueStartMs(cues, positionMs, skipTargetIn(cues))
    }

    suspend fun previousTarget(lessonId: String, track: String, positionMs: Long): Long? {
        val cues = cuesFor(lessonId, track)
        return CueNavigation.previousCueStartMs(cues, positionMs, skipTargetIn(cues))
    }

    /** A skip range's bounds, for logging/applying a hit at [rangeStartMs]. */
    data class SkipHit(val rangeStartMs: Long, val targetMs: Long)

    /** The position-check tick's fallback: the skip range (if any) containing [positionMs]. */
    suspend fun skipHitAt(lessonId: String, track: String, positionMs: Long): SkipHit? {
        if (!chineseOnlyEnabled) return null
        val range = ChineseOnlyPlan.rangeFor(cuesFor(lessonId, track), positionMs) ?: return null
        return SkipHit(range.fromMs, range.toMs)
    }

    private fun skipTargetIn(cues: List<TimelineCue>): (Long) -> Long? =
        { pos -> if (chineseOnlyEnabled) ChineseOnlyPlan.skipTargetFor(cues, pos) else null }

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
