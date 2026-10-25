package com.hanziflow.ai

import com.hanziflow.data.CourseDao
import com.hanziflow.data.ExplainThreadEntity
import com.hanziflow.shared.ReferenceLine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** The line to explain plus up to 2 same-track Chinese lines either side. */
data class ExplainTarget(
    val lessonId: String,
    val lessonVersion: Int,
    val line: ReferenceLine,
    val before: List<ReferenceLine> = emptyList(),
    val after: List<ReferenceLine> = emptyList(),
)

/** `isStale` = the saved hanzi differs from the current line's hanzi (UI offers re-explain). */
data class ExplainThread(
    val lessonId: String,
    val lineId: String,
    val hanzi: String,
    val lessonVersion: Int,
    val messages: List<ChatMessage>,
    val updatedAt: Long,
    val isStale: Boolean = false,
)

class ExplainRepository @Inject constructor(
    private val dao: CourseDao,
    private val client: ChatClient,
    private val prompt: ExplainPrompt,
) {
    internal var now: () -> Long = System::currentTimeMillis

    /** Saved thread for a line; pass the current hanzi to get `isStale` filled in. */
    fun thread(lessonId: String, lineId: String, currentHanzi: String? = null): Flow<ExplainThread?> =
        dao.observeExplainThread(lessonId, lineId).map { it?.toThread(currentHanzi) }

    suspend fun load(lessonId: String, lineId: String, currentHanzi: String? = null): ExplainThread? =
        dao.explainThread(lessonId, lineId)?.toThread(currentHanzi)

    /**
     * Saved thread if one exists (no network, even when stale); otherwise asks the model and saves
     * [user, assistant]. [force] replaces any saved thread ("Giải thích lại").
     */
    suspend fun explain(target: ExplainTarget, force: Boolean = false): Result<ExplainThread> {
        val id = target.line.id
        if (!force) load(target.lessonId, id, target.line.text)?.let { return Result.success(it) }
        if (!force) {
            // The same sentence in another mode/line of this lesson: copy its explanation, no network.
            dao.explainThreadWithSameHanzi(target.lessonId, id, target.line.text)?.let { src ->
                val first = decode(src.messagesJson).take(2)
                if (first.size == 2) return Result.success(save(target.lessonId, id, target.line.text, target.lessonVersion, first))
            }
        }
        val user = ChatMessage(ChatMessage.USER, prompt.render(target.line, target.before, target.after), now())
        return try {
            val answer = client.complete(listOf(system(), user))
            val saved = listOf(user, ChatMessage(ChatMessage.ASSISTANT, answer.text, now(), model = answer.model))
            Result.success(save(target.lessonId, id, target.line.text, target.lessonVersion, saved))
        } catch (e: ChatFailure) {
            Result.failure(e)
        }
    }

    /** Appends a question; on failure the question stays in the thread marked failed. */
    suspend fun ask(lessonId: String, lineId: String, question: String): Result<ExplainThread> {
        val entity = dao.explainThread(lessonId, lineId) ?: return Result.failure(IllegalStateException("No thread"))
        return answer(entity, decode(entity.messagesJson) + ChatMessage(ChatMessage.USER, question, now()))
    }

    /** Re-sends the trailing failed question, if any. */
    suspend fun retry(lessonId: String, lineId: String): Result<ExplainThread> {
        val entity = dao.explainThread(lessonId, lineId) ?: return Result.failure(IllegalStateException("No thread"))
        val messages = decode(entity.messagesJson)
        val last = messages.lastOrNull()
        if (last == null || !last.failed) return Result.failure(IllegalStateException("Nothing to retry"))
        return answer(entity, messages.dropLast(1) + last.copy(failed = false))
    }

    private suspend fun answer(entity: ExplainThreadEntity, withQuestion: List<ChatMessage>): Result<ExplainThread> {
        val send = withQuestion.filter { !it.failed }
        return try {
            val reply = client.complete(listOf(system()) + send)
            val all = withQuestion + ChatMessage(ChatMessage.ASSISTANT, reply.text, now(), model = reply.model)
            Result.success(save(entity.lessonId, entity.lineId, entity.hanzi, entity.lessonVersion, all))
        } catch (e: ChatFailure) {
            val kept = withQuestion.dropLast(1) + withQuestion.last().copy(failed = true)
            save(entity.lessonId, entity.lineId, entity.hanzi, entity.lessonVersion, kept)
            Result.failure(e)
        }
    }

    private fun system() = ChatMessage(ChatMessage.SYSTEM, prompt.system)

    private suspend fun save(lessonId: String, lineId: String, hanzi: String, version: Int, messages: List<ChatMessage>): ExplainThread {
        val at = now()
        dao.putExplainThread(ExplainThreadEntity(lessonId, lineId, hanzi, version, encode(messages), at))
        return ExplainThread(lessonId, lineId, hanzi, version, messages, at)
    }

    private fun ExplainThreadEntity.toThread(currentHanzi: String?) = ExplainThread(
        lessonId, lineId, hanzi, lessonVersion, decode(messagesJson), updatedAt,
        isStale = currentHanzi != null && currentHanzi != hanzi,
    )

    private companion object {
        fun encode(messages: List<ChatMessage>): String = JsonArray(
            messages.map {
                JsonObject(
                    buildMap {
                        put("role", JsonPrimitive(it.role))
                        put("content", JsonPrimitive(it.content))
                        put("createdAtMs", JsonPrimitive(it.createdAtMs))
                        if (it.failed) put("failed", JsonPrimitive(true))
                        it.model?.let { m -> put("model", JsonPrimitive(m)) }
                    },
                )
            },
        ).toString()

        fun decode(json: String): List<ChatMessage> = Json.parseToJsonElement(json).jsonArray.map {
            val o = it.jsonObject
            ChatMessage(
                role = o.getValue("role").jsonPrimitive.content,
                content = o.getValue("content").jsonPrimitive.content,
                createdAtMs = o["createdAtMs"]?.jsonPrimitive?.long ?: 0,
                failed = o["failed"]?.jsonPrimitive?.booleanOrNull ?: false,
                model = o["model"]?.jsonPrimitive?.contentOrNull,
            )
        }
    }
}
