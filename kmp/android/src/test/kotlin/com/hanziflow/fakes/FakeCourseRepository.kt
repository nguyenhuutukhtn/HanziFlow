package com.hanziflow.fakes

import com.hanziflow.data.CourseRepository
import com.hanziflow.data.LessonEntity
import com.hanziflow.shared.CatalogLesson
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CourseRepository] fake: no Room/OkHttp/WorkManager, just recorded calls and canned returns. */
class FakeCourseRepository : CourseRepository {
    val lessonsFlow = MutableStateFlow<List<LessonEntity>>(emptyList())
    override val lessons = lessonsFlow

    var syncCatalogError: Throwable? = null
    var syncCatalogCalls = 0
    val downloadCalls = mutableListOf<String>()
    val cancelCalls = mutableListOf<String>()
    val deleteCalls = mutableListOf<String>()

    var trackFiles: Map<Pair<String, String>, File> = emptyMap()
    var timelineFiles: Map<Pair<String, String>, File> = emptyMap()
    var referenceFiles: Map<String, File> = emptyMap()
    var itemsFiles: Map<String, File> = emptyMap()
    var charactersFiles: Map<String, File> = emptyMap()
    var strokePack: File? = null
    var clipFiles: Map<Pair<String, String>, File> = emptyMap()
    var catalogLessons: Map<String, CatalogLesson> = emptyMap()

    override suspend fun syncCatalog() {
        syncCatalogCalls++
        syncCatalogError?.let { throw it }
    }

    override fun download(id: String) { downloadCalls += id }
    override fun cancel(id: String) { cancelCalls += id }
    override suspend fun delete(id: String) { deleteCalls += id }

    override suspend fun trackFile(lessonId: String, track: String): File? = trackFiles[lessonId to track]
    override suspend fun timelineFile(lessonId: String, track: String): File? = timelineFiles[lessonId to track]
    override suspend fun referenceFile(lessonId: String): File? = referenceFiles[lessonId]
    override suspend fun itemsFile(lessonId: String): File? = itemsFiles[lessonId]
    override suspend fun charactersFile(lessonId: String): File? = charactersFiles[lessonId]
    override suspend fun strokePackFile(): File? = strokePack
    override suspend fun clipFile(lessonId: String, relative: String): File? = clipFiles[lessonId to relative]
    override suspend fun catalogLesson(id: String): CatalogLesson? = catalogLessons[id]
}
