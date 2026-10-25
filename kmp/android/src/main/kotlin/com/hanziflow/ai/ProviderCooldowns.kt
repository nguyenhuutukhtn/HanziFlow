package com.hanziflow.ai

import android.content.SharedPreferences
import java.time.Instant
import java.time.ZoneId

/**
 * Per-provider "locked until" times after a 429, persisted so a daily lock survives a restart.
 * Key = model + base url. DAY locks until the next Pacific midnight (Gemini's reset).
 */
class ProviderCooldowns(private val prefs: SharedPreferences, private val clock: () -> Long = System::currentTimeMillis) {
    fun resetAt(key: String): Long? {
        val until = prefs.getLong("until:$key", 0L)
        if (until <= clock()) {
            if (until != 0L) prefs.edit().remove("until:$key").apply()
            return null
        }
        return until
    }

    fun isCooling(key: String): Boolean = resetAt(key) != null

    fun recordRateLimit(key: String, info: QuotaInfo) {
        val now = clock()
        val info = info.copy(retryAfterMs = info.retryAfterMs?.coerceAtMost(DAY_MS)) // "Retry-After: 1e30" must not overflow
        val strikes = prefs.getInt("strikes:$key", 0) + 1
        val until = when {
            info.window == QuotaWindow.DAY -> nextPacificMidnight(now)
            info.window == QuotaWindow.MINUTE -> now + maxOf(info.retryAfterMs ?: 0L, MINUTE_MS)
            info.retryAfterMs != null -> now + info.retryAfterMs
            else -> now + minOf(MINUTE_MS shl (strikes - 1).coerceAtMost(10), MAX_BACKOFF_MS)
        }
        prefs.edit().putLong("until:$key", until).putInt("strikes:$key", strikes).apply()
    }

    fun recordSuccess(key: String) {
        if (prefs.contains("until:$key") || prefs.contains("strikes:$key")) {
            prefs.edit().remove("until:$key").remove("strikes:$key").apply()
        }
    }

    private fun nextPacificMidnight(now: Long): Long =
        Instant.ofEpochMilli(now).atZone(PACIFIC).toLocalDate().plusDays(1).atStartOfDay(PACIFIC).toInstant().toEpochMilli()

    private companion object {
        const val MINUTE_MS = 60_000L
        const val DAY_MS = 24 * 60 * MINUTE_MS
        const val MAX_BACKOFF_MS = 30 * 60_000L
        val PACIFIC: ZoneId = ZoneId.of("America/Los_Angeles")
    }
}
