package com.hanziflow.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class Fake(val model: String, val code: Int? = null) : ChatClient {
    var calls = 0
    override suspend fun complete(messages: List<ChatMessage>): ChatAnswer {
        calls++
        if (code != null || model.startsWith("net")) throw ChatFailure(code, "fail")
        return ChatAnswer("ok from $model", model)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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

    @Test fun `4xx config errors fall back too`() = runTest {
        assertEquals("b", FallbackChatClient(listOf(Fake("a", 401), Fake("b"))).complete(msgs).model)
        assertEquals("b", FallbackChatClient(listOf(Fake("a", 404), Fake("b"))).complete(msgs).model)
    }

    @Test fun `all failing reports the last error`() = runTest {
        try {
            FallbackChatClient(listOf(Fake("a", 429), Fake("b", 401))).complete(msgs)
            fail("expected ChatFailure")
        } catch (e: ChatFailure) {
            assertEquals(401, e.httpCode)
            assertFalse(e.quotaExhausted)
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

    private fun cooldowns(clock: () -> Long) = ProviderCooldowns(
        ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("fb_cd", Context.MODE_PRIVATE).also { it.edit().clear().commit() },
        clock,
    )

    @Test fun `cooling provider is skipped until its minute passes`() = runTest {
        var now = 1_000_000L
        val a = Fake("a", 429)
        val b = Fake("b")
        val client = FallbackChatClient(listOf(a, b), cooldowns { now }, listOf("a", "b"))
        assertEquals("b", client.complete(msgs).model)
        assertEquals(1, a.calls)
        now += 30_000
        assertEquals("b", client.complete(msgs).model)
        assertEquals(1, a.calls)
        now += 31_000
        client.complete(msgs)
        assertEquals(2, a.calls)
    }

    @Test fun `all providers cooling makes no calls and reports earliest reset`() = runTest {
        val now = 1_000_000L
        val a = Fake("a", 429)
        val b = Fake("b", 429)
        val client = FallbackChatClient(listOf(a, b), cooldowns { now }, listOf("a", "b"))
        try { client.complete(msgs); fail() } catch (e: ChatFailure) { assertTrue(e.quotaExhausted) }
        try { client.complete(msgs); fail() } catch (e: ChatFailure) {
            assertTrue(e.quotaExhausted)
            assertEquals(429, e.httpCode)
            assertEquals(now + 60_000, e.resetAtMs)
        }
        assertEquals(1, a.calls)
        assertEquals(1, b.calls)
    }

    @Test fun `non-429 failures set no cooldown`() = runTest {
        val a = Fake("a", 503)
        val client = FallbackChatClient(listOf(a, Fake("b")), cooldowns { 1L }, listOf("a", "b"))
        client.complete(msgs); client.complete(msgs)
        assertEquals(2, a.calls)
    }
}
