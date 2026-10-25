package com.hanziflow.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.data.CourseDatabase

class CountingChat : ChatClient {
    var calls = 0
    var fail = false
    var failure: ChatFailure? = null
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        calls++
        failure?.let { throw it }
        if (fail) throw ChatFailure(500, "boom")
        return ChatAnswer(if (calls == 1) "Đây là **lời giải thích** đầu tiên." else "Trả lời số $calls", "fake-model")
    }
}


/** In-memory Room + counting fake chat client; kept out of composable test files (layering rule). */
class ExplainTestEnv {
    val chat = CountingChat()
    private val prompt = ExplainPrompt("# System\n\nsys\n\n---USER---\n\nExplain {{hanzi}} {{pinyin}} {{meaning_vi}} {{context_before}} {{context_after}}")
    private val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CourseDatabase::class.java)
        .allowMainThreadQueries().build()

    fun repository() = ExplainRepository(db.dao(), chat, prompt)
    fun viewModel(configured: Boolean = true) =
        ExplainViewModel(repository(), AiProviders(if (configured) listOf(AiConfig("https://x", "m", "k")) else emptyList()))
    fun repository(client: ChatClient) = ExplainRepository(db.dao(), client, prompt)
    fun close() = db.close()
}
