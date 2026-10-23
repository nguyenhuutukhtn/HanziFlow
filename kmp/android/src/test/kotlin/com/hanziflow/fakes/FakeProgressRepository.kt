package com.hanziflow.fakes

import com.hanziflow.data.ProgressRepository

class FakeProgressRepository : ProgressRepository {
    private val positions = mutableMapOf<Pair<String, String>, Long>()

    override suspend fun positionMs(lessonId: String, track: String): Long = positions[lessonId to track] ?: 0L

    override suspend fun savePosition(lessonId: String, track: String, positionMs: Long) {
        positions[lessonId to track] = positionMs
    }

    fun stored(lessonId: String, track: String): Long = positions[lessonId to track] ?: 0L
}
