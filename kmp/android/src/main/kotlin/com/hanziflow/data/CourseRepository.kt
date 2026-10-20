package com.hanziflow.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hanziflow.download.LessonDownloadWorker
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.CourseJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class CourseRepository(
    private val context: Context,
    private val database: CourseDatabase,
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val dao = database.dao()
    val lessons: Flow<List<LessonEntity>> = dao.observeLessons()

    suspend fun baseUrl(): String = dao.setting(BASE_URL) ?: "http://10.0.2.2:8000/"

    suspend fun setBaseUrl(value: String) {
        dao.putSetting(SettingEntity(BASE_URL, value.trim().trimEnd('/') + "/"))
    }

    suspend fun syncCatalog() = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(resolveUrl(baseUrl(), "catalog.json")).build()
        val json = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Catalog HTTP ${response.code}" }
            response.body.string()
        }
        val catalog = CourseJson.catalog(json)
        catalog.lessons.forEach { remote ->
            val previous = dao.lesson(remote.id)
            dao.putLesson(
                LessonEntity(
                    id = remote.id,
                    title = remote.title,
                    level = remote.level,
                    lessonOrder = remote.order,
                    remoteVersion = remote.version,
                    downloadedVersion = previous?.downloadedVersion,
                    downloadedCatalogJson = previous?.downloadedCatalogJson,
                    catalogJson = CourseJson.codec.encodeToString(CatalogLesson.serializer(), remote),
                    downloadState = when {
                        previous?.downloadState == "DOWNLOADING" -> "DOWNLOADING"
                        previous?.downloadedVersion == remote.version -> "DOWNLOADED"
                        previous?.downloadedVersion != null -> "UPDATE_AVAILABLE"
                        else -> "AVAILABLE"
                    },
                    error = if (previous?.downloadState == "DOWNLOADING") previous.error else null,
                )
            )
        }
    }

    fun download(id: String) {
        val request = OneTimeWorkRequestBuilder<LessonDownloadWorker>()
            .setInputData(workDataOf(LessonDownloadWorker.LESSON_ID to id))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(workName(id))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(id: String) = WorkManager.getInstance(context).cancelUniqueWork(workName(id))

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        cancel(id)
        context.filesDir.resolve("course/packages/$id").deleteRecursively()
        dao.markDeleted(id)
    }

    suspend fun trackFile(lessonId: String, track: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        val artifact = if (track == "practice") lesson.tracks.practice else lesson.tracks.guided
        return artifact?.let { packageFile(lessonId, version, it.file) }
    }

    suspend fun timelineFile(lessonId: String, track: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        val artifact = if (track == "practice") lesson.tracks.practice?.timeline else lesson.tracks.guided.timeline
        return artifact?.let { packageFile(lessonId, version, it.file) }
    }

    suspend fun referenceFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        return packageFile(lessonId, version, lesson.files.reference.file)
    }

    suspend fun itemsFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        return packageFile(lessonId, version, lesson.files.items.file)
    }

    private fun downloadedLesson(entity: LessonEntity): CatalogLesson =
        CourseJson.codec.decodeFromString(CatalogLesson.serializer(), entity.downloadedCatalogJson ?: entity.catalogJson)

    private fun packageFile(lessonId: String, version: Int, relative: String): File? =
        context.filesDir.resolve("course/packages/$lessonId/v$version/$relative").takeIf(File::isFile)

    suspend fun clipFile(lessonId: String, relative: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        return packageFile(lessonId, version, relative)
    }

    suspend fun catalogLesson(id: String): CatalogLesson? = dao.lesson(id)?.let {
        CourseJson.codec.decodeFromString(CatalogLesson.serializer(), it.catalogJson)
    }

    suspend fun transcriptView(track: String): String? = dao.setting(transcriptViewKey(track))
    suspend fun setTranscriptView(track: String, mode: String) =
        dao.putSetting(SettingEntity(transcriptViewKey(track), mode))

    suspend fun vietnameseEnabled(): Boolean = dao.setting(VI_ENABLED)?.toBooleanStrictOrNull() ?: true
    suspend fun setVietnameseEnabled(enabled: Boolean) = dao.putSetting(SettingEntity(VI_ENABLED, enabled.toString()))

    companion object {
        const val BASE_URL = "catalog_base_url"
        const val VI_ENABLED = "transcript_vi_enabled"
        fun transcriptViewKey(track: String) = "transcript_view_$track"
        fun workName(id: String) = "lesson-download-$id"
        fun resolveUrl(base: String, relative: String): String =
            java.net.URI(base.trimEnd('/') + "/").resolve(relative).toString()
    }
}
