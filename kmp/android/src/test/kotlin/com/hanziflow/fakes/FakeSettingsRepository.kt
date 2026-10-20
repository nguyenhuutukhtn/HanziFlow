package com.hanziflow.fakes

import com.hanziflow.data.SettingsRepository

class FakeSettingsRepository(
    private var baseUrl: String = SettingsRepository.DEFAULT_BASE_URL,
    private var vietnamese: Boolean = true,
) : SettingsRepository {
    private val transcriptViews = mutableMapOf<String, String>()
    val setBaseUrlCalls = mutableListOf<String>()

    override suspend fun baseUrl(): String = baseUrl
    override suspend fun setBaseUrl(value: String) {
        setBaseUrlCalls += value
        baseUrl = value.trim().trimEnd('/') + "/"
    }

    override suspend fun transcriptView(track: String): String? = transcriptViews[track]
    override suspend fun setTranscriptView(track: String, mode: String) { transcriptViews[track] = mode }

    override suspend fun vietnameseEnabled(): Boolean = vietnamese
    override suspend fun setVietnameseEnabled(enabled: Boolean) { vietnamese = enabled }
}
