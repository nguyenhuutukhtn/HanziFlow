package com.hanziflow.ui.characters

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.hanziflow.shared.characters.StrokeEntry

@Composable
fun StrokeAnimation(entry: StrokeEntry, modifier: Modifier = Modifier) {
    val paths = remember(entry.strokes) { entry.strokes.map(::strokePath) }
    val progress = remember(paths) { paths.map { Animatable(0f) } }
    var playing by remember { mutableStateOf(true) }
    var replay by remember { mutableStateOf(0) }
    LaunchedEffect(playing, replay) {
        if (replay > 0) progress.forEach { it.snapTo(0f) }
        if (playing) progress.forEach { if (it.value < 1f) it.animateTo(1f, tween(500)) }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val scale = size.minDimension / 1024f
            val offsetX = (size.width - 1024f * scale) / 2
            val offsetY = (size.height - 1024f * scale) / 2
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.save()
                native.translate(offsetX, offsetY)
                native.scale(scale, scale)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.Black.toArgb() }
                paths.forEachIndexed { index, path ->
                    when {
                        progress[index].value >= 1f -> native.drawPath(path, paint)
                        progress[index].value > 0f -> native.drawPath(path.segment(progress[index].value), paint)
                    }
                }
                native.restore()
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { playing = !playing }) { Text(if (playing) "Tạm dừng" else "Phát") }
            Button(onClick = {
                playing = true
                replay++
            }) { Text("Phát lại") }
        }
    }
}

private fun Path.segment(progress: Float): Path = Path().also { destination ->
    val measure = PathMeasure(this, false)
    measure.getSegment(0f, measure.length * progress, destination, true)
}
