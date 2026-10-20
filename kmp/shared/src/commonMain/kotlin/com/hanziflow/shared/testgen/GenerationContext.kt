package com.hanziflow.shared.testgen

import com.hanziflow.shared.Items
import com.hanziflow.shared.QaItem
import com.hanziflow.shared.SentenceItem
import com.hanziflow.shared.WordItem

internal data class TaggedWord(val word: WordItem, val lessonId: String)
internal data class TaggedSentence(val sentence: SentenceItem, val lessonId: String)
internal data class TaggedQa(val qa: QaItem, val lessonId: String)

/** One candidate (item, question-type) source the generator can draw a question from. */
internal sealed class Spec(val itemId: String, val lessonId: String, val type: QuestionType) {
    class ListenWord(val word: WordItem, lessonId: String) : Spec(word.id, lessonId, QuestionType.LISTEN_WORD)
    class HanziToPinyin(val word: WordItem, lessonId: String) : Spec(word.id, lessonId, QuestionType.HANZI_TO_PINYIN)
    class HanziToMeaning(val word: WordItem, lessonId: String) : Spec(word.id, lessonId, QuestionType.HANZI_TO_MEANING)
    class MeaningToHanzi(val word: WordItem, lessonId: String) : Spec(word.id, lessonId, QuestionType.MEANING_TO_HANZI)
    class Tone(val word: WordItem, lessonId: String) : Spec(word.id, lessonId, QuestionType.TONE)
    class Cloze(val sentence: SentenceItem, val tokenIndex: Int, lessonId: String) :
        Spec(sentence.id, lessonId, QuestionType.CLOZE)
    class Qa(val qa: QaItem, lessonId: String) : Spec(qa.id, lessonId, QuestionType.QA)
}

/**
 * Holds the flattened item pools for a generation request and knows how to turn a
 * [Spec] into a concrete [GeneratedQuestion]. Shared by [TestGenerator.generate] (a
 * one-shot test) and [TestSession] (which must regenerate a different question of
 * the same type on the same item after a wrong answer, from the same pools).
 */
internal class GenerationContext(
    val seed: Int,
    val targetLessonId: String,
    val wordPool: List<TaggedWord>,
    val sentencePool: List<TaggedSentence>,
    val qaPool: List<TaggedQa>,
) {
    private val wordById: Map<String, WordItem> = wordPool.associate { it.word.id to it.word }

    fun wordLessonId(word: WordItem): String = wordPool.first { it.word.id == word.id }.lessonId

    fun isCurrent(lessonId: String): Boolean = lessonId == targetLessonId

    /** All feasible specs this lesson bank can produce, current-lesson specs first. */
    fun buildSpecs(): List<Spec> {
        val specs = mutableListOf<Spec>()
        for (tw in wordPool) {
            val w = tw.word
            if (w.clip != null) specs.add(Spec.ListenWord(w, tw.lessonId))
            specs.add(Spec.HanziToPinyin(w, tw.lessonId))
            specs.add(Spec.HanziToMeaning(w, tw.lessonId))
            specs.add(Spec.MeaningToHanzi(w, tw.lessonId))
            if (w.tones.size == 1) specs.add(Spec.Tone(w, tw.lessonId))
        }
        for (ts in sentencePool) {
            val tokens = ts.sentence.tokens.orEmpty()
            val blankable = tokens.indices.filter { tokens[it].itemId != null }
            if (blankable.isEmpty() || ts.sentence.clip == null) continue
            // Prefer sentences with >= 3 vocabulary tokens, but still allow shorter ones.
            for (idx in blankable) {
                specs.add(Spec.Cloze(ts.sentence, idx, ts.lessonId))
            }
        }
        for (tq in qaPool) {
            if (tq.qa.question.clip != null) specs.add(Spec.Qa(tq.qa, tq.lessonId))
        }
        return specs
    }

    /** Vocabulary-token count of a sentence, used to prefer richer cloze candidates. */
    fun vocabTokenCount(sentence: SentenceItem): Int = sentence.tokens.orEmpty().count { it.itemId != null }

    /**
     * Builds the concrete question for [spec] at [attemptIndex] (0 for the first time
     * the item is asked, incremented for each requeue after a wrong answer). Returns
     * null if the data on hand cannot support a valid question (fewer than 2 usable
     * distractors, i.e. fewer than 3 total options).
     */
    fun buildQuestion(spec: Spec, attemptIndex: Int): GeneratedQuestion? {
        val rng = Rng(deriveSeed(seed, spec.itemId, spec.type.name, attemptIndex))
        val id = "${spec.lessonId}:${spec.itemId}:${spec.type.name.lowercase()}:$attemptIndex"
        return when (spec) {
            is Spec.ListenWord -> buildListenWord(spec, rng, id)
            is Spec.HanziToPinyin -> buildWordQuestion(
                spec.word, spec.lessonId, QuestionType.HANZI_TO_PINYIN, rng, id,
                promptText = spec.word.hanzi, promptClip = null,
                correctText = spec.word.pinyin, attr = { it.pinyin },
            )
            is Spec.HanziToMeaning -> buildWordQuestion(
                spec.word, spec.lessonId, QuestionType.HANZI_TO_MEANING, rng, id,
                promptText = spec.word.hanzi, promptClip = null,
                correctText = spec.word.meaningVi, attr = { it.meaningVi },
            )
            is Spec.MeaningToHanzi -> buildWordQuestion(
                spec.word, spec.lessonId, QuestionType.MEANING_TO_HANZI, rng, id,
                promptText = spec.word.meaningVi, promptClip = null,
                correctText = spec.word.hanzi, attr = { it.hanzi },
            )
            is Spec.Tone -> buildTone(spec, rng, id)
            is Spec.Cloze -> buildCloze(spec, rng, id)
            is Spec.Qa -> buildQa(spec, rng, id)
        }
    }

    private fun tieredWordDistractors(correct: WordItem, rng: Rng): List<WordItem> {
        val correctBase = toneless(correct.pinyin)
        val correctKey = confusableKey(correct.pinyin)
        val candidates = wordPool.map { it.word }.filter { it.id != correct.id }
        val tier1 = candidates.filter { toneless(it.pinyin) == correctBase && it.tones != correct.tones }
        val tier1Ids = tier1.map { it.id }.toSet()
        val tier2 = candidates.filter { it.id !in tier1Ids && confusableKey(it.pinyin) == correctKey }
        val tier2Ids = tier2.map { it.id }.toSet()
        val tier3 = candidates.filter { it.id !in tier1Ids && it.id !in tier2Ids }
        return rng.shuffled(tier1) + rng.shuffled(tier2) + rng.shuffled(tier3)
    }

    /** Builds up to [maxOptions] unique option texts (correct + distinct distractors). */
    private fun buildOptionTexts(
        correctText: String,
        distractorSource: List<String>,
        rng: Rng,
        maxOptions: Int = 4,
    ): Pair<List<String>, Int>? {
        val texts = mutableListOf(correctText)
        for (candidate in distractorSource) {
            if (texts.size >= maxOptions) break
            if (candidate != correctText && candidate !in texts) texts.add(candidate)
        }
        if (texts.size < 3) return null
        val order = rng.shuffled(texts.indices.toList())
        val shuffledTexts = order.map { texts[it] }
        val answerIndex = order.indexOf(0)
        return shuffledTexts to answerIndex
    }

    private fun buildWordQuestion(
        word: WordItem,
        lessonId: String,
        type: QuestionType,
        rng: Rng,
        id: String,
        promptText: String?,
        promptClip: com.hanziflow.shared.Clip?,
        correctText: String,
        attr: (WordItem) -> String,
    ): GeneratedQuestion? {
        val distractors = tieredWordDistractors(word, rng).map(attr)
        val (texts, answerIndex) = buildOptionTexts(correctText, distractors, rng) ?: return null
        return GeneratedQuestion(
            id = id,
            type = type,
            itemId = word.id,
            lessonId = lessonId,
            promptText = promptText,
            promptClip = promptClip,
            options = texts.map { QuestionOption(it) },
            answerIndex = answerIndex,
        )
    }

    private fun buildListenWord(spec: Spec.ListenWord, rng: Rng, id: String): GeneratedQuestion? {
        val word = spec.word
        val fieldOrder = rng.shuffled(listOf(0, 1, 2))
        for (field in fieldOrder) {
            val pair: Pair<String, (WordItem) -> String> = when (field) {
                0 -> word.hanzi to { w: WordItem -> w.hanzi }
                1 -> word.pinyin to { w: WordItem -> w.pinyin }
                else -> word.meaningVi to { w: WordItem -> w.meaningVi }
            }
            val (correctText, attr) = pair
            val distractors = tieredWordDistractors(word, rng).map(attr)
            val built = buildOptionTexts(correctText, distractors, rng)
            if (built != null) {
                val (texts, answerIndex) = built
                return GeneratedQuestion(
                    id = id,
                    type = QuestionType.LISTEN_WORD,
                    itemId = word.id,
                    lessonId = spec.lessonId,
                    promptText = null,
                    promptClip = word.clip,
                    options = texts.map { QuestionOption(it) },
                    answerIndex = answerIndex,
                )
            }
        }
        return null
    }

    private fun buildTone(spec: Spec.Tone, rng: Rng, id: String): GeneratedQuestion? {
        val word = spec.word
        val correctTone = word.tones.single()
        val base = toneless(word.pinyin)
        val correctText = word.pinyin
        val otherTones = rng.shuffled((1..5).filter { it != correctTone })
        val texts = mutableListOf(correctText)
        for (tone in otherTones) {
            if (texts.size >= 4) break
            val candidate = applyTone(base, tone)
            if (candidate != correctText && candidate !in texts) texts.add(candidate)
        }
        if (texts.size < 3) return null
        val order = rng.shuffled(texts.indices.toList())
        val shuffledTexts = order.map { texts[it] }
        val answerIndex = order.indexOf(0)
        return GeneratedQuestion(
            id = id,
            type = QuestionType.TONE,
            itemId = word.id,
            lessonId = spec.lessonId,
            promptText = word.hanzi,
            promptClip = word.clip,
            options = shuffledTexts.map { QuestionOption(it) },
            answerIndex = answerIndex,
        )
    }

    private fun buildCloze(spec: Spec.Cloze, rng: Rng, id: String): GeneratedQuestion? {
        val sentence = spec.sentence
        val tokens = sentence.tokens.orEmpty()
        val blankItemId = tokens[spec.tokenIndex].itemId ?: return null
        val correctWord = wordById[blankItemId] ?: return null
        val promptText = tokens.mapIndexed { idx, token ->
            if (idx == spec.tokenIndex) "___" else token.hanzi
        }.joinToString("")
        val distractors = tieredWordDistractors(correctWord, rng).map { it.hanzi }
        val (texts, answerIndex) = buildOptionTexts(correctWord.hanzi, distractors, rng) ?: return null
        return GeneratedQuestion(
            id = id,
            type = QuestionType.CLOZE,
            itemId = blankItemId,
            lessonId = spec.lessonId,
            promptText = promptText,
            promptClip = sentence.clip,
            options = texts.map { QuestionOption(it) },
            answerIndex = answerIndex,
        )
    }

    private fun buildQa(spec: Spec.Qa, rng: Rng, id: String): GeneratedQuestion? {
        val qa = spec.qa
        val correctText = qa.answer.hanzi
        val otherAnswers = rng.shuffled(qaPool.filter { it.qa.id != qa.id }).map { it.qa.answer.hanzi }
        val (texts, answerIndex) = buildOptionTexts(correctText, otherAnswers, rng) ?: return null
        return GeneratedQuestion(
            id = id,
            type = QuestionType.QA,
            itemId = qa.id,
            lessonId = spec.lessonId,
            promptText = null,
            promptClip = qa.question.clip,
            options = texts.map { QuestionOption(it) },
            answerIndex = answerIndex,
        )
    }
}

internal fun buildContext(
    lessonItems: List<Items>,
    targetLessonId: String,
    seed: Int,
    excludedActivities: List<String>,
): GenerationContext {
    val wordPool = lessonItems.flatMap { items -> items.words.map { TaggedWord(it, items.lessonId) } }
    val sentencePool = lessonItems.flatMap { items ->
        items.sentences
            .filter { it.activity !in excludedActivities }
            .map { TaggedSentence(it, items.lessonId) }
    }
    val qaPool = lessonItems.flatMap { items ->
        items.qa
            .filter { it.activity !in excludedActivities }
            .map { TaggedQa(it, items.lessonId) }
    }
    return GenerationContext(seed, targetLessonId, wordPool, sentencePool, qaPool)
}
