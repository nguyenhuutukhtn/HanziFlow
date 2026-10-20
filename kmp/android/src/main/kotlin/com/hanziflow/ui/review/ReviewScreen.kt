package com.hanziflow.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

const val ReviewScreenTestTag = "screen_review"

/** G2c placeholder: test flow + attempt history land here. Copy is the final Vietnamese copy. */
@Composable
fun ReviewScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(ReviewScreenTestTag)
            .padding(Spacing.lg),
    ) {
        Text(stringResource(R.string.review_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Spacing.xl))
        Text(
            stringResource(R.string.review_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ReviewScreenPreview() {
    HanziFlowTheme { ReviewScreen() }
}
