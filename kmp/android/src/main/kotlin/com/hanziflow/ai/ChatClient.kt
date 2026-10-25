package com.hanziflow.ai

/** One chat message. `failed` marks a user message whose request did not get an answer (kept for retry). */
data class ChatMessage(
    val role: String,
    val content: String,
    val createdAtMs: Long = 0,
    val failed: Boolean = false,
    /** Model that wrote an assistant message; null for user messages and rows saved before X4. */
    val model: String? = null,
) {
    companion object {
        const val SYSTEM = "system"
        const val USER = "user"
        const val ASSISTANT = "assistant"
    }
}

/** An assistant answer plus the model that produced it. */
data class ChatAnswer(val text: String, val model: String)

/** Typed failure from a chat call; `message` is short and never contains the API key. `httpCode` null = network/timeout. */
class ChatFailure(val httpCode: Int?, message: String, val quotaExhausted: Boolean = false) : Exception(message)

interface ChatClient {
    /** Returns the answer, or throws [ChatFailure]. */
    suspend fun complete(messages: List<ChatMessage>): ChatAnswer
}

/** Tries [clients] in order; falls back on 429, 5xx and network errors, never on 4xx config errors. */
class FallbackChatClient(private val clients: List<ChatClient>) : ChatClient {
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        var last: ChatFailure? = null
        var all429 = true
        for (client in clients) {
            try {
                return client.complete(messages)
            } catch (e: ChatFailure) {
                val code = e.httpCode
                if (code != null && code != 429 && code < 500) throw e
                if (code != 429) all429 = false
                last = e
            }
        }
        val failure = last ?: throw ChatFailure(null, "No AI provider configured")
        throw if (all429) ChatFailure(429, failure.message.orEmpty(), quotaExhausted = true) else failure
    }
}
