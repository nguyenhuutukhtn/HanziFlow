package com.hanziflow.playback

import android.content.Context
import androidx.media3.common.MediaItem
import com.hanziflow.data.CourseDatabase
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.PlaybackEntity
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads/saves the last played lesson+track+position (local only), so playback survives an app restart. */
class ResumptionStore(
    private val context: Context,
    private val courseRepository: CourseRepository,
    private val courseDatabase: CourseDatabase,
) {
    data class Resumption(val saved: PlaybackEntity, val file: File, val title: String, val trackLabel: String) {
        fun mediaItem(): MediaItem = PlaybackContract.mediaItem(
            lessonId = saved.lessonId,
            track = saved.track,
            uri = file.toURI().toString(),
            title = title,
            trackLabel = trackLabel,
        )
    }

    suspend fun load(): Resumption? = withContext(Dispatchers.IO) {
        val saved = courseDatabase.dao().playback() ?: return@withContext null
        val file = courseRepository.trackFile(saved.lessonId, saved.track) ?: return@withContext null
        val title = courseDatabase.dao().lesson(saved.lessonId)?.title ?: saved.lessonId
        Resumption(saved, file, title, context.getString(PlaybackContract.trackLabelRes(saved.track)))
    }

    suspend fun persist(entity: PlaybackEntity) = withContext(Dispatchers.IO) {
        courseDatabase.dao().putPlayback(entity)
    }
}
