package com.hanziflow.ai

/** One chat message. `failed` marks a user message whose request did not get an answer (kept for retry). */
data class ChatMessage(
    val role: String,
    val content: String,
    val createdAtMs: Long = 0,
    val failed: Boolean = false,
) {
    companion object {
        const val SYSTEM = "system"
        const val USER = "user"
        const val ASSISTANT = "assistant"
    }
}

/** Typed failure from a chat call; `message` is short and never contains the API key. */
class ChatFailure(val httpCode: Int?, message: String) : Exception(message)

interface ChatClient {
    /** Returns the assistant text, or throws [ChatFailure]. */
    suspend fun complete(messages: List<ChatMessage>): String
}
