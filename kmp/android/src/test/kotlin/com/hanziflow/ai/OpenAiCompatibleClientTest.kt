package com.hanziflow.ai

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class OpenAiCompatibleClientTest {
    private lateinit var server: MockWebServer

    @Before fun start() { server = MockWebServer().apply { start() } }
    @After fun stop() { server.shutdown() }

    private fun client(baseUrl: String = server.url("/v1").toString()) =
        OpenAiCompatibleClient(OkHttpClient(), AiConfig(baseUrl, "test-model", "test-key"))

    @Test
    fun `posts chat completions with bearer key model and ordered messages and parses content`() = runTest {
        server.enqueue(MockResponse().setBody("""{"choices":[{"index":0,"message":{"role":"assistant","content":"Xin chào"}}]}"""))
        val out = client().complete(listOf(ChatMessage("system", "sys"), ChatMessage("user", "hi")))
        assertEquals("Xin chào", out)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.endsWith("/chat/completions"))
        assertEquals("/v1/chat/completions", req.path)
        assertEquals("Bearer test-key", req.getHeader("Authorization"))
        val body = req.body.readUtf8()
        assertTrue(body.contains("\"model\":\"test-model\""))
        assertTrue(body.indexOf("\"sys\"") in 0 until body.indexOf("\"hi\""))
    }

    @Test
    fun `http 429 becomes a typed failure without the key`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":"rate limit for Bearer test-key"}"""))
        try {
            client().complete(listOf(ChatMessage("user", "hi")))
            fail("expected ChatFailure")
        } catch (e: ChatFailure) {
            assertEquals(429, e.httpCode)
            assertNotNull(e.message)
            assertFalse(e.message!!.contains("test-key"))
            assertTrue(e.message!!.contains("429"))
        }
    }
}
