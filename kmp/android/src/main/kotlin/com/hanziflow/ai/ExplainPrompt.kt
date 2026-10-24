package com.hanziflow.ai

import android.content.Context
import com.hanziflow.shared.ReferenceLine

/** Renders prompts/explain_line.md (copied into assets/prompts/ at build time). */
class ExplainPrompt(template: String) {
    val system: String
    private val userTemplate: String

    init {
        val parts = template.substringAfter("# System").split("---USER---", limit = 2)
        system = parts[0].trim()
        userTemplate = parts.getOrElse(1) { "" }.trim()
    }

    fun render(line: ReferenceLine, before: List<ReferenceLine>, after: List<ReferenceLine>): String =
        userTemplate
            .replace("{{hanzi}}", line.text)
            .replace("{{pinyin}}", line.pinyin.orEmpty())
            .replace("{{meaning_vi}}", line.translation.orEmpty())
            .replace("{{context_before}}", joined(before.takeLast(2)))
            .replace("{{context_after}}", joined(after.take(2)))

    private fun joined(lines: List<ReferenceLine>) = lines.joinToString(" ").ifBlank { "(không có)" }

    companion object {
        const val ASSET = "prompts/explain_line.md"

        fun fromAssets(context: Context) =
            ExplainPrompt(context.assets.open(ASSET).bufferedReader().use { it.readText() })
    }
}
