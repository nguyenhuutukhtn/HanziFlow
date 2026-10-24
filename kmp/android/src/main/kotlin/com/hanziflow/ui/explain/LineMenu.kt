package com.hanziflow.ui.explain

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.hanziflow.R
import com.hanziflow.shared.ReferenceLine

/** Any row with Chinese text in the data; Vietnamese narration rows have no menu. */
fun hasLineMenu(line: ReferenceLine): Boolean = !(line.language == "vi" || line.language.startsWith("vi-"))

/**
 * Wraps one transcript row and anchors the "Giải thích / Sao chép" menu to it.
 * [content] receives the long-press callback (null when the row has no menu).
 */
@Composable
fun LineMenuAnchor(line: ReferenceLine, onExplain: () -> Unit, content: @Composable (onLongClick: (() -> Unit)?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copied = stringResource(R.string.line_copied)
    Box {
        content(if (hasLineMenu(line)) ({ open = true }) else null)
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.line_menu_explain)) },
                onClick = { open = false; onExplain() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.line_menu_copy)) },
                onClick = {
                    open = false
                    clipboard.setText(AnnotatedString(line.text))
                    Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
