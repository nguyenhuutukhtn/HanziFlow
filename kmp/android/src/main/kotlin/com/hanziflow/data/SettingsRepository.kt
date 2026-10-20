package com.hanziflow.data

import javax.inject.Inject

/** Simple key/value app settings (catalog base URL, transcript view, Vietnamese toggle). */
interface SettingsRepository {
    suspend fun baseUrl(): String
    suspend fun setBaseUrl(value: String)
    suspend fun transcriptView(track: String): String?
    suspend fun setTranscriptView(track: String, mode: String)
    suspend fun vietnameseEnabled(): Boolean
    suspend fun setVietnameseEnabled(enabled: Boolean)

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
        fun transcriptViewKey(track: String) = "transcript_view_$track"
    }
}

class SettingsRepositoryImpl @Inject constructor(private val database: CourseDatabase) : SettingsRepository {
    private val dao = database.dao()

    override suspend fun baseUrl(): String = dao.setting(BASE_URL) ?: SettingsRepository.DEFAULT_BASE_URL

    override suspend fun setBaseUrl(value: String) {
        dao.putSetting(SettingEntity(BASE_URL, value.trim().trimEnd('/') + "/"))
    }

    override suspend fun transcriptView(track: String): String? =
        dao.setting(SettingsRepository.transcriptViewKey(track))

    override suspend fun setTranscriptView(track: String, mode: String) =
        dao.putSetting(SettingEntity(SettingsRepository.transcriptViewKey(track), mode))

    override suspend fun vietnameseEnabled(): Boolean = dao.setting(VI_ENABLED)?.toBooleanStrictOrNull() ?: true

    override suspend fun setVietnameseEnabled(enabled: Boolean) =
        dao.putSetting(SettingEntity(VI_ENABLED, enabled.toString()))

    companion object {
        const val BASE_URL = "catalog_base_url"
        const val VI_ENABLED = "transcript_vi_enabled"
    }
}
