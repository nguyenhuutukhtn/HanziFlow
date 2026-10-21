package com.hanziflow.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hanziflow.data.CourseDao
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.SettingsRepository
import com.hanziflow.shared.CatalogLesson
import com.hanziflow.shared.Clip
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.Track
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class LessonDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val dao: CourseDao,
    private val settingsRepository: SettingsRepository,
    private val client: OkHttpClient,
) : CoroutineWorker(context, parameters) {
    private var completed = false
    private var lessonId = ""

    override suspend fun doWork(): Result {
        lessonId = inputData.getString(LESSON_ID) ?: return Result.failure()
        val stage = applicationContext.filesDir.resolve("course/staging/$lessonId-${id}.part")
        return try {
            dao.setDownloadState(lessonId, "DOWNLOADING")
            val entity = dao.lesson(lessonId) ?: error("Lesson is not in the catalog")
            val lesson = CourseJson.codec.decodeFromString(CatalogLesson.serializer(), entity.catalogJson)
            val rootUrl = CourseRepository.resolveUrl(settingsRepository.baseUrl(), lesson.baseUrl)
            stage.deleteRecursively()
            stage.mkdirs()

            val initial = buildList {
                add(lesson.tracks.guided.asClip())
                add(lesson.tracks.guided.timeline)
                lesson.tracks.guidedZh?.let {
                    add(it.asClip())
                    add(it.timeline)
                }
                lesson.tracks.practice?.let {
                    add(it.asClip())
                    add(it.timeline)
                }
                add(lesson.files.manifest)
                add(lesson.files.reference)
                add(lesson.files.items)
                lesson.files.characters?.let(::add)
            }
            initial.forEachIndexed { index, artifact ->
                download(rootUrl, artifact, stage)
                setProgress(androidx.work.workDataOf("done" to index + 1, "total" to initial.size))
            }

            val itemsFile = checkedDestination(stage, lesson.files.items.file + ".part")
            val items = CourseJson.items(itemsFile.readText())
            items.clips().forEach { download(rootUrl, it, stage) }

            stage.walkTopDown().filter { it.isFile && it.name.endsWith(".part") }.toList().forEach { part ->
                Files.move(part.toPath(), part.resolveSibling(part.name.removeSuffix(".part")).toPath())
            }
            val finalDir = applicationContext.filesDir.resolve("course/packages/${lesson.id}/v${lesson.version}")
            finalDir.parentFile?.mkdirs()
            if (finalDir.exists()) finalDir.deleteRecursively()
            try {
                Files.move(stage.toPath(), finalDir.toPath(), StandardCopyOption.ATOMIC_MOVE)
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                check(stage.renameTo(finalDir)) { "Could not promote verified lesson package" }
            }
            dao.markDownloaded(lesson.id, lesson.version, entity.catalogJson)
            finalDir.parentFile?.listFiles()
                ?.filter { it.isDirectory && it != finalDir }
                ?.forEach(File::deleteRecursively)
            completed = true
            Result.success()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            dao.setDownloadState(lessonId, "FAILED", error.message ?: error.javaClass.simpleName)
            Result.failure()
        } finally {
            if (!completed) withContext(NonCancellable + Dispatchers.IO) {
                stage.deleteRecursively()
                if (lessonId.isNotBlank() && dao.lesson(lessonId)?.downloadState == "DOWNLOADING") {
                    dao.setDownloadState(lessonId, "CANCELLED")
                }
            }
        }
    }

    private suspend fun download(baseUrl: String, artifact: Clip, stage: File) = withContext(Dispatchers.IO) {
        ensureActive()
        val destination = checkedDestination(stage, artifact.file + ".part")
        destination.parentFile?.mkdirs()
        val request = Request.Builder().url(CourseRepository.resolveUrl(baseUrl, artifact.file)).build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "${artifact.file}: HTTP ${response.code}" }
            val body = response.body
            FileOutputStream(destination).use { output -> body.byteStream().copyTo(output) }
        }
        check(destination.length() == artifact.bytes) {
            "${artifact.file}: expected ${artifact.bytes} bytes, got ${destination.length()}"
        }
        check(sha256(destination).equals(artifact.sha256, ignoreCase = true)) {
            "${artifact.file}: SHA-256 mismatch"
        }
    }

    private fun checkedDestination(root: File, relative: String): File {
        val destination = root.resolve(relative).canonicalFile
        check(destination.path.startsWith(root.canonicalPath + File.separator)) { "Unsafe artifact path: $relative" }
        return destination
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun Track.asClip() = Clip(file, bytes, sha256)

    companion object { const val LESSON_ID = "lesson_id" }
}
