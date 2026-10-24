package com.hanziflow.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hanziflow.download.LessonDownloadWorker
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.CourseJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

internal fun androidCatalogLessons(lessons: List<CatalogLesson>): List<CatalogLesson> = lessons

class CourseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CourseDatabase,
    private val settingsRepository: SettingsRepository,
    private val client: OkHttpClient,
) : CourseRepository {
    private val dao = database.dao()
    private var strokeVersion: Int? = null
    override val lessons: Flow<List<LessonEntity>> = dao.observeLessons()

    override suspend fun syncCatalog() = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(CourseRepository.catalogUrl(settingsRepository.baseUrl(), System.currentTimeMillis())).build()
        val json = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Catalog HTTP ${response.code}" }
            response.body.string()
        }
        val catalog = CourseJson.catalog(json)
        androidCatalogLessons(catalog.lessons).forEach { remote ->
            val previous = dao.lesson(remote.id)
            // Same version but a different track set (package downloaded by an older app
            // build that didn't know a track) needs a re-download, not "DOWNLOADED".
            val downloadedCatalog = previous?.downloadedCatalogJson
                ?.let { CourseJson.codec.decodeFromString(CatalogLesson.serializer(), it) }
            val downloadedTracks = downloadedCatalog?.tracks
            val downloadedFiles = downloadedCatalog?.files
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
                        previous?.downloadedVersion == remote.version && downloadedTracks == remote.tracks && downloadedFiles == remote.files -> "DOWNLOADED"
                        previous?.downloadedVersion != null -> "UPDATE_AVAILABLE"
                        else -> "AVAILABLE"
                    },
                    error = if (previous?.downloadState == "DOWNLOADING") previous.error else null,
                )
            )
        }
    }

    override fun download(id: String) {
        val request = OneTimeWorkRequestBuilder<LessonDownloadWorker>()
            .setInputData(workDataOf(LessonDownloadWorker.LESSON_ID to id))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(CourseRepository.workName(id))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(CourseRepository.workName(id), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(CourseRepository.workName(id))
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        cancel(id)
        context.filesDir.resolve("course/packages/$id").deleteRecursively()
        dao.markDeleted(id)
    }

    override suspend fun trackFile(lessonId: String, track: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        val artifact = lesson.tracks.forName(track)
        return artifact?.let { packageFile(lessonId, version, it.file) }
    }

    override suspend fun timelineFile(lessonId: String, track: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        val artifact = lesson.tracks.forName(track)?.timeline
        return artifact?.let { packageFile(lessonId, version, it.file) }
    }

    override suspend fun referenceFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        return packageFile(lessonId, version, lesson.files.reference.file)
    }

    override suspend fun itemsFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val lesson = downloadedLesson(entity)
        return packageFile(lessonId, version, lesson.files.items.file)
    }

    override suspend fun charactersFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val file = downloadedLesson(entity).files.characters ?: return null
        return packageFile(lessonId, version, file.file)
    }

    override suspend fun notesFile(lessonId: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        val file = downloadedLesson(entity).files.notes ?: return null
        return packageFile(lessonId, version, file.file)
    }

    override suspend fun strokePackFile(): File? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(CourseRepository.catalogUrl(settingsRepository.baseUrl(), System.currentTimeMillis())).build()
        val catalog = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Catalog HTTP ${response.code}" }
            CourseJson.catalog(response.body.string())
        }
        val ref = catalog.strokePack ?: return@withContext null
        val metadata = ref.files["strokes.json"] ?: return@withContext null
        val destination = context.filesDir.resolve("course/stroke-pack/v${ref.version}/strokes.json")
        if (strokeVersion == ref.version && destination.isFile && verified(destination, metadata.sha256, metadata.bytes)) return@withContext destination
        if (destination.isFile && verified(destination, metadata.sha256, metadata.bytes)) {
            strokeVersion = ref.version
            return@withContext destination
        }
        destination.parentFile?.mkdirs()
        val url = CourseRepository.resolveUrl(settingsRepository.baseUrl(), "stroke-pack/v${ref.version}/strokes.json")
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            check(response.isSuccessful) { "strokes.json: HTTP ${response.code}" }
            FileOutputStream(destination).use { response.body.byteStream().copyTo(it) }
        }
        check(verified(destination, metadata.sha256, metadata.bytes)) { "strokes.json: SHA-256 mismatch" }
        strokeVersion = ref.version
        destination
    }

    override suspend fun clipFile(lessonId: String, relative: String): File? {
        val entity = dao.lesson(lessonId) ?: return null
        val version = entity.downloadedVersion ?: return null
        return packageFile(lessonId, version, relative)
    }

    override suspend fun catalogLesson(id: String): CatalogLesson? = dao.lesson(id)?.let {
        CourseJson.codec.decodeFromString(CatalogLesson.serializer(), it.catalogJson)
    }

    private fun downloadedLesson(entity: LessonEntity): CatalogLesson =
        CourseJson.codec.decodeFromString(CatalogLesson.serializer(), entity.downloadedCatalogJson ?: entity.catalogJson)

    private fun packageFile(lessonId: String, version: Int, relative: String): File? =
        context.filesDir.resolve("course/packages/$lessonId/v$version/$relative").takeIf(File::isFile)

    private fun verified(file: File, sha256: String, bytes: Long): Boolean {
        if (file.length() != bytes) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.equals(sha256, true)
    }
}
