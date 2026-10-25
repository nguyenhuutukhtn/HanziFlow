package com.hanziflow.ai

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hanziflow.data.CourseDatabase
import com.hanziflow.shared.ReferenceLine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeChat : ChatClient {
    var calls = 0
    var lastSent: List<ChatMessage> = emptyList()
    var fail = false
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        calls++
        lastSent = messages
        if (fail) throw ChatFailure(500, "boom")
        return ChatAnswer("answer $calls", "fake-model")
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ExplainRepositoryTest {
    private lateinit var db: CourseDatabase
    private val chat = FakeChat()
    private val prompt = ExplainPrompt("# System\n\nsys prompt\n\n---USER---\n\nExplain {{hanzi}} / {{pinyin}} / {{meaning_vi}} / {{context_before}} / {{context_after}}")
    private lateinit var repo: ExplainRepository

    private fun line(text: String) = ReferenceLine("l1", "sentence", "zh", "A", text, pinyin = "py", translation = "vi")
    private fun target(text: String = "大厅在楼里面，很大。") = ExplainTarget("lesson_002", 4, line(text))

    @Before fun open() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CourseDatabase::class.java)
            .allowMainThreadQueries().build()
        repo = ExplainRepository(db.dao(), chat, prompt)
    }

    @After fun close() { db.close() }

    @Test
    fun `explain saves two messages and reopening makes no new call`() = runTest {
        val first = repo.explain(target()).getOrThrow()
        assertEquals(1, chat.calls)
        assertEquals(2, first.messages.size)
        val again = repo.explain(target()).getOrThrow()
        assertEquals(1, chat.calls)
        assertEquals(first.messages, again.messages)
        assertEquals(first.messages, repo.thread("lesson_002", "l1").first()!!.messages)
    }

    @Test
    fun `ask sends system plus history and saves four messages`() = runTest {
        repo.explain(target()).getOrThrow()
        val t = repo.ask("lesson_002", "l1", "Tại sao?").getOrThrow()
        assertEquals(4, chat.lastSent.size)
        assertEquals("system", chat.lastSent[0].role)
        assertEquals("Tại sao?", chat.lastSent[3].content)
        assertEquals(4, t.messages.size)
        assertEquals(4, repo.load("lesson_002", "l1")!!.messages.size)
    }

    @Test
    fun `failed ask keeps the question marked failed and earlier messages intact, retry completes it`() = runTest {
        val first = repo.explain(target()).getOrThrow()
        chat.fail = true
        assertTrue(repo.ask("lesson_002", "l1", "Q?").isFailure)
        val saved = repo.load("lesson_002", "l1")!!
        assertEquals(3, saved.messages.size)
        assertTrue(saved.messages.last().failed)
        assertEquals(first.messages, saved.messages.take(2))
        chat.fail = false
        val done = repo.retry("lesson_002", "l1").getOrThrow()
        assertEquals(4, done.messages.size)
        assertFalse(done.messages.any { it.failed })
        assertEquals("Q?", chat.lastSent.last().content)
    }

    @Test
    fun `same line id with different hanzi is stale, no network, force replaces`() = runTest {
        repo.explain(target()).getOrThrow()
        val stale = repo.explain(target("别的句子。")).getOrThrow()
        assertEquals(1, chat.calls)
        assertTrue(stale.isStale)
        assertTrue(repo.thread("lesson_002", "l1", "别的句子。").first()!!.isStale)
        val fresh = repo.explain(target("别的句子。"), force = true).getOrThrow()
        assertEquals(2, chat.calls)
        assertFalse(fresh.isStale)
        assertNotNull(repo.load("lesson_002", "l1", "别的句子。"))
        assertEquals(2, repo.load("lesson_002", "l1")!!.messages.size)
    }

    @Test
    fun `blank key is NotConfigured and the client is never called`() = runTest {
        val vm = ExplainViewModel(repo, AiProviders.parse("""[{"base_url":"https://x/v1/","model":"m","api_key":""}]"""))
        vm.open(target())
        assertEquals(ExplainUiState.NotConfigured, vm.state.value)
        assertEquals(0, chat.calls)
    }

    private fun lineAt(id: String, text: String) = ReferenceLine(id, "sentence", "zh", "A", text, pinyin = "py", translation = "vi")

    @Test
    fun `identical hanzi on another line reuses the explanation with no call and without follow-ups`() = runTest {
        val a = ExplainTarget("lesson_002", 4, lineAt("a", "大厅很大。"))
        repo.explain(a).getOrThrow()
        repo.ask("lesson_002", "a", "Tại sao?").getOrThrow()
        assertEquals(2, chat.calls)
        val b = repo.explain(ExplainTarget("lesson_002", 4, lineAt("b", "大厅很大。"))).getOrThrow()
        assertEquals(2, chat.calls)
        assertEquals(2, b.messages.size)
        assertEquals("b", b.messages.let { repo.load("lesson_002", "b")!!.lineId })
        assertEquals(2, repo.load("lesson_002", "b")!!.messages.size)
        assertEquals("fake-model", b.messages[1].model)
        assertEquals(4, repo.load("lesson_002", "a")!!.messages.size)
        repo.explain(ExplainTarget("lesson_002", 4, lineAt("c", "别的。"))).getOrThrow()
        assertEquals(3, chat.calls)
    }

    @Test
    fun `old stored thread json without model still loads`() = runTest {
        db.dao().putExplainThread(
            com.hanziflow.data.ExplainThreadEntity(
                "lesson_002", "old", "你好", 1,
                """[{"role":"user","content":"p","createdAtMs":1},{"role":"assistant","content":"a","createdAtMs":2}]""", 2,
            ),
        )
        val t = repo.load("lesson_002", "old")!!
        assertEquals(2, t.messages.size)
        assertEquals(null, t.messages[1].model)
    }
}
