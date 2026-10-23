@file:OptIn(ExperimentalJsExport::class)

package com.hanziflow.shared.web

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.characters.CharacterIndex
import com.hanziflow.shared.characters.ComponentEntry
import com.hanziflow.shared.transcript.CueNavigation
import com.hanziflow.shared.transcript.TranscriptCues
import com.hanziflow.shared.transcript.TranscriptRendering
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.shared.transcript.TrackSwitch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
private data class TrackJson(val name: String, @SerialName("display_name") val displayName: String)

@Serializable
private data class TokenJson(val hanzi: String, val pinyin: String?, val showPinyin: Boolean)

@Serializable
private data class LineJson(
    val id: String,
    val type: String,
    val language: String,
    val speaker: String,
    val text: String,
    val pinyin: String?,
    val tokens: List<TokenJson>,
    val showHanzi: Boolean,
    val showPinyinLine: Boolean,
    val translation: String?,
    val showTranslation: Boolean,
    val lineVisible: Boolean,
    val placeholder: Boolean,
)

@Serializable
private data class CharacterJson(
    val char: String,
    val pinyin: String,
    @SerialName("han_viet") val hanViet: String,
    @SerialName("hsk_level") val hskLevel: Int?,
    @SerialName("decomp_ids") val decompIds: String?,
    val semantic: String?,
    val phonetic: String?,
    @SerialName("en_meaning") val enMeaning: String?,
    @SerialName("vi_meaning") val viMeaning: String,
    @SerialName("phonetic_family") val phoneticFamily: List<String>,
    @SerialName("stroke_available") val strokeAvailable: Boolean,
    @SerialName("lesson_words") val lessonWords: List<String>,
    val components: List<ComponentEntry>,
)

/** Small JS-facing facade for one lesson's player data and transcript rules. */
@JsExport
class LessonSessionJs(
    catalogJson: String,
    lessonId: String,
    timelineJson: String,
    referenceJson: String,
    charactersJson: String? = null,
    track: String = "guided",
) {
    private val lesson = CourseJson.catalog(catalogJson).lessons.first { it.id == lessonId }
    private val timeline = CourseJson.timeline(timelineJson)
    private val referenceTracks = CourseJson.reference(referenceJson).tracks
    private val lines = linesFor(track)
    private val characterIndex = charactersJson?.let { CharacterIndex(CourseJson.characters(it)) }

    fun lessonTitle(): String = lesson.title

    fun audioPath(track: String): String? = lesson.tracks.forName(track)?.let { path(it.file) }
    fun timelinePath(track: String): String? = lesson.tracks.forName(track)?.let { path(it.timeline.file) }
    fun referencePath(): String = path(lesson.files.reference.file)
    fun charactersPath(): String? = lesson.files.characters?.let { path(it.file) }

    fun tracksJson(): String = CourseJson.codec.encodeToString(
        buildList {
            if (lesson.format == "modes_v1") {
                val labels = mapOf("hieu" to "Hiểu", "nghe" to "Nghe", "phan_xa" to "Phản xạ", "noi" to "Nói")
                lesson.tracks.names.filter(labels::containsKey).forEach { add(TrackJson(it, labels.getValue(it))) }
            } else {
                if (lesson.tracks.guided != null) add(TrackJson("guided", "Học có hướng dẫn"))
                if (lesson.tracks.guidedZh != null) add(TrackJson("guided_zh", "Chỉ tiếng Trung"))
                if (lesson.tracks.practice != null) add(TrackJson("practice", "Luyện nghe"))
            }
        },
    )

    fun activeCueIndex(positionMs: Double): Int =
        TranscriptCues.activeCueIndex(timeline.cues, positionMs.toLong()) ?: -1

    fun cuePlaceJson(positionMs: Double): String {
        val place = TranscriptCues.placeAt(timeline.cues, positionMs.toLong())
        fun Int?.jsonIndex(): Int = this ?: -1
        return "{\"speaking\":${place.speaking.jsonIndex()},\"last\":${place.lastStarted.jsonIndex()},\"next\":${place.next.jsonIndex()}}"
    }

    fun cueCount(): Int = timeline.cues.size
    fun cueJson(index: Int): String? = timeline.cues.getOrNull(index)?.let { CourseJson.codec.encodeToString(it) }
    fun linesJson(track: String): String = CourseJson.codec.encodeToString(linesFor(track).map { it.id })

    fun lineJson(lineId: String, view: String, showVietnamese: Boolean, positionMs: Double): String? {
        val lineIndex = lines.indexOfFirst { it.id == lineId }
        if (lineIndex < 0) return null
        val line = lines[lineIndex]
        val decision = TranscriptRendering.decide(
            line = line,
            view = TranscriptViewMode.valueOf(view),
            showVietnamese = showVietnamese,
            modelAnswerRevealed = TranscriptCues.isModelAnswerRevealed(timeline.cues, lineId, positionMs.toLong()),
            nextLine = lines.getOrNull(lineIndex + 1),
        )
        return CourseJson.codec.encodeToString(
            LineJson(
                id = line.id,
                type = line.type,
                language = line.language,
                speaker = line.speaker,
                text = line.text,
                pinyin = line.pinyin,
                tokens = decision.tokens.map { TokenJson(it.token.hanzi, it.token.pinyin, it.showPinyin) },
                showHanzi = decision.showHanzi,
                showPinyinLine = decision.showPinyinLine,
                translation = line.translation,
                showTranslation = decision.showTranslation,
                lineVisible = decision.lineVisible,
                placeholder = decision.placeholder,
            ),
        )
    }

    fun seekTargetMs(lineId: String, positionMs: Double): Double? =
        TranscriptCues.seekTargetMs(timeline.cues, lineId, positionMs.toLong())?.toDouble()

    fun nextCueStartMs(positionMs: Double): Double? =
        CueNavigation.nextCueStartMs(timeline.cues, positionMs.toLong())?.toDouble()

    fun previousCueStartMs(positionMs: Double): Double? =
        CueNavigation.previousCueStartMs(timeline.cues, positionMs.toLong())?.toDouble()

    fun sectionsJson(): String = CourseJson.codec.encodeToString(timeline.sections)

    fun characterJson(char: String): String? {
        val index = characterIndex ?: return null
        val entry = index.entry(char) ?: return null
        return CourseJson.codec.encodeToString(
            CharacterJson(
                char = entry.char,
                pinyin = entry.pinyin,
                hanViet = entry.hanViet,
                hskLevel = entry.hskLevel,
                decompIds = entry.decompIds,
                semantic = entry.semantic,
                phonetic = entry.phonetic,
                enMeaning = entry.enMeaning,
                viMeaning = entry.viMeaning,
                phoneticFamily = entry.phoneticFamily,
                strokeAvailable = entry.strokeAvailable,
                lessonWords = entry.lessonWords,
                components = index.componentsOf(char),
            ),
        )
    }

    private fun path(file: String): String = lesson.baseUrl.trimEnd('/') + "/" + file

    private fun linesFor(track: String) = if (track == "guided_zh") referenceTracks.guided.orEmpty() else referenceTracks.forName(track).orEmpty()
}

/** JS entry point for the small library-list payload needed before opening a lesson. */
@JsExport
object CatalogJs {
    fun lessonsJson(catalogJson: String): String = CourseJson.codec.encodeToString(
        CourseJson.catalog(catalogJson).lessons.map { lesson ->
            CatalogLessonJson(lesson.id, lesson.title, lesson.order, lesson.version)
        },
    )
}

/** JS entry point for pure position mapping between published track timelines. */
@JsExport
object TrackSwitchJs {
    fun positionFor(fromTimelineJson: String, toTimelineJson: String, positionMs: Double): Double {
        // Do not quantize a no-op track change to a cue boundary.
        if (fromTimelineJson == toTimelineJson) return positionMs
        return TrackSwitch.positionFor(
            CourseJson.timeline(fromTimelineJson).cues,
            CourseJson.timeline(toTimelineJson).cues,
            positionMs.toLong(),
        ).toDouble()
    }
}

@Serializable
private data class CatalogLessonJson(val id: String, val title: String, val order: Int, val version: Int)
