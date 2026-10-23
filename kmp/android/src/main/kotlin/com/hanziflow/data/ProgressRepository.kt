package com.hanziflow.data

import javax.inject.Inject

/** Local listening resumption. Sync and completion status remain D2 work. */
interface ProgressRepository {
    suspend fun positionMs(lessonId: String, track: String): Long
    suspend fun savePosition(lessonId: String, track: String, positionMs: Long)

    companion object {
        fun positionKey(lessonId: String, track: String) = "playback_position_${lessonId}_$track"
    }
}

class ProgressRepositoryImpl @Inject constructor(private val database: CourseDatabase) : ProgressRepository {
    private val dao = database.dao()

    override suspend fun positionMs(lessonId: String, track: String): Long =
        dao.setting(ProgressRepository.positionKey(lessonId, track))?.toLongOrNull() ?: 0L

    override suspend fun savePosition(lessonId: String, track: String, positionMs: Long) {
        dao.putSetting(SettingEntity(ProgressRepository.positionKey(lessonId, track), positionMs.coerceAtLeast(0).toString()))
    }
}
