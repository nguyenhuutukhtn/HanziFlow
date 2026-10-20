package com.hanziflow.shared.testgen

private val TONE_TO_BASE: Map<Char, Char> = mapOf(
    'ā' to 'a', 'á' to 'a', 'ǎ' to 'a', 'à' to 'a',
    'ē' to 'e', 'é' to 'e', 'ě' to 'e', 'è' to 'e',
    'ī' to 'i', 'í' to 'i', 'ǐ' to 'i', 'ì' to 'i',
    'ō' to 'o', 'ó' to 'o', 'ǒ' to 'o', 'ò' to 'o',
    'ū' to 'u', 'ú' to 'u', 'ǔ' to 'u', 'ù' to 'u',
    'ǖ' to 'ü', 'ǘ' to 'ü', 'ǚ' to 'ü', 'ǜ' to 'ü',
    'Ā' to 'a', 'Á' to 'a', 'Ǎ' to 'a', 'À' to 'a',
    'Ē' to 'e', 'É' to 'e', 'Ě' to 'e', 'È' to 'e',
    'Ī' to 'i', 'Í' to 'i', 'Ǐ' to 'i', 'Ì' to 'i',
    'Ō' to 'o', 'Ó' to 'o', 'Ǒ' to 'o', 'Ò' to 'o',
    'Ū' to 'u', 'Ú' to 'u', 'Ǔ' to 'u', 'Ù' to 'u',
    'Ü' to 'ü',
)

/** Tone-mark variants for each base vowel, index 0 = no mark (tone 5 / neutral). */
private val TONE_MARKS: Map<Char, List<Char>> = mapOf(
    'a' to listOf('a', 'ā', 'á', 'ǎ', 'à'),
    'e' to listOf('e', 'ē', 'é', 'ě', 'è'),
    'o' to listOf('o', 'ō', 'ó', 'ǒ', 'ò'),
    'i' to listOf('i', 'ī', 'í', 'ǐ', 'ì'),
    'u' to listOf('u', 'ū', 'ú', 'ǔ', 'ù'),
    'ü' to listOf('ü', 'ǖ', 'ǘ', 'ǚ', 'ǜ'),
)

/** Strips tone diacritics and lowercases every letter (ü stays distinct from u). */
fun toneless(pinyin: String): String {
    val sb = StringBuilder(pinyin.length)
    for (ch in pinyin) {
        val base = TONE_TO_BASE[ch]
        sb.append(base ?: ch.lowercaseChar())
    }
    return sb.toString()
}

/**
 * Canonical key that collapses commonly confused initials/finals so two
 * syllable strings that differ only by one of those pairs map to the same
 * key: zh/z, ch/c, sh/s, ü/u, an/ang, en/eng, in/ing.
 */
fun confusableKey(pinyin: String): String {
    var s = toneless(pinyin)
    s = s.replace("zh", "z").replace("ch", "c").replace("sh", "s")
    s = s.replace("ü", "u")
    s = s.replace("ang", "an").replace("eng", "en").replace("ing", "in")
    return s
}

/**
 * Re-applies a tone mark to a toneless single-syllable string, following the
 * standard pinyin placement rule (a > o > e > the second vowel of iu/ui >
 * i > u > ü). tone 5 (or 0) returns the syllable unmarked. Preserves the
 * capitalization of the first letter (for proper nouns such as "Li").
 * Returns the input unchanged if no vowel is found.
 */
fun applyTone(syllableToneless: String, tone: Int): String {
    val lower = syllableToneless.lowercase()
    if (tone <= 0 || tone == 5) {
        return capitalizeLike(syllableToneless, lower)
    }
    val pos = when {
        lower.contains("iu") -> lower.indexOf("iu") + 1
        lower.contains("ui") -> lower.indexOf("ui")
        lower.contains("a") -> lower.indexOf('a')
        lower.contains("e") -> lower.indexOf('e')
        lower.contains("o") -> lower.indexOf('o')
        lower.contains("i") -> lower.indexOf('i')
        lower.contains("u") -> lower.indexOf('u')
        lower.contains("ü") -> lower.indexOf('ü')
        else -> -1
    }
    if (pos < 0) return capitalizeLike(syllableToneless, lower)
    val ch = lower[pos]
    val marks = TONE_MARKS[ch] ?: return capitalizeLike(syllableToneless, lower)
    val marked = marks.getOrElse(tone) { ch }
    val result = lower.substring(0, pos) + marked + lower.substring(pos + 1)
    return capitalizeLike(syllableToneless, result)
}

private fun capitalizeLike(original: String, candidate: String): String {
    if (original.isEmpty() || candidate.isEmpty()) return candidate
    return if (original[0].isUpperCase()) {
        candidate[0].uppercaseChar() + candidate.substring(1)
    } else {
        candidate
    }
}
