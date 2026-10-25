package com.hanziflow.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Fixtures are hand-written from the documented Gemini / Groq error shapes, not recorded responses. */
class QuotaParserTest {
    private val arrayDay = """[{"error":{"code":429,"status":"RESOURCE_EXHAUSTED","details":[
        {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[{"quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier"}]},
        {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"34s"}]}}]"""
    private val objectMinute = """{"error":{"code":429,"details":[
        {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[{"quotaId":"GenerateContentInputTokensPerModelPerMinute-FreeTier"}]},
        {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"1.5s"}]}}"""

    @Test fun `gemini array body with per-day quota`() {
        val q = QuotaParser.parse(null, arrayDay)
        assertEquals(QuotaWindow.DAY, q.window)
        assertEquals(34_000L, q.retryAfterMs)
    }

    @Test fun `gemini object body with per-minute quota`() {
        val q = QuotaParser.parse(null, objectMinute)
        assertEquals(QuotaWindow.MINUTE, q.window)
        assertEquals(1_500L, q.retryAfterMs)
    }

    @Test fun `Retry-After header with empty body`() {
        val q = QuotaParser.parse("20", "")
        assertEquals(20_000L, q.retryAfterMs)
        assertEquals(QuotaWindow.UNKNOWN, q.window)
    }

    @Test fun `garbage never throws`() {
        for (body in listOf("not json", "[1,2]", "{\"error\":5}", "[{\"error\":{\"details\":\"x\"}}]", "")) {
            val q = QuotaParser.parse("soon", body)
            assertEquals(QuotaWindow.UNKNOWN, q.window)
            assertNull(q.retryAfterMs)
        }
    }
}
