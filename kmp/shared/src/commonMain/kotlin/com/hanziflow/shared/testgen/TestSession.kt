package com.hanziflow.shared.testgen

import com.hanziflow.shared.Items

/**
 * Processes answer(choiceIndex) / reveal() against a generated test, applying the
 * app's pass rule (see docs/app_architecture.md "Test generation"): a wrong answer
 * keeps the attempt going and appends a different question of the same type on the
 * same item to the end of the queue; a reveal marks the attempt as not passable.
 * Finished when the queue is empty; passed only when finished and nothing was
 * revealed.
 */
class TestSession private constructor(
    val lessonId: String,
    val lessonVersion: Int,
    val generatorVersion: Int,
    val seed: Int,
    val questionCount: Int,
    private val context: GenerationContext,
    initial: List<SelectedQuestion>,
) {
    private class Entry(val spec: Spec, var attemptIndex: Int, val question: GeneratedQuestion)

    private val queue: MutableList<Entry> = initial.map { Entry(it.spec, it.attemptIndex, it.question) }.toMutableList()
    private var mistakes = 0
    private var revealed = false

    /** The question currently at the head of the queue, or null when finished. */
    fun current(): GeneratedQuestion? = queue.firstOrNull()?.question

    /**
     * Submits an answer for the current question. Returns true if correct. A wrong
     * answer requeues a different question of the same type on the same item at
     * the end of the queue. Does nothing (returns false) if already finished.
     */
    fun answer(choiceIndex: Int): Boolean {
        val entry = queue.removeFirstOrNull() ?: return false
        val correct = choiceIndex == entry.question.answerIndex
        if (!correct) {
            mistakes++
            queue.add(regenerate(entry))
        }
        return correct
    }

    /** Reveals the current question's answer, consuming it without a correct answer. */
    fun reveal(): GeneratedQuestion? {
        val entry = queue.removeFirstOrNull() ?: return null
        revealed = true
        return entry.question
    }

    val isFinished: Boolean get() = queue.isEmpty()
    val isPassed: Boolean get() = isFinished && !revealed
    val mistakeCount: Int get() = mistakes
    val remainingCount: Int get() = queue.size

    fun summary(): AttemptSummary = AttemptSummary(
        lessonId = lessonId,
        lessonVersion = lessonVersion,
        generatorVersion = generatorVersion,
        seed = seed,
        questionCount = questionCount,
        mistakes = mistakes,
        passed = isPassed,
    )

    /** Builds the requeued entry: same spec/item/type, a higher attemptIndex, and
     * content that differs from the question just missed whenever the data on hand
     * allows it (bounded search, falls back to accepting a repeat on tiny banks). */
    private fun regenerate(missed: Entry): Entry {
        var attemptIndex = missed.attemptIndex
        var question = missed.question
        for (i in 1..MAX_REGENERATE_TRIES) {
            attemptIndex = missed.attemptIndex + i
            val candidate = context.buildQuestion(missed.spec, attemptIndex) ?: continue
            question = candidate
            if (!contentEquals(candidate, missed.question)) break
        }
        return Entry(missed.spec, attemptIndex, question)
    }

    private fun contentEquals(a: GeneratedQuestion, b: GeneratedQuestion): Boolean =
        a.promptText == b.promptText && a.options == b.options && a.answerIndex == b.answerIndex

    companion object {
        private const val MAX_REGENERATE_TRIES = 8

        fun create(
            lessonItems: List<Items>,
            targetLessonId: String,
            seed: Int,
            questionCount: Int = 12,
            excludedActivities: List<String> = TestGenerator.DEFAULT_EXCLUDED_ACTIVITIES,
        ): TestSession {
            val target = lessonItems.first { it.lessonId == targetLessonId }
            val context = buildContext(lessonItems, targetLessonId, seed, excludedActivities)
            val initial = selectQuestions(context, seed, questionCount)
            return TestSession(
                lessonId = target.lessonId,
                lessonVersion = target.lessonVersion,
                generatorVersion = TestGenerator.GENERATOR_VERSION,
                seed = seed,
                questionCount = initial.size,
                context = context,
                initial = initial,
            )
        }
    }
}
