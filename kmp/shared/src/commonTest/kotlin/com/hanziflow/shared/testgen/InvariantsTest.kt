package com.hanziflow.shared.testgen

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.Items
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Splits the real lesson_001 item bank into a synthetic "earlier" lesson and a
 * synthetic "target" lesson so the >=50%-current-lesson and no-later-lesson-leak
 * invariants are actually exercised (lesson_001 has no real earlier lesson yet).
 */
private fun splitFixtureIntoTwoLessons(): Pair<Items, Items> {
    val items = CourseJson.items(fixtureText("items.json"))
    val earlierWords = items.words.filterIndexed { i, _ -> i % 2 == 0 }
    val targetWords = items.words.filterIndexed { i, _ -> i % 2 == 1 }
    val earlierSentences = items.sentences.filterIndexed { i, _ -> i % 2 == 0 }
    val targetSentences = items.sentences.filterIndexed { i, _ -> i % 2 == 1 }
    val earlierQa = items.qa.filterIndexed { i, _ -> i % 2 == 0 }
    val targetQa = items.qa.filterIndexed { i, _ -> i % 2 == 1 }

    val earlier = items.copy(
        lessonId = "lesson_000",
        lessonVersion = 1,
        words = earlierWords,
        sentences = earlierSentences,
        qa = earlierQa,
    )
    val target = items.copy(
        lessonId = "lesson_001",
        lessonVersion = 1,
        words = targetWords,
        sentences = targetSentences,
        qa = targetQa,
    )
    return earlier to target
}

class InvariantsTest {
    @Test
    fun invariantsHoldAcross200Seeds() {
        val (earlier, target) = splitFixtureIntoTwoLessons()
        val earlierWordIds = earlier.words.map { it.id }.toSet()
        val earlierSentenceIds = earlier.sentences.map { it.id }.toSet()
        val earlierQaIds = earlier.qa.map { it.id }.toSet()
        val targetWordIds = target.words.map { it.id }.toSet()
        val targetSentenceIds = target.sentences.map { it.id }.toSet()
        val targetQaIds = target.qa.map { it.id }.toSet()

        val knownWordIds = earlierWordIds + targetWordIds
        val knownSentenceIds = earlierSentenceIds + targetSentenceIds
        val knownQaIds = earlierQaIds + targetQaIds

        for (seed in 1..200) {
            val test = TestGenerator.generate(listOf(earlier, target), "lesson_001", seed, 12)
            assertTrue(test.questions.isNotEmpty(), "seed $seed produced no questions")

            var currentCount = 0
            for (q in test.questions) {
                // Options are unique and never equal to a non-answer duplicate; answer index in range.
                val texts = q.options.map { it.text }
                assertTrue(texts.size == texts.distinct().size, "seed $seed question ${q.id} has duplicate options: $texts")
                assertTrue(q.answerIndex in texts.indices, "seed $seed question ${q.id} answer index out of range")
                assertTrue(texts.size in 3..4, "seed $seed question ${q.id} has ${texts.size} options")

                if (q.lessonId == "lesson_001") currentCount++

                // Never surface a word/sentence/qa pair this lesson bank doesn't know about
                // (defends against accidentally leaking an id outside the provided pools).
                when (q.type) {
                    QuestionType.CLOZE -> assertTrue(
                        q.itemId in knownWordIds,
                        "seed $seed cloze answer item ${q.itemId} not a taught word",
                    )
                    QuestionType.QA -> assertTrue(
                        q.itemId in knownQaIds,
                        "seed $seed qa item ${q.itemId} not a known qa pair",
                    )
                    else -> assertTrue(
                        q.itemId in knownWordIds,
                        "seed $seed word question item ${q.itemId} not a taught word",
                    )
                }

                // Every question answerable from the data present: audio-dependent types
                // must carry a clip.
                when (q.type) {
                    QuestionType.LISTEN_WORD, QuestionType.QA -> assertTrue(
                        q.promptClip != null,
                        "seed $seed question ${q.id} of type ${q.type} has no clip",
                    )
                    QuestionType.CLOZE -> assertTrue(
                        q.promptClip != null,
                        "seed $seed cloze question ${q.id} has no sentence clip",
                    )
                    else -> Unit
                }
            }

            val minCurrent = (test.questions.size + 1) / 2
            assertTrue(
                currentCount >= minCurrent,
                "seed $seed only $currentCount/${test.questions.size} questions target the current lesson",
            )
        }
    }
}
