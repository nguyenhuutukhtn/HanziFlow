package com.hanziflow.ai

import com.hanziflow.BuildConfig

/** Optional chat-LLM settings from local.properties (via BuildConfig). The key is never logged. */
data class AiConfig(val baseUrl: String, val model: String, val apiKey: String) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank()

    override fun toString(): String = "AiConfig(baseUrl=$baseUrl, model=$model, apiKey=***)"

    companion object {
        fun fromBuildConfig() = AiConfig(BuildConfig.LLM_BASE_URL, BuildConfig.LLM_MODEL, BuildConfig.LLM_API_KEY)
    }
}
