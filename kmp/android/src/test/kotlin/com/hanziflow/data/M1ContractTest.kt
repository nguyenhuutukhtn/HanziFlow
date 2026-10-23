package com.hanziflow.data

import com.hanziflow.download.trackArtifacts
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.Clip
import com.hanziflow.shared.PackageFiles
import com.hanziflow.shared.Track
import com.hanziflow.shared.Tracks
import org.junit.Assert.assertEquals
import org.junit.Test

class M1ContractTest {
    private fun lesson(id: String, format: String = "linear_v1") = CatalogLesson(
        id, 1, 1, id, "beginner", "", format,
        Tracks(
            mapOf(
                "guided" to Track("guided.mp3", 1, 1, "a", Clip("timeline_guided.json", 1, "a")),
                "guided_zh" to Track("guided_zh.mp3", 1, 1, "a", Clip("timeline_guided_zh.json", 1, "a")),
                "practice" to Track("practice.mp3", 1, 1, "a", Clip("timeline_practice.json", 1, "a")),
            ),
        ),
        PackageFiles(Clip("lesson.json", 1, "a"), Clip("reference.json", 1, "a"), Clip("items.json", 1, "a")),
    )

    @Test
    fun library_hides_modes_lessons_and_downloads_all_linear_track_files() {
        val linear = lesson("lesson_001")
        assertEquals(listOf(linear), androidCatalogLessons(listOf(linear, lesson("lesson_002", "modes_v1"))))
        assertEquals(
            listOf("guided.mp3", "timeline_guided.json", "guided_zh.mp3", "timeline_guided_zh.json", "practice.mp3", "timeline_practice.json"),
            trackArtifacts(linear).map { it.file },
        )
    }
}
