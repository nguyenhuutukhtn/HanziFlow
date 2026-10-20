package com.hanziflow.data

import com.hanziflow.shared.CatalogLesson
import java.io.File
import kotlinx.coroutines.flow.Flow

/** Course catalog, downloads, and the downloaded lesson package file store. */
interface CourseRepository {
    val lessons: Flow<List<LessonEntity>>

    suspend fun syncCatalog()
    fun download(id: String)
    fun cancel(id: String)
    suspend fun delete(id: String)

    suspend fun trackFile(lessonId: String, track: String): File?
    suspend fun timelineFile(lessonId: String, track: String): File?
    suspend fun referenceFile(lessonId: String): File?
    suspend fun itemsFile(lessonId: String): File?
    suspend fun clipFile(lessonId: String, relative: String): File?
    suspend fun catalogLesson(id: String): CatalogLesson?

    companion object {
        fun workName(id: String) = "lesson-download-$id"
        fun resolveUrl(base: String, relative: String): String =
            java.net.URI(base.trimEnd('/') + "/").resolve(relative).toString()
    }
}
