package com.hanziflow.shared.characters

import kotlinx.serialization.Serializable

@Serializable
data class StrokeEntry(val strokes: List<String>, val medians: List<List<List<Double>>>)

// The downloaded file is a flat `{char: entry}` object, not wrapped under a "strokes" key.
typealias StrokePack = Map<String, StrokeEntry>
