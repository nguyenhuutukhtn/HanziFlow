package com.hanziflow.ui.characters

import android.graphics.Path

/** Parses the absolute M/L/Q/C/Z subset used by Hanzi Writer paths. */
fun strokePath(svg: String): Path {
    val tokens = TOKEN.findAll(svg).map { it.value }.toList()
    var index = 0
    fun number() = tokens.getOrNull(index++)?.toFloatOrNull()
        ?: error("Expected path number at token $index")
    return Path().apply {
        while (index < tokens.size) when (tokens[index++]) {
            "M" -> moveTo(number(), number())
            "L" -> lineTo(number(), number())
            "Q" -> quadTo(number(), number(), number(), number())
            "C" -> cubicTo(number(), number(), number(), number(), number(), number())
            "Z" -> close()
            else -> error("Unsupported Hanzi Writer path command")
        }
    }
}

private val TOKEN = Regex("[MLQCZ]|[-+]?(?:\\d*\\.)?\\d+")
