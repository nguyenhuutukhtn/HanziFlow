package com.hanziflow.ai

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiCompatibleClient(http: OkHttpClient, private val config: AiConfig) : ChatClient {
    private val http = http.newBuilder()
        .callTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun complete(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val base = config.baseUrl.let { if (it.endsWith("/")) it else "$it/" }
        val body = JsonObject(
            mapOf(
                "model" to JsonPrimitive(config.model),
                "messages" to JsonArray(
                    messages.map {
                        JsonObject(mapOf("role" to JsonPrimitive(it.role), "content" to JsonPrimitive(it.content)))
                    },
                ),
            ),
        )
        val request = try {
            Request.Builder()
                .url(base + "chat/completions")
                .header("Authorization", "Bearer ${config.apiKey}")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()
        } catch (e: IllegalArgumentException) {
            throw ChatFailure(null, "Invalid LLM base URL")
        }
        try {
            http.newCall(request).execute().use { response ->
                val text = response.body.string()
                if (!response.isSuccessful) throw ChatFailure(response.code, "HTTP ${response.code}: ${redact(text.take(200))}")
                parseContent(text) ?: throw ChatFailure(response.code, "Unexpected response shape")
            }
        } catch (e: IOException) {
            throw ChatFailure(null, "Network error: ${redact(e.message.orEmpty().take(200))}")
        }
    }

    private fun parseContent(text: String): String? = try {
        Json.parseToJsonElement(text).jsonObject["choices"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
    } catch (e: Exception) {
        null
    }

    private fun redact(s: String) = if (config.apiKey.isBlank()) s else s.replace(config.apiKey, "***")
}
