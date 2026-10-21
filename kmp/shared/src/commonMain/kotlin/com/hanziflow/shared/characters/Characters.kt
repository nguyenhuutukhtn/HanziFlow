package com.hanziflow.shared.characters

import com.hanziflow.shared.CourseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonCharacters(
    @SerialName("lesson_id") val lessonId: String,
    val characters: List<CharacterEntry> = emptyList(),
    @SerialName("components_prereq") val componentsPrereq: List<ComponentEntry> = emptyList(),
)

@Serializable
data class CharacterEntry(
    val char: String,
    val pinyin: String,
    @SerialName("han_viet") val hanViet: String,
    @SerialName("hsk_level") val hskLevel: Int? = null,
    @SerialName("decomp_ids") val decompIds: String? = null,
    val semantic: String? = null,
    val phonetic: String? = null,
    @SerialName("en_meaning") val enMeaning: String? = null,
    @SerialName("vi_meaning") val viMeaning: String,
    @SerialName("phonetic_family") val phoneticFamily: List<String> = emptyList(),
    @SerialName("stroke_available") val strokeAvailable: Boolean = false,
    @SerialName("lesson_words") val lessonWords: List<String> = emptyList(),
)

@Serializable
data class ComponentEntry(
    val char: String,
    val role: String,
    @SerialName("name_en") val nameEn: String? = null,
    @SerialName("name_vi") val nameVi: String,
)

class CharacterIndex(private val data: LessonCharacters) {
    private val chars = data.characters.associateBy { it.char }
    private val components = data.componentsPrereq.associateBy { it.char }
    fun entry(char: String): CharacterEntry? = chars[char]
    fun component(char: String): ComponentEntry? = components[char]
    fun componentsOf(char: String): List<ComponentEntry> {
        val ids = chars[char]?.decompIds.orEmpty().filterNot { it in IDEOGRAPHIC_DESCRIPTION }
        return ids.mapNotNull { components[it.toString()] }.distinctBy { it.char }
    }
    fun charactersOf(word: String): List<CharacterEntry> = word.mapNotNull { chars[it.toString()] }
    fun allCharacters(): List<CharacterEntry> = data.characters
    fun allComponents(): List<ComponentEntry> = data.componentsPrereq
    companion object { private const val IDEOGRAPHIC_DESCRIPTION = "⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻" }
}
