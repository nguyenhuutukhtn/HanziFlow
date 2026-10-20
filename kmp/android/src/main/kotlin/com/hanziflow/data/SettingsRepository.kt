package com.hanziflow.data

import com.hanziflow.BuildConfig
import javax.inject.Inject

/** Simple key/value app settings (catalog base URL, transcript view, Vietnamese toggle, theme). */
interface SettingsRepository {
    suspend fun baseUrl(): String
    suspend fun setBaseUrl(value: String)
    suspend fun transcriptView(track: String): String?
    suspend fun setTranscriptView(track: String, mode: String)
    suspend fun vietnameseEnabled(): Boolean
    suspend fun setVietnameseEnabled(enabled: Boolean)
    suspend fun darkTheme(): Boolean
    suspend fun setDarkTheme(enabled: Boolean)

    /** Per lesson+track, default off (manual only; never auto-on for a lesson's first listen). */
    suspend fun chineseOnlyEnabled(lessonId: String, track: String): Boolean
    suspend fun setChineseOnlyEnabled(lessonId: String, track: String, enabled: Boolean)

    companion object {
        /**
         * Public Supabase Storage root for lesson packages, built from
         * `supabase.url` in kmp/local.properties at build time. Debug builds may
         * override it in the UI; release builds always use this value.
         */
        val DEFAULT_BASE_URL: String = BuildConfig.CONTENT_BASE_URL
        const val DEFAULT_DARK_THEME = true
        fun transcriptViewKey(track: String) = "transcript_view_$track"
        fun chineseOnlyKey(lessonId: String, track: String) = "chinese_only_${lessonId}_$track"
    }
}

class SettingsRepositoryImpl @Inject constructor(private val database: CourseDatabase) : SettingsRepository {
    private val dao = database.dao()

    override suspend fun baseUrl(): String =
        if (BuildConfig.DEBUG) {
            dao.setting(BASE_URL) ?: SettingsRepository.DEFAULT_BASE_URL
        } else {
            SettingsRepository.DEFAULT_BASE_URL
        }

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

    override suspend fun darkTheme(): Boolean =
        dao.setting(DARK_THEME)?.toBooleanStrictOrNull() ?: SettingsRepository.DEFAULT_DARK_THEME

    override suspend fun setDarkTheme(enabled: Boolean) =
        dao.putSetting(SettingEntity(DARK_THEME, enabled.toString()))

    override suspend fun chineseOnlyEnabled(lessonId: String, track: String): Boolean =
        dao.setting(SettingsRepository.chineseOnlyKey(lessonId, track))?.toBooleanStrictOrNull() ?: false

    override suspend fun setChineseOnlyEnabled(lessonId: String, track: String, enabled: Boolean) =
        dao.putSetting(SettingEntity(SettingsRepository.chineseOnlyKey(lessonId, track), enabled.toString()))

    companion object {
        const val BASE_URL = "catalog_base_url"
        const val VI_ENABLED = "transcript_vi_enabled"
        const val DARK_THEME = "dark_theme"
    }
}
