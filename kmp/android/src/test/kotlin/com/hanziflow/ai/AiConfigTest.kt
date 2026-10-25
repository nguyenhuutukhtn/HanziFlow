package com.hanziflow.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigTest {
    private fun p(base: String, model: String, key: String) = """{"base_url":"$base","model":"$model","api_key":"$key"}"""
    private val blank = p("", "", "")

    @Test fun `only provider 1 set gives one provider`() {
        val r = AiProviders.parse("[${p("https://x/v1", "m1", "test-key")},$blank,$blank,$blank]")
        assertEquals(listOf(AiConfig("https://x/v1", "m1", "test-key")), r.list)
        assertTrue(r.isConfigured)
    }

    @Test fun `provider 2 with only a model reuses provider 1 base url and key`() {
        val r = AiProviders.parse("[${p("https://x/v1", "m1", "test-key")},${p("", "m2", "")},$blank,$blank]")
        assertEquals(2, r.list.size)
        assertEquals(AiConfig("https://x/v1", "m2", "test-key"), r.list[1])
    }

    @Test fun `blank everything is not configured`() {
        assertFalse(AiProviders.parse("[$blank,$blank,$blank,$blank]").isConfigured)
        assertFalse(AiProviders.parse("").isConfigured)
        assertFalse(AiProviders.parse("[$blank,${p("", "m2", "")}]").isConfigured)
    }
}
