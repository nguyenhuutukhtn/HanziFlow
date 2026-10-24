package com.hanziflow.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractParsingTest {
    @Test
    fun parsesPublishedCatalogAndManifest() {
        val catalog = CourseJson.catalog(fixtureText("catalog.json"))
        val manifest = CourseJson.lesson(fixtureText("lesson.json"))
        val lesson = catalog.lessons.single()

        assertEquals(1, catalog.schemaVersion)
        assertNull(catalog.generatedAt)
        assertEquals(2, assertNotNull(catalog.strokePack).version)
        assertEquals(300852L, catalog.strokePack.files.getValue("strokes.json").bytes)
        assertEquals("lesson_001", lesson.id)
        assertNull(lesson.tracks.practice)
        assertEquals("timeline_guided.json", assertNotNull(lesson.tracks.guided).timeline.file)
        assertEquals("timeline_guided_zh.json", lesson.tracks.forName("guided_zh")?.timeline?.file)
        assertNull(lesson.tracks.forName("unknown"))
        assertEquals(lesson.id, manifest.id)
        assertEquals(7, manifest.newPatterns.size)
    }

    @Test
    fun parsesModesV1CatalogTracks() {
        val catalog = CourseJson.catalog(
            """{"schema_version":1,"catalog_version":1,"lessons":[
              {"id":"lesson_001","version":1,"order":1,"title":"Linear","level":"beginner","base_url":"linear/","tracks":{"guided":{"file":"guided.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_guided.json","bytes":1,"sha256":"a"}}},"files":{"manifest":{"file":"lesson.json","bytes":1,"sha256":"a"},"reference":{"file":"reference.json","bytes":1,"sha256":"a"},"items":{"file":"items.json","bytes":1,"sha256":"a"}}},
              {"id":"lesson_002","version":1,"order":2,"title":"Modes","level":"beginner","base_url":"modes/","format":"modes_v1","tracks":{"hieu":{"file":"hieu.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_hieu.json","bytes":1,"sha256":"a"}},"nghe":{"file":"nghe.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_nghe.json","bytes":1,"sha256":"a"}},"phan_xa":{"file":"phan_xa.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_phan_xa.json","bytes":1,"sha256":"a"}},"noi":{"file":"noi.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_noi.json","bytes":1,"sha256":"a"}}},"files":{"manifest":{"file":"lesson.json","bytes":1,"sha256":"a"},"reference":{"file":"reference.json","bytes":1,"sha256":"a"},"items":{"file":"items.json","bytes":1,"sha256":"a"}}}
            ]}""".trimIndent(),
        )
        val modes = catalog.lessons[1]
        assertEquals("modes_v1", modes.format)
        assertEquals(listOf("hieu", "nghe", "phan_xa", "noi"), modes.tracks.names)
        assertNull(modes.tracks.guided)
        assertEquals("timeline_phan_xa.json", modes.tracks.forName("phan_xa")?.timeline?.file)
    }

    @Test
    fun ordersFiveModesWithTuVungSecond() {
        fun track(name: String) = """"$name":{"file":"$name.mp3","bytes":1,"duration_ms":1,"sha256":"a","timeline":{"file":"timeline_$name.json","bytes":1,"sha256":"a"}}"""
        val tracks = listOf("noi", "tu_vung", "phan_xa", "nghe", "hieu").joinToString(",") { track(it) }
        val catalog = CourseJson.catalog(
            """{"schema_version":1,"catalog_version":1,"lessons":[
              {"id":"lesson_002","version":1,"order":2,"title":"Modes","level":"beginner","base_url":"modes/","format":"modes_v1","tracks":{$tracks},"files":{"manifest":{"file":"lesson.json","bytes":1,"sha256":"a"},"reference":{"file":"reference.json","bytes":1,"sha256":"a"},"items":{"file":"items.json","bytes":1,"sha256":"a"}}}
            ]}""".trimIndent(),
        )
        assertEquals(listOf("hieu", "tu_vung", "nghe", "phan_xa", "noi"), catalog.lessons[0].tracks.names)
    }

    @Test
    fun parsesRealNotesAndModesWordsForBothLessons() {
        val catalog = CourseJson.catalog(fixtureText("catalog_modes.json"))
        val linear = CourseJson.notes(fixtureText("notes.json"))
        val modes = CourseJson.notes(fixtureText("notes_modes.json"))
        val modesItems = CourseJson.items(fixtureText("items_modes.json"))

        assertEquals("lesson_001", linear.lessonId)
        assertEquals(7, linear.patterns.size)
        assertEquals("lesson_002", modes.lessonId)
        assertEquals(3, modes.patterns.size)
        for (notes in listOf(linear, modes)) {
            assertTrue(notes.questions.isNotEmpty())
            assertTrue(notes.patterns.all { it.explainVi.isNotBlank() && it.examples.isNotEmpty() })
            assertTrue(notes.questions.all { it.howToAnswerVi.isNotBlank() && it.examples.isNotEmpty() })
        }
        assertEquals(listOf("notes.json", "notes.json"), catalog.lessons.map { assertNotNull(it.files.notes).file })
        assertEquals(88, modesItems.words.size)
        assertTrue(modesItems.words.all { it.id.isNotBlank() && it.hanzi.isNotBlank() && it.tones.isNotEmpty() && it.meaningVi.isNotBlank() })
        // Inline catalogs in the tests above carry no notes entry and still parse (notes is optional).
    }

    @Test
    fun parsesRealModesV1Package() {
        val lesson = CourseJson.catalog(fixtureText("catalog_modes.json")).lessons.single { it.id == "lesson_002" }
        val reference = CourseJson.reference(fixtureText("reference_modes.json"))
        val timeline = CourseJson.timeline(fixtureText("timeline_noi.json"))
        val noi = assertNotNull(reference.tracks.forName("noi"))

        assertEquals("modes_v1", lesson.format)
        assertEquals(listOf("hieu", "tu_vung", "nghe", "phan_xa", "noi"), lesson.tracks.names)
        assertEquals(43, noi.size)
        assertEquals(121, assertNotNull(reference.tracks.forName("phan_xa")).size)
        assertTrue(timeline.cues.all { cue -> cue.lineId in noi.map { it.id } })
        assertEquals(42, timeline.gaps.size)
        assertTrue(timeline.gaps.all { it.kind == "repeat" })
    }

    @Test
    fun parsesPublishedReferenceItemsAndTimeline() {
        val reference = CourseJson.reference(fixtureText("reference.json"))
        val items = CourseJson.items(fixtureText("items.json"))
        val timeline = CourseJson.timeline(fixtureText("timeline_guided.json"))
        val guided = assertNotNull(reference.tracks.guided)
        val guidedIds = guided.mapTo(mutableSetOf()) { it.id }

        assertEquals(797, guided.size)
        assertEquals(797, timeline.cues.size)
        assertTrue(timeline.cues.all { cue -> cue.lineId in guidedIds })
        assertEquals(40, assertNotNull(reference.tracks.practice).size)
        assertTrue(guided.flatMap { it.tokens.orEmpty() }.any { it.pinyin == null })
        assertTrue(items.words.any { it.clip == null })
        assertTrue(items.sentences.all { it.activity.isNotBlank() && it.speaker.isNotBlank() })
        assertTrue(items.qa.all { it.activity.isNotBlank() && it.speaker.isNotBlank() })
    }

    @Test
    fun timelineSectionsCoverTheTrackAndEveryCueHasOne() {
        val timeline = CourseJson.timeline(fixtureText("timeline_guided.json"))
        val sections = timeline.sections
        val sectionIds = sections.mapTo(mutableSetOf()) { it.id }

        assertTrue(sections.isNotEmpty())
        assertEquals(0L, sections.first().startMs)
        assertEquals(timeline.durationMs, sections.last().endMs)
        sections.zipWithNext { earlier, later -> assertEquals(earlier.endMs, later.startMs) }
        assertTrue(sections.all { it.title.isNotBlank() })
        assertTrue(timeline.cues.all { cue -> cue.sectionId != null && cue.sectionId in sectionIds })
        assertTrue(timeline.cues.all { cue -> cue.language != null && cue.type != null })
    }

    @Test
    fun parsesChineseOnlyTimelineWithoutVietnameseCues() {
        val timeline = CourseJson.timeline(fixtureText("timeline_guided_zh.json"))
        assertEquals("guided_zh", timeline.track)
        assertTrue(timeline.cues.none { it.language == "vi-VN" })
        assertEquals(8, timeline.sections.size)
        assertEquals(0L, timeline.sections.first().startMs)
        assertEquals(timeline.durationMs, timeline.sections.last().endMs)
        timeline.sections.zipWithNext { earlier, later -> assertEquals(earlier.endMs, later.startMs) }
    }

    @Test
    fun parsesModeAnswerGapsAndDefaultsMissingGapsToEmpty() {
        val timeline = CourseJson.timeline(fixtureText("timeline_phan_xa.json"))
        assertEquals(40, timeline.gaps.size)
        assertTrue(timeline.gaps.all { it.kind == "answer" })
        assertEquals(TimelineGap(3874, 7294, "answer", "phan_xa_q2"), timeline.gaps.first())

        val oldTimeline = CourseJson.timeline(
            """{"schema_version":1,"lesson_id":"lesson_001","lesson_version":1,"track":"guided","duration_ms":1,"cues":[]}""",
        )
        assertEquals(emptyList(), oldTimeline.gaps)
    }
}

fun fixtureText(relativePath: String): String = FIXTURE_JSON.getValue(relativePath)
