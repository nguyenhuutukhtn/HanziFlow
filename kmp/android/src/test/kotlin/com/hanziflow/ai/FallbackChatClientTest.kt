package com.hanziflow.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

private class Fake(val model: String, val code: Int? = null) : ChatClient {
    var calls = 0
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        calls++
        if (code != null || model.startsWith("net")) throw ChatFailure(code, "fail")
        return ChatAnswer("ok from $model", model)
    }
}

class FallbackChatClientTest {
    private val msgs = listOf(ChatMessage("user", "hi"))

    @Test fun `429 falls back to the next and reports its model`() = runTest {
        val out = FallbackChatClient(listOf(Fake("a", 429), Fake("b"))).complete(msgs)
        assertEquals("b", out.model)
    }

    @Test fun `5xx falls back`() = runTest {
        assertEquals("b", FallbackChatClient(listOf(Fake("a", 503), Fake("b"))).complete(msgs).model)
    }

    @Test fun `timeout or io error falls back`() = runTest {
        assertEquals("b", FallbackChatClient(listOf(Fake("net-a"), Fake("b"))).complete(msgs).model)
    }

    @Test fun `401 does not fall back`() = runTest {
        val second = Fake("b")
        try {
            FallbackChatClient(listOf(Fake("a", 401), second)).complete(msgs)
            fail("expected ChatFailure")
        } catch (e: ChatFailure) {
            assertEquals(401, e.httpCode)
            assertEquals(0, second.calls)
        }
    }

    @Test fun `all 429 means quota exhausted`() = runTest {
        try {
            FallbackChatClient(listOf(Fake("a", 429), Fake("b", 429))).complete(msgs)
            fail("expected ChatFailure")
        } catch (e: ChatFailure) {
            assertTrue(e.quotaExhausted)
            assertEquals(429, e.httpCode)
        }
    }

    @Test fun `mixed failures are not flagged as quota`() = runTest {
        try {
            FallbackChatClient(listOf(Fake("a", 429), Fake("net-b"))).complete(msgs)
            fail("expected ChatFailure")
        } catch (e: ChatFailure) {
            assertFalse(e.quotaExhausted)
        }
    }
}
