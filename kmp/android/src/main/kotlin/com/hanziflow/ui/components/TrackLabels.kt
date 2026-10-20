package com.hanziflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hanziflow.R

/**
 * Display names only: internal ids stay `guided` / `practice` everywhere in data,
 * playback and persisted state (decided 2026-10-20).
 */
@Composable
fun trackDisplayName(track: String): String =
    stringResource(if (track == "practice") R.string.track_practice else R.string.track_guided)
