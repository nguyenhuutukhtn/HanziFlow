package com.hanziflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hanziflow.R
import com.hanziflow.shared.modes.ModeInfo

/**
 * Display names only: internal ids stay `guided` / `practice` everywhere in data,
 * playback and persisted state (decided 2026-10-20).
 */
@Composable
fun trackDisplayName(track: String): String =
    ModeInfo.all.firstOrNull { it.id == track }?.label
        ?: stringResource(if (track == "practice") R.string.track_practice else R.string.track_guided)
