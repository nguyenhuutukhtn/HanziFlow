package com.hanziflow.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseRepositoryTest {
    @Test
    fun resolvesRelativeCatalogPaths() {
        assertEquals(
            "http://192.168.1.2:8000/lessons/lesson_001/v1/guided.mp3",
            CourseRepository.resolveUrl(
                "http://192.168.1.2:8000/",
                "lessons/lesson_001/v1/guided.mp3",
            ),
        )
    }

    @Test
    fun addsCacheBusterOnlyToCatalogUrl() {
        assertEquals(
            "http://192.168.1.2:8000/catalog.json?t=1234",
            CourseRepository.catalogUrl("http://192.168.1.2:8000/", 1234),
        )
    }
}
