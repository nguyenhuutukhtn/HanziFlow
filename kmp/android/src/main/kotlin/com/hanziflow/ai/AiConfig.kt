package com.hanziflow.ai

import com.hanziflow.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** One chat-LLM provider from local.properties. The key is never logged. */
data class AiConfig(val baseUrl: String, val model: String, val apiKey: String) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank()

    override fun toString(): String = "AiConfig(baseUrl=$baseUrl, model=$model, apiKey=***)"
}

/** Ordered, complete providers; empty = the feature is off. */
data class AiProviders(val list: List<AiConfig>) {
    val isConfigured: Boolean get() = list.any { it.isConfigured }

    companion object {
        fun fromBuildConfig() = parse(BuildConfig.LLM_PROVIDERS)

        /**
         * `[{base_url, model, api_key}, ...]`, provider 1 first. Providers 2+ with a blank base_url or
         * api_key reuse provider 1's (e.g. Flash -> Flash-Lite on the same key); incomplete ones are dropped.
         */
        fun parse(json: String): AiProviders {
            val raw = try {
                Json.parseToJsonElement(json.ifBlank { "[]" }).jsonArray.map {
                    val o = it.jsonObject
                    fun f(k: String) = o[k]?.jsonPrimitive?.content.orEmpty().trim()
                    AiConfig(f("base_url"), f("model"), f("api_key"))
                }
            } catch (e: Exception) {
                emptyList()
            }
            val first = raw.firstOrNull() ?: return AiProviders(emptyList())
            val all = raw.mapIndexed { i, c ->
                if (i == 0) c else c.copy(baseUrl = c.baseUrl.ifBlank { first.baseUrl }, apiKey = c.apiKey.ifBlank { first.apiKey })
            }
            return AiProviders(all.filter { it.isConfigured })
        }
    }
}
