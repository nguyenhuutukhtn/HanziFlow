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
class ChatFailure(
    val httpCode: Int?,
    message: String,
    val quotaExhausted: Boolean = false,
    /** From a 429 answer: how long the provider asked us to wait. */
    val retryAfterMs: Long? = null,
    val window: QuotaWindow = QuotaWindow.UNKNOWN,
    /** Epoch ms when the earliest provider becomes usable again (set on all-providers-cooling). */
    val resetAtMs: Long? = null,
) : Exception(message)

interface ChatClient {
    /** Returns the answer, or throws [ChatFailure]. */
    suspend fun complete(messages: List<ChatMessage>): ChatAnswer
}

/**
 * Tries [clients] in order and falls back on any failure (user, 2026-10-25: personal app; a removed
 * preview model 404s, a bad key 401s). The answer's model label shows when a fallback answered.
 * With [cooldowns], a provider that answered 429 is skipped until its window resets ([keys] identify providers).
 */
class FallbackChatClient(
    private val clients: List<ChatClient>,
    private val cooldowns: ProviderCooldowns? = null,
    private val keys: List<String> = clients.indices.map { it.toString() },
) : ChatClient {
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        var last: ChatFailure? = null
        var all429 = true
        var tried = false
        for ((i, client) in clients.withIndex()) {
            val key = keys[i]
            if (cooldowns?.isCooling(key) == true) continue
            tried = true
            try {
                return client.complete(messages).also { cooldowns?.recordSuccess(key) }
            } catch (e: ChatFailure) {
                if (e.httpCode == 429) cooldowns?.recordRateLimit(key, QuotaInfo(e.retryAfterMs, e.window)) else all429 = false
                last = e
            }
        }
        if (!tried && clients.isNotEmpty()) {
            val reset = keys.mapNotNull { cooldowns?.resetAt(it) }.minOrNull()
            throw ChatFailure(429, "All AI providers are rate limited", quotaExhausted = true, resetAtMs = reset)
        }
        val failure = last ?: throw ChatFailure(null, "No AI provider configured")
        if (!all429) throw failure
        val reset = keys.mapNotNull { cooldowns?.resetAt(it) }.minOrNull()
        throw ChatFailure(429, failure.message.orEmpty(), quotaExhausted = true, resetAtMs = reset)
    }
}
