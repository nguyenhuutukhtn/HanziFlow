package com.hanziflow.ai

import com.hanziflow.shared.ReferenceLine
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplainPromptTest {
    private fun line(id: String, text: String) = ReferenceLine(id, "sentence", "zh", "A", text, pinyin = "p $id", translation = "nghĩa $id")

    @Test
    fun `renders line and neighbours with no placeholders left`() {
        val template = listOf(File("../../prompts/explain_line.md"), File("prompts/explain_line.md")).first { it.exists() }.readText()
        val prompt = ExplainPrompt(template)
        val out = prompt.render(
            line("l3", "大厅在楼里面，很大。"),
            before = listOf(line("l1", "前一句一。"), line("l2", "前一句二。"), line("l0", "前一句三。")).reversed().takeLast(3),
            after = listOf(line("l4", "后一句一。"), line("l5", "后一句二。"), line("l6", "后一句三。")),
        )
        assertTrue(out.contains("大厅在楼里面，很大。"))
        assertTrue(out.contains("后一句一。") && out.contains("后一句二。"))
        assertFalse(out.contains("后一句三。"))
        assertTrue(out.contains("前一句二。") || out.contains("前一句一。"))
        assertFalse(out.contains("{{"))
        assertFalse(prompt.system.contains("{{"))
        assertTrue(prompt.system.isNotBlank())
    }
}
