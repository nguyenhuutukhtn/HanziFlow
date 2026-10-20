package com.hanziflow.shared.transcript

import com.hanziflow.shared.TimelineSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SectionNavigationTest {
    private val sections = listOf(
        TimelineSection("story", "Nghe chuyện", 0, 10_000),
        TimelineSection("qa", "Hỏi đáp", 10_000, 20_000),
        TimelineSection("retell", "Kể lại", 20_000, 30_000),
    )

    @Test
    fun sectionAt_findsTheContainingSection() {
        assertEquals("story", SectionNavigation.sectionAt(sections, 0)?.id)
        assertEquals("qa", SectionNavigation.sectionAt(sections, 15_000)?.id)
        assertEquals("retell", SectionNavigation.sectionAt(sections, 29_999)?.id)
        assertEquals("retell", SectionNavigation.sectionAt(sections, 30_000)?.id)
    }

    @Test
    fun nextSectionStart_isNullPastTheLastSection() {
        assertEquals(10_000L, SectionNavigation.nextSectionStart(sections, 0))
        assertEquals(20_000L, SectionNavigation.nextSectionStart(sections, 10_000))
        assertNull(SectionNavigation.nextSectionStart(sections, 20_000))
    }

    @Test
    fun sectionStart_looksUpById() {
        assertEquals(10_000L, SectionNavigation.sectionStart(sections, "qa"))
        assertNull(SectionNavigation.sectionStart(sections, "missing"))
    }

    @Test
    fun sectionLoop_repeatsFourTimesThenContinuesOnTheFifthPass() {
        var loop = SectionLoop("story", 0, 10_000, pass = 1, stopAfterPass = false)
        repeat(4) {
            val outcome = loop.onReachedEnd() as SectionLoop.Outcome.Repeat
            assertEquals(0L, outcome.seekToMs)
            loop = loop.copy(pass = loop.pass + 1)
        }
        assertEquals(5, loop.pass)
        assertEquals(SectionLoop.Outcome.Continue, loop.onReachedEnd())
    }

    @Test
    fun stopAfterPass_finishesTheCurrentPassThenContinues() {
        val loop = SectionLoop("story", 0, 10_000, pass = 2, stopAfterPass = true)
        assertEquals(SectionLoop.Outcome.Continue, loop.onReachedEnd())
    }

    @Test
    fun lastSection_alsoContinuesRatherThanRepeatingForever() {
        val loop = SectionLoop("retell", 20_000, 30_000, pass = 5, stopAfterPass = false)
        assertEquals(SectionLoop.Outcome.Continue, loop.onReachedEnd())
    }
}
