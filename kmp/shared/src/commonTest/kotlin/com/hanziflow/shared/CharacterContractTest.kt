package com.hanziflow.shared

import com.hanziflow.shared.characters.CharacterIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CharacterContractTest {
    @Test
    fun strokePackParsesFlatContractPayload() {
        val pack = CourseJson.strokePack(fixtureText("strokes.json"))
        assertEquals(setOf("我", "们", "扌"), pack.keys)
        assertEquals(1, pack["我"]!!.strokes.size)
        assertEquals(1, pack["们"]!!.medians.size)
        assertTrue(pack["扌"]!!.strokes.single().startsWith("M"))
    }

    @Test
    fun realLessonCharacterIndexMatchesContract() {
        val data = CourseJson.characters(fixtureText("characters.json"))
        val index = CharacterIndex(data)
        val wo = assertNotNull(index.entry("我"))
        assertEquals("wǒ", wo.pinyin); assertEquals("ngã", wo.hanViet); assertEquals("tôi, mình", wo.viMeaning)
        assertEquals(1, wo.hskLevel); assertEquals(listOf("扌", "戈"), index.componentsOf("我").map { it.char })
        assertEquals(94, data.componentsPrereq.size); assertEquals("thủ đứng", index.component("扌")?.nameVi); assertEquals("qua", index.component("戈")?.nameVi)
        assertEquals(listOf("我", "们"), index.charactersOf("我们").map { it.char }); assertFalse(index.entry("们")!!.phoneticFamily.isEmpty())
        listOf(0, 19, 39, 59, data.characters.lastIndex).forEach { i ->
            val entry = data.characters[i]
            println("sample ${entry.char}: ${entry.hanViet} — ${entry.viMeaning}")
            assertFalse(entry.hanViet.isBlank()); assertFalse(entry.viMeaning.isBlank())
        }
    }
}
