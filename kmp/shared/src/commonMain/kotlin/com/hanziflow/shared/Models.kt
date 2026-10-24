@file:OptIn(ExperimentalJsExport::class)

package com.hanziflow.shared

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import com.hanziflow.shared.characters.LessonCharacters
import com.hanziflow.shared.characters.StrokeEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

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

@Serializable(with = TracksSerializer::class)
data class Tracks(val values: Map<String, Track>) {
    val guided: Track? get() = forName("guided")
    val practice: Track? get() = forName("practice")
    val guidedZh: Track? get() = forName("guided_zh")
    val names: List<String>
        get() = (DISPLAY_ORDER.filter(values::containsKey) + (values.keys - DISPLAY_ORDER.toSet()).sorted())

    /** Track by its published name; null if absent. */
    fun forName(name: String): Track? = values[name]

    private companion object {
        val DISPLAY_ORDER = listOf("hieu", "tu_vung", "nghe", "phan_xa", "noi", "guided", "guided_zh", "practice")
    }
}

object TracksSerializer : KSerializer<Tracks> {
    private val delegate = MapSerializer(String.serializer(), Track.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: Tracks) = encoder.encodeSerializableValue(delegate, value.values)
    override fun deserialize(decoder: Decoder): Tracks = Tracks(decoder.decodeSerializableValue(delegate))
}

@Serializable(with = ReferenceTracksSerializer::class)
data class ReferenceTracks(val values: Map<String, List<ReferenceLine>>) {
    val guided: List<ReferenceLine>? get() = forName("guided")
    val practice: List<ReferenceLine>? get() = forName("practice")
    fun forName(name: String): List<ReferenceLine>? = values[name]
}

object ReferenceTracksSerializer : KSerializer<ReferenceTracks> {
    private val delegate = MapSerializer(String.serializer(), ListSerializer(ReferenceLine.serializer()))
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: ReferenceTracks) = encoder.encodeSerializableValue(delegate, value.values)
    override fun deserialize(decoder: Decoder): ReferenceTracks = ReferenceTracks(decoder.decodeSerializableValue(delegate))
}

@Serializable
data class PackageFiles(val manifest: Clip, val reference: Clip, val items: Clip, val characters: Clip? = null, val notes: Clip? = null)

@Serializable
data class StrokePackRef(val version: Int, val files: Map<String, StrokePackFile>)

@Serializable
data class StrokePackFile(val bytes: Long, val sha256: String)

@Serializable
data class CatalogLesson(
    val id: String,
    val version: Int,
    val order: Int,
    val title: String,
    val level: String,
    @SerialName("base_url") val baseUrl: String,
    val format: String = "linear_v1",
    val tracks: Tracks,
    val files: PackageFiles,
)

@Serializable
data class Catalog(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("catalog_version") val catalogVersion: Int,
    @SerialName("generated_at") val generatedAt: String? = null,
    val lessons: List<CatalogLesson>,
    @SerialName("stroke_pack") val strokePack: StrokePackRef? = null,
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
data class NoteExample(val hanzi: String, val pinyin: String, @SerialName("meaning_vi") val meaningVi: String)

@Serializable
data class NotePattern(
    val id: String,
    val pattern: String,
    @SerialName("explain_vi") val explainVi: String,
    val examples: List<NoteExample> = emptyList(),
)

@Serializable
data class NoteQuestion(
    val id: String,
    val hanzi: String,
    val pinyin: String,
    @SerialName("meaning_vi") val meaningVi: String,
    @SerialName("how_to_answer_vi") val howToAnswerVi: String,
    val examples: List<NoteExample> = emptyList(),
)

/** Optional `notes.json`: written explanations of the lesson's patterns and question forms. */
@Serializable
data class LessonNotes(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_version") val lessonVersion: Int,
    val patterns: List<NotePattern> = emptyList(),
    val questions: List<NoteQuestion> = emptyList(),
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
data class TimelineGap(
    @SerialName("start_ms") val startMs: Long,
    @SerialName("end_ms") val endMs: Long,
    val kind: String,
    @SerialName("after_line_id") val afterLineId: String,
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
    val gaps: List<TimelineGap> = emptyList(),
)

object CourseJson {
    val codec = Json { ignoreUnknownKeys = true }
    fun catalog(value: String): Catalog = codec.decodeFromString(value)
    fun lesson(value: String): LessonManifest = codec.decodeFromString(value)
    fun reference(value: String): Reference = codec.decodeFromString(value)
    fun items(value: String): Items = codec.decodeFromString(value)
    fun notes(value: String): LessonNotes = codec.decodeFromString(value)
    fun timeline(value: String): Timeline = codec.decodeFromString(value)
    fun characters(value: String): LessonCharacters = codec.decodeFromString(value)
    fun strokePack(value: String): com.hanziflow.shared.characters.StrokePack =
        codec.parseToJsonElement(value).jsonObject.mapNotNull { (char, entry) ->
            runCatching { char to codec.decodeFromJsonElement(StrokeEntry.serializer(), entry) }.getOrNull()
        }.toMap()
}

@JsExport
object ContractParser {
    fun catalogLessonCount(json: String): Int = CourseJson.catalog(json).lessons.size
    fun catalogVersion(json: String): Int = CourseJson.catalog(json).catalogVersion
    fun normalizeItems(json: String): String = CourseJson.codec.encodeToString(CourseJson.items(json))
}
