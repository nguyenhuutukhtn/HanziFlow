package com.hanziflow.fakes

import com.hanziflow.data.SettingsRepository

class FakeSettingsRepository(
    private var baseUrl: String = SettingsRepository.DEFAULT_BASE_URL,
    private var vietnamese: Boolean = true,
    private var dark: Boolean = SettingsRepository.DEFAULT_DARK_THEME,
) : SettingsRepository {
    private val transcriptViews = mutableMapOf<String, String>()
    private val chineseOnly = mutableMapOf<String, Boolean>()
    private val repeatEach = mutableMapOf<String, Boolean>()
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

    override suspend fun darkTheme(): Boolean = dark
    override suspend fun setDarkTheme(enabled: Boolean) { dark = enabled }

    override suspend fun chineseOnlyEnabled(lessonId: String, track: String): Boolean =
        chineseOnly[SettingsRepository.chineseOnlyKey(lessonId, track)] ?: false

    override suspend fun setChineseOnlyEnabled(lessonId: String, track: String, enabled: Boolean) {
        chineseOnly[SettingsRepository.chineseOnlyKey(lessonId, track)] = enabled
    }

    override suspend fun repeatEachEnabled(lessonId: String, track: String): Boolean =
        repeatEach[SettingsRepository.repeatEachKey(lessonId, track)] ?: false

    override suspend fun setRepeatEachEnabled(lessonId: String, track: String, enabled: Boolean) {
        repeatEach[SettingsRepository.repeatEachKey(lessonId, track)] = enabled
    }
}
