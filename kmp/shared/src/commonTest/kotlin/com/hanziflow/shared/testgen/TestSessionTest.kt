package com.hanziflow.shared.testgen

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.fixtureText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestSessionTest {
    @Test
    fun correctPathPasses() {
        val items = listOf(CourseJson.items(fixtureText("items.json")))
        val session = TestSession.create(items, "lesson_001", seed = 1, questionCount = 12)

        var guard = 0
        while (!session.isFinished && guard < 1000) {
            val q = session.current()!!
            val correct = session.answer(q.answerIndex)
            assertTrue(correct)
            guard++
        }

        assertTrue(session.isFinished)
        assertTrue(session.isPassed)
        assertEquals(0, session.mistakeCount)
        val summary = session.summary()
        assertEquals("lesson_001", summary.lessonId)
        assertEquals(TestGenerator.GENERATOR_VERSION, summary.generatorVersion)
        assertEquals(1, summary.seed)
        assertEquals(12, summary.questionCount)
        assertEquals(0, summary.mistakes)
        assertTrue(summary.passed)
    }

    @Test
    fun oneMistakeRequeuesADifferentQuestionAndStillPasses() {
        val items = listOf(CourseJson.items(fixtureText("items.json")))
        val session = TestSession.create(items, "lesson_001", seed = 7, questionCount = 12)

        val first = session.current()!!
        val wrongChoice = (first.answerIndex + 1) % first.options.size
        val wasCorrect = session.answer(wrongChoice)
        assertFalse(wasCorrect)
        assertEquals(1, session.mistakeCount)

        // The missed item must reappear later in the queue as a different question
        // of the same type on the same item (different options/order/prompt).
        var seenRequeued = false
        var guard = 0
        while (!session.isFinished && guard < 1000) {
            val q = session.current()!!
            if (q.itemId == first.itemId && q.type == first.type) {
                seenRequeued = true
                assertFalse(
                    q.promptText == first.promptText &&
                        q.options == first.options &&
                        q.answerIndex == first.answerIndex,
                    "requeued question for ${q.itemId} was byte-identical to the missed one",
                )
            }
            session.answer(q.answerIndex)
            guard++
        }

        assertTrue(seenRequeued, "the missed item never came back on the queue")
        assertTrue(session.isFinished)
        assertTrue(session.isPassed)
        assertEquals(1, session.mistakeCount)
    }

    @Test
    fun revealPreventsPassing() {
        val items = listOf(CourseJson.items(fixtureText("items.json")))
        val session = TestSession.create(items, "lesson_001", seed = 3, questionCount = 12)

        session.reveal()
        var guard = 0
        while (!session.isFinished && guard < 1000) {
            val q = session.current()!!
            session.answer(q.answerIndex)
            guard++
        }

        assertTrue(session.isFinished)
        assertFalse(session.isPassed)
        assertFalse(session.summary().passed)
    }
}
