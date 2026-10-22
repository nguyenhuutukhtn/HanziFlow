package com.hanziflow.shared.web

import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.fixtureText
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerJsTest {
    private val session = LessonSessionJs(
        catalogJson = fixtureText("catalog.json"),
        lessonId = "lesson_001",
        timelineJson = fixtureText("timeline_guided.json"),
        referenceJson = fixtureText("reference.json"),
        charactersJson = fixtureText("characters.json"),
    )

    @Test
    fun exposesCatalogPathsAndCueNavigation() {
        val lessons = CourseJson.codec.parseToJsonElement(CatalogJs.lessonsJson(fixtureText("catalog.json"))).jsonArray
        assertEquals(1, lessons.size)
        assertEquals("lesson_001", lessons.single().jsonObject.getValue("id").jsonPrimitive.content)
        assertEquals("Đồ ăn mang về của đồng nghiệp", session.lessonTitle())
        assertEquals("lessons/lesson_001/v7/guided.mp3", session.audioPath("guided"))
        assertEquals("lessons/lesson_001/v7/timeline_guided.json", session.timelinePath("guided"))
        assertEquals("lessons/lesson_001/v7/reference.json", session.referencePath())
        assertEquals("lessons/lesson_001/v7/characters.json", session.charactersPath())

        assertEquals(5, session.activeCueIndex(21500.0))
        assertEquals(-1, session.activeCueIndex(-1.0))
        assertEquals(21024.0, session.seekTargetMs("scene_bag_vi", 0.0))
        val cue = CourseJson.codec.parseToJsonElement(assertNotNull(session.cueJson(5))).jsonObject
        assertEquals("scene_bag_vi", cue.getValue("line_id").jsonPrimitive.content)
        assertEquals(21024L, cue.getValue("start_ms").jsonPrimitive.long)
        assertEquals(29902L, cue.getValue("end_ms").jsonPrimitive.long)
    }

    @Test
    fun cuePlaceJson_inGapAfterCue0End1840BeforeCue1Start2240() {
        assertEquals("{\"speaking\":-1,\"last\":0,\"next\":1}", session.cuePlaceJson(2000.0))
    }

    @Test
    fun exposesTrackScopedLinesAndTrackSwitching() {
        val reference = CourseJson.reference(fixtureText("reference.json"))
        val guidedIds = CourseJson.codec.parseToJsonElement(session.linesJson("guided")).jsonArray
            .map { it.jsonPrimitive.content }
        assertEquals(reference.tracks.guided.size, guidedIds.size)
        assertEquals(reference.tracks.guided.first().id, guidedIds.first())

        val practice = LessonSessionJs(
            catalogJson = fixtureText("catalog.json"),
            lessonId = "lesson_001",
            timelineJson = fixtureText("timeline_guided.json"),
            referenceJson = fixtureText("reference.json"),
            track = "practice",
        )
        assertEquals(reference.tracks.practice.orEmpty().map { it.id }, CourseJson.codec
            .parseToJsonElement(practice.linesJson("practice")).jsonArray.map { it.jsonPrimitive.content })
        assertEquals(
            21500.0,
            TrackSwitchJs.positionFor(
                fixtureText("timeline_guided.json"),
                fixtureText("timeline_guided.json"),
                21500.0,
            ),
        )
        val zhCues = CourseJson.timeline(fixtureText("timeline_guided_zh.json")).cues
        val mapped = TrackSwitchJs.positionFor(fixtureText("timeline_guided.json"), fixtureText("timeline_guided_zh.json"), 21500.0)
        assertTrue(zhCues.any { it.startMs.toDouble() == mapped }, "guided→guided_zh must land on a guided_zh cue start, got $mapped")
    }

    @Test
    fun exposesTranscriptRenderingAndCharacters() {
        val reference = CourseJson.reference(fixtureText("reference.json"))
        val line = reference.tracks.guided.first { it.language == "zh-CN" && !it.tokens.isNullOrEmpty() }
        val rendered = CourseJson.codec.parseToJsonElement(
            assertNotNull(session.lineJson(line.id, "HANZI_WITH_PINYIN", true, 0.0)),
        ).jsonObject
        val tokens = rendered.getValue("tokens").jsonArray
        assertEquals(line.text, tokens.joinToString("") { it.jsonObject.getValue("hanzi").jsonPrimitive.content })
        assertTrue(tokens.filter { it.jsonObject.getValue("hanzi").jsonPrimitive.content in "，。？！" }
            .all { !it.jsonObject.getValue("showPinyin").jsonPrimitive.boolean })

        val character = CourseJson.codec.parseToJsonElement(assertNotNull(session.characterJson("我"))).jsonObject
        assertEquals("ngã", character.getValue("han_viet").jsonPrimitive.content)
        assertEquals("tôi, mình", character.getValue("vi_meaning").jsonPrimitive.content)
        assertNull(session.characterJson("龘"))
        assertFalse(character.getValue("components").jsonArray.isEmpty())
    }

    @Test
    fun exposesLineFieldsForTheCurrentReferenceTrack() {
        val line = CourseJson.codec.parseToJsonElement(
            assertNotNull(session.lineJson("scene_bag_vi", "HANZI_WITH_PINYIN", true, 0.0)),
        ).jsonObject
        assertTrue(line.getValue("language").jsonPrimitive.content.startsWith("vi"))
        assertEquals(
            "Trên bàn có một túi đồ ăn mang về. Đây không phải bàn của Anna. Đây không phải bàn của Lý Minh. Anna và Lý Minh nhìn túi.",
            line.getValue("text").jsonPrimitive.content,
        )
    }
}
