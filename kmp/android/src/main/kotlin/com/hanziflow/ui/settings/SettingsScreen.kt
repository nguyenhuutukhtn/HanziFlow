package com.hanziflow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hanziflow.R
import com.hanziflow.ui.theme.HanziFlowTheme
import com.hanziflow.ui.theme.Spacing

const val SettingsScreenTestTag = "screen_settings"

/**
 * G2b placeholder. The rows use the final Vietnamese copy but have no controls yet;
 * theme, transcript defaults, Vietnamese, Wi-Fi-only and account settings get wired
 * in the Settings task.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(SettingsScreenTestTag)
            .padding(Spacing.lg),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Spacing.xl))
        Text(
            stringResource(R.string.settings_section_display),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.sm))
        PlaceholderRow(stringResource(R.string.settings_default_view_guided))
        PlaceholderRow(stringResource(R.string.settings_default_view_practice))
        PlaceholderRow(stringResource(R.string.settings_show_vietnamese))
        PlaceholderRow(stringResource(R.string.settings_dark_theme))
    }
}

@Composable
private fun PlaceholderRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    HanziFlowTheme { SettingsScreen() }
}
