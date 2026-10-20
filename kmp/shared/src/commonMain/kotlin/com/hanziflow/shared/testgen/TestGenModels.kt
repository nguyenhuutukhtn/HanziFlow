package com.hanziflow.shared.testgen

import com.hanziflow.shared.Clip
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QuestionType {
    @SerialName("listen_word") LISTEN_WORD,
    @SerialName("hanzi_to_pinyin") HANZI_TO_PINYIN,
    @SerialName("hanzi_to_meaning") HANZI_TO_MEANING,
    @SerialName("meaning_to_hanzi") MEANING_TO_HANZI,
    @SerialName("tone") TONE,
    @SerialName("cloze") CLOZE,
    @SerialName("qa") QA,
}

@Serializable
data class QuestionOption(val text: String)

@Serializable
data class GeneratedQuestion(
    val id: String,
    val type: QuestionType,
    @SerialName("item_id") val itemId: String,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("prompt_text") val promptText: String? = null,
    @SerialName("prompt_clip") val promptClip: Clip? = null,
    val options: List<QuestionOption>,
    @SerialName("answer_index") val answerIndex: Int,
)

@Serializable
data class GeneratedTest(
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_version") val lessonVersion: Int,
    @SerialName("generator_version") val generatorVersion: Int,
    val seed: Int,
    val questions: List<GeneratedQuestion>,
)

/** Mirrors the Supabase `test_attempts` columns that the generator/session own. */
@Serializable
data class AttemptSummary(
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_version") val lessonVersion: Int,
    @SerialName("generator_version") val generatorVersion: Int,
    val seed: Int,
    @SerialName("question_count") val questionCount: Int,
    val mistakes: Int,
    val passed: Boolean,
)
