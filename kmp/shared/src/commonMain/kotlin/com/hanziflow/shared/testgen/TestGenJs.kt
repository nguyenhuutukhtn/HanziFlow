@file:OptIn(ExperimentalJsExport::class)

package com.hanziflow.shared.testgen

import com.hanziflow.shared.CourseJson
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.encodeToString

/** Web (JS) facade for one-shot test generation: items JSON in, test JSON out. */
@JsExport
object TestGeneratorJs {
    fun generate(
        itemsJsonArray: Array<String>,
        targetLessonId: String,
        seed: Int,
        questionCount: Int,
    ): String {
        val items = itemsJsonArray.map { CourseJson.items(it) }
        val test = TestGenerator.generate(items, targetLessonId, seed, questionCount)
        return CourseJson.codec.encodeToString(test)
    }
}

/**
 * Web (JS) facade for [TestSession]. Exposes only primitives and JSON strings so
 * it is directly usable from TypeScript without exporting Kotlin data classes.
 */
@JsExport
class TestSessionJs(
    itemsJsonArray: Array<String>,
    targetLessonId: String,
    seed: Int,
    questionCount: Int,
) {
    private val session = TestSession.create(
        itemsJsonArray.map { CourseJson.items(it) },
        targetLessonId,
        seed,
        questionCount,
    )

    /** JSON of the question currently at the head of the queue, or null when finished. */
    fun currentQuestionJson(): String? = session.current()?.let { CourseJson.codec.encodeToString(it) }

    fun answer(choiceIndex: Int): Boolean = session.answer(choiceIndex)

    /** JSON of the revealed question, or null if already finished. */
    fun reveal(): String? = session.reveal()?.let { CourseJson.codec.encodeToString(it) }

    fun isFinished(): Boolean = session.isFinished
    fun isPassed(): Boolean = session.isPassed
    fun mistakeCount(): Int = session.mistakeCount
    fun remainingCount(): Int = session.remainingCount
    fun summaryJson(): String = CourseJson.codec.encodeToString(session.summary())
}
