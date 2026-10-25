package com.hanziflow.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

enum class QuotaWindow { MINUTE, DAY, UNKNOWN }

/** What a 429 told us about when to come back. */
data class QuotaInfo(val retryAfterMs: Long?, val window: QuotaWindow)

/** Reads `Retry-After` and Gemini's RetryInfo / QuotaFailure error details. Never throws. */
object QuotaParser {
    fun parse(retryAfterHeader: String?, body: String): QuotaInfo {
        val fromHeader = retryAfterHeader?.trim()?.toDoubleOrNull()?.takeIf { it >= 0 }?.let { (it * 1000).toLong() }
        var delay: Long? = null
        var window = QuotaWindow.UNKNOWN
        try {
            val root: JsonElement = Json.parseToJsonElement(body)
            val obj = (if (root is JsonArray) root.firstOrNull() else root) as? JsonObject
            val details = (obj?.get("error") as? JsonObject)?.get("details") as? JsonArray
            for (d in details.orEmpty()) {
                val o = d as? JsonObject ?: continue
                val type = o["@type"]?.jsonPrimitive?.contentOrNull.orEmpty()
                if (type.endsWith("google.rpc.RetryInfo")) {
                    delay = o["retryDelay"]?.jsonPrimitive?.contentOrNull?.removeSuffix("s")?.toDoubleOrNull()
                        ?.takeIf { it >= 0 }?.let { (it * 1000).toLong() }
                } else if (type.endsWith("google.rpc.QuotaFailure")) {
                    for (v in o["violations"] as? JsonArray ?: JsonArray(emptyList())) {
                        val id = (v as? JsonObject)?.get("quotaId")?.jsonPrimitive?.contentOrNull.orEmpty()
                        if (id.contains("PerDay")) window = QuotaWindow.DAY
                        else if (id.contains("PerMinute") && window != QuotaWindow.DAY) window = QuotaWindow.MINUTE
                    }
                }
            }
        } catch (e: Exception) {
            // unparseable body: fall through with what we have
        }
        return QuotaInfo(fromHeader ?: delay, window)
    }
}
