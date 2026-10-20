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
}

