package com.hanziflow.shared.testgen

import com.hanziflow.shared.Items

/**
 * Deterministic test generator: same (items, targetLessonId, seed, questionCount,
 * excludedActivities) always produces the same [GeneratedTest], byte-identical on
 * JVM and JS. Bump [GENERATOR_VERSION] whenever output changes for the same input.
 */
object TestGenerator {
    const val GENERATOR_VERSION = 1

    /**
     * Activities excluded from cloze/qa generation (e.g. once coach-instruction
     * lines carry a dedicated activity label). Empty by default: every activity in
     * the current lesson_001 item bank ("Nghe chuyện", "Nói theo", "Hỏi đáp",
     * "Đổi một chi tiết", "Túi của Anna", "Kể lại", "Nghe lại", "Giải nghĩa") is
     * story/drill content, not a coach instruction, so none are excluded yet.
     */
    val DEFAULT_EXCLUDED_ACTIVITIES: List<String> = emptyList()

    /**
     * @param lessonItems the target lesson's [Items] plus every earlier lesson's
     *   [Items] (callers must not pass items from a later lesson).
     */
    fun generate(
        lessonItems: List<Items>,
        targetLessonId: String,
        seed: Int,
        questionCount: Int = 12,
        excludedActivities: List<String> = DEFAULT_EXCLUDED_ACTIVITIES,
    ): GeneratedTest {
        val target = lessonItems.first { it.lessonId == targetLessonId }
        val context = buildContext(lessonItems, targetLessonId, seed, excludedActivities)
        val questions = selectQuestions(context, seed, questionCount).map { it.question }
        return GeneratedTest(target.lessonId, target.lessonVersion, GENERATOR_VERSION, seed, questions)
    }
}

/** A chosen spec paired with the attemptIndex and question it was built at. */
internal data class SelectedQuestion(val spec: Spec, val attemptIndex: Int, val question: GeneratedQuestion)

/**
 * Picks which specs become questions: at least half current-lesson (when the bank
 * allows it), no item asked twice unless the bank is too small to avoid it, cloze
 * sentences with >=3 vocabulary tokens preferred over thinner ones.
 */
internal fun selectQuestions(context: GenerationContext, seed: Int, questionCount: Int): List<SelectedQuestion> {
    val allSpecs = context.buildSpecs()
    val current = allSpecs.filter { context.isCurrent(it.lessonId) }
    val earlier = allSpecs.filterNot { context.isCurrent(it.lessonId) }
    val selectRng = Rng(deriveSeed(seed, "select-order"))

    fun richness(spec: Spec): Int = when (spec) {
        is Spec.Cloze -> if (context.vocabTokenCount(spec.sentence) >= 3) 0 else 1
        else -> 0
    }

    fun order(specs: List<Spec>): List<Spec> {
        val (preferred, rest) = specs.partition { richness(it) == 0 }
        return selectRng.shuffled(preferred) + selectRng.shuffled(rest)
    }

    val orderedCurrent = order(current)
    val orderedEarlier = order(earlier)
    val minCurrent = (questionCount + 1) / 2 // ceil(questionCount / 2)

    val result = mutableListOf<SelectedQuestion>()
    val usedKeys = mutableListOf<String>()
    fun keyOf(spec: Spec) = "${spec.type.name}|${spec.itemId}"

    fun tryAdd(spec: Spec, attemptIndex: Int, allowRepeat: Boolean): Boolean {
        if (result.size >= questionCount) return false
        val key = keyOf(spec)
        if (!allowRepeat && key in usedKeys) return false
        val question = context.buildQuestion(spec, attemptIndex) ?: return false
        result.add(SelectedQuestion(spec, attemptIndex, question))
        if (!allowRepeat) usedKeys.add(key)
        return true
    }

    // Pass 1: fill the current-lesson quota first, unique items only.
    for (spec in orderedCurrent) {
        if (result.size >= minCurrent) break
        tryAdd(spec, attemptIndex = 0, allowRepeat = false)
    }

    // Pass 2: fill remaining slots from both tiers, unique items only.
    val remainingCurrent = orderedCurrent.filterNot { keyOf(it) in usedKeys }
    val remainingEarlier = orderedEarlier.filterNot { keyOf(it) in usedKeys }
    val combined = selectRng.shuffled(remainingCurrent + remainingEarlier)
    for (spec in combined) {
        if (result.size >= questionCount) break
        tryAdd(spec, attemptIndex = 0, allowRepeat = false)
    }

    // Pass 3: the bank is too small to reach questionCount without repeats. Repeat
    // items are allowed, but each repeat regenerates a different variant (higher
    // attemptIndex), never a byte-identical duplicate of a question already picked.
    if (result.size < questionCount && (orderedCurrent.isNotEmpty() || orderedEarlier.isNotEmpty())) {
        val everything = selectRng.shuffled(orderedCurrent + orderedEarlier)
        val nextAttempt = mutableMapOf<String, Int>()
        var guard = 0
        val maxGuard = questionCount * 20
        while (result.size < questionCount && guard < maxGuard) {
            val spec = everything[guard % everything.size]
            val key = keyOf(spec)
            val attemptIndex = nextAttempt.getOrElse(key) { 1 }
            if (tryAdd(spec, attemptIndex, allowRepeat = true)) {
                nextAttempt[key] = attemptIndex + 1
            }
            guard++
        }
    }

    return result
}
