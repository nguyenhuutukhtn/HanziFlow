package com.hanziflow.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.data.CourseDatabase

class CountingChat : ChatClient {
    var calls = 0
    var fail = false
    override suspend fun complete(messages: List<ChatMessage>): String {
        calls++
        if (fail) throw ChatFailure(500, "boom")
        return if (calls == 1) "Đây là **lời giải thích** đầu tiên." else "Trả lời số $calls"
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
        ExplainViewModel(repository(), AiConfig(if (configured) "https://x" else "", "m", if (configured) "k" else ""))
    fun close() = db.close()
}
