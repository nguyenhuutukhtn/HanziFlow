package com.hanziflow.ui.explain

import com.hanziflow.ai.ExplainTarget
import com.hanziflow.shared.ReferenceLine

/** Up to [CONTEXT_LINES] Chinese lines each side of [line], taken from the same track's [lines]. */
object ExplainTargets {
    const val CONTEXT_LINES = 2

    fun build(lessonId: String, lessonVersion: Int, lines: List<ReferenceLine>, line: ReferenceLine): ExplainTarget {
        val index = lines.indexOfFirst { it.id == line.id }
        val zh = { l: ReferenceLine -> hasLineMenu(l) }
        val before = if (index < 0) emptyList() else lines.subList(0, index).filter(zh).takeLast(CONTEXT_LINES)
        val after = if (index < 0) emptyList() else lines.subList(index + 1, lines.size).filter(zh).take(CONTEXT_LINES)
        return ExplainTarget(lessonId, lessonVersion, line, before, after)
    }
}
