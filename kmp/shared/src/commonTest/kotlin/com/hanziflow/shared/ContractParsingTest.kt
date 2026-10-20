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
        assertEquals("lesson_001", lesson.id)
        assertNull(lesson.tracks.practice)
        assertEquals("timeline_guided.json", lesson.tracks.guided.timeline.file)
        assertEquals(lesson.id, manifest.id)
        assertEquals(7, manifest.newPatterns.size)
    }

    @Test
    fun parsesPublishedReferenceItemsAndTimeline() {
        val reference = CourseJson.reference(fixtureText("reference.json"))
        val items = CourseJson.items(fixtureText("items.json"))
        val timeline = CourseJson.timeline(fixtureText("timeline_guided.json"))
        val guidedIds = reference.tracks.guided.mapTo(mutableSetOf()) { it.id }

        assertEquals(797, reference.tracks.guided.size)
        assertEquals(797, timeline.cues.size)
        assertTrue(timeline.cues.all { cue -> cue.lineId in guidedIds })
        assertEquals(40, assertNotNull(reference.tracks.practice).size)
        assertTrue(reference.tracks.guided.flatMap { it.tokens.orEmpty() }.any { it.pinyin == null })
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
}

fun fixtureText(relativePath: String): String = FIXTURE_JSON.getValue(relativePath)

