package com.hanziflow.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProviderCooldownsTest {
    private val la = ZoneId.of("America/Los_Angeles")
    private fun ms(y: Int, mo: Int, d: Int, h: Int, mi: Int, s: Int = 0) =
        ZonedDateTime.of(y, mo, d, h, mi, s, 0, la).toInstant().toEpochMilli()

    private var now = 0L
    private val prefs get() = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("ai_cooldowns_test", Context.MODE_PRIVATE)
    private fun cd() = ProviderCooldowns(prefs) { now }

    @Test fun `day lock lasts until next midnight Pacific`() {
        prefs.edit().clear().commit()
        now = ms(2026, 10, 25, 10, 0)
        val c = cd()
        c.recordRateLimit("a", QuotaInfo(34_000, QuotaWindow.DAY))
        assertTrue(c.isCooling("a"))
        now = ms(2026, 10, 25, 23, 59, 59)
        assertTrue(c.isCooling("a"))
        assertEquals(ms(2026, 10, 26, 0, 0), c.resetAt("a"))
        now = ms(2026, 10, 26, 0, 0, 1)
        assertFalse(c.isCooling("a"))
        assertNull(c.resetAt("a"))
    }

    @Test fun `minute lock is at least 60 seconds`() {
        prefs.edit().clear().commit()
        now = 1_000_000
        val c = cd()
        c.recordRateLimit("a", QuotaInfo(5_000, QuotaWindow.MINUTE))
        now += 59_000
        assertTrue(c.isCooling("a"))
        now += 2_000
        assertFalse(c.isCooling("a"))
    }

    @Test fun `huge retryAfter is capped at a day, no overflow`() {
        prefs.edit().clear().commit()
        now = 1_000_000
        val c = cd()
        c.recordRateLimit("a", QuotaInfo(Long.MAX_VALUE, QuotaWindow.UNKNOWN))
        assertEquals(now + 86_400_000L, c.resetAt("a"))
    }

    @Test fun `unknown with retryAfter uses it`() {
        prefs.edit().clear().commit()
        now = 1_000_000
        val c = cd()
        c.recordRateLimit("a", QuotaInfo(10_000, QuotaWindow.UNKNOWN))
        now += 11_000
        assertFalse(c.isCooling("a"))
    }

    @Test fun `unknown backoff doubles and resets on success`() {
        prefs.edit().clear().commit()
        now = 1_000_000
        val c = cd()
        val unknown = QuotaInfo(null, QuotaWindow.UNKNOWN)
        c.recordRateLimit("a", unknown); assertEquals(now + 60_000, c.resetAt("a"))
        c.recordRateLimit("a", unknown); assertEquals(now + 120_000, c.resetAt("a"))
        c.recordRateLimit("a", unknown); assertEquals(now + 240_000, c.resetAt("a"))
        c.recordSuccess("a")
        assertFalse(c.isCooling("a"))
        c.recordRateLimit("a", unknown); assertEquals(now + 60_000, c.resetAt("a"))
    }

    @Test fun `backoff is capped at 30 minutes`() {
        prefs.edit().clear().commit()
        now = 1_000_000
        val c = cd()
        repeat(12) { c.recordRateLimit("a", QuotaInfo(null, QuotaWindow.UNKNOWN)) }
        assertEquals(now + 30 * 60_000L, c.resetAt("a"))
    }

    @Test fun `lock survives a new instance on the same prefs`() {
        prefs.edit().clear().commit()
        now = ms(2026, 10, 25, 10, 0)
        cd().recordRateLimit("a", QuotaInfo(null, QuotaWindow.DAY))
        val other = cd()
        assertTrue(other.isCooling("a"))
        assertFalse(other.isCooling("b"))
    }
}
