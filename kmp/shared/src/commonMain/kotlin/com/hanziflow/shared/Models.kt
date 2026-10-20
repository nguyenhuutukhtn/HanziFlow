@file:OptIn(ExperimentalJsExport::class)

package com.hanziflow.shared

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Clip(val file: String, val bytes: Long, val sha256: String)

@Serializable
data class Track(
    val file: String,
    val bytes: Long,
    @SerialName("duration_ms") val durationMs: Long,
    val sha256: String,
    val timeline: Clip,
)

@Serializable
data class Tracks(val guided: Track, val practice: Track? = null)

@Serializable
data class PackageFiles(val manifest: Clip, val reference: Clip, val items: Clip)

@Serializable
data class CatalogLesson(
    val id: String,
    val version: Int,
    val order: Int,
    val title: String,
    val level: String,
    @SerialName("base_url") val baseUrl: String,
    val tracks: Tracks,
    val files: PackageFiles,
)

@Serializable
data class Catalog(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("catalog_version") val catalogVersion: Int,
    @SerialName("generated_at") val generatedAt: String? = null,
    val lessons: List<CatalogLesson>,
)

@Serializable
data class LessonManifest(
    val id: String,
    val version: Int,
    val title: String,
    val level: String,
    @SerialName("communicative_goals") val communicativeGoals: List<String> = emptyList(),
    @SerialName("new_patterns") val newPatterns: List<String> = emptyList(),
)

@Serializable
data class Token(
    val hanzi: String,
    val pinyin: String? = null,
    @SerialName("item_id") val itemId: String? = null,
)

@Serializable
data class ReferenceLine(
    val id: String,
    val type: String,
    val language: String,
    val speaker: String,
    val text: String,
    val pinyin: String? = null,
    val translation: String? = null,
    val tokens: List<Token>? = null,
)

@Serializable
data class ReferenceTracks(
    val guided: List<ReferenceLine>,
    val practice: List<ReferenceLine>? = null,
)

@Serializable
data class Reference(val tracks: ReferenceTracks)

@Serializable
data class WordItem(
    val id: String,
    val hanzi: String,
    val pinyin: String,
    val tones: List<Int>,
    @SerialName("meaning_vi") val meaningVi: String,
    val clip: Clip? = null,
)

@Serializable
data class SentenceItem(
    val id: String,
    val activity: String,
    val speaker: String,
    val hanzi: String,
    val pinyin: String,
    @SerialName("meaning_vi") val meaningVi: String,
    val clip: Clip? = null,
    val tokens: List<Token>? = null,
)

@Serializable
data class QaText(
    val hanzi: String,
    val pinyin: String? = null,
    @SerialName("meaning_vi") val meaningVi: String? = null,
    val clip: Clip? = null,
)

@Serializable
data class QaItem(
    val id: String,
    val activity: String,
    val speaker: String,
    val question: QaText,
    val answer: QaText,
)

@Serializable
data class Items(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_version") val lessonVersion: Int,
    val words: List<WordItem> = emptyList(),
    val sentences: List<SentenceItem> = emptyList(),
    val qa: List<QaItem> = emptyList(),
) {
    fun clips(): List<Clip> = buildList {
        words.mapNotNullTo(this) { it.clip }
        sentences.mapNotNullTo(this) { it.clip }
        qa.forEach { pair ->
            pair.question.clip?.let(::add)
            pair.answer.clip?.let(::add)
        }
    }.distinctBy { it.file }
}

@Serializable
data class TimelineCue(
    @SerialName("line_id") val lineId: String,
    @SerialName("start_ms") val startMs: Long,
    @SerialName("end_ms") val endMs: Long,
    @SerialName("line_ids") val lineIds: List<String>? = null,
    val granularity: String? = null,
    val language: String? = null,
    val type: String? = null,
    @SerialName("section_id") val sectionId: String? = null,
)

@Serializable
data class TimelineSection(
    val id: String,
    val title: String,
    @SerialName("start_ms") val startMs: Long,
    @SerialName("end_ms") val endMs: Long,
)

@Serializable
data class Timeline(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_version") val lessonVersion: Int,
    val track: String,
    @SerialName("duration_ms") val durationMs: Long,
    val cues: List<TimelineCue>,
    val sections: List<TimelineSection> = emptyList(),
)

object CourseJson {
    val codec = Json { ignoreUnknownKeys = true }
    fun catalog(value: String): Catalog = codec.decodeFromString(value)
    fun lesson(value: String): LessonManifest = codec.decodeFromString(value)
    fun reference(value: String): Reference = codec.decodeFromString(value)
    fun items(value: String): Items = codec.decodeFromString(value)
    fun timeline(value: String): Timeline = codec.decodeFromString(value)
}

@JsExport
object ContractParser {
    fun catalogLessonCount(json: String): Int = CourseJson.catalog(json).lessons.size
    fun catalogVersion(json: String): Int = CourseJson.catalog(json).catalogVersion
    fun normalizeItems(json: String): String = CourseJson.codec.encodeToString(CourseJson.items(json))
}

