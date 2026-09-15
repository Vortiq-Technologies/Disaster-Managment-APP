package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MountainLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    mountainColor: Color = Color(0xFF1B6B48),
    snowColor: Color = Color.White
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Left Peak
        val leftPeak = Path().apply {
            moveTo(w * 0.08f, h * 0.92f)
            lineTo(w * 0.28f, h * 0.38f)
            lineTo(w * 0.48f, h * 0.92f)
            close()
        }
        drawPath(leftPeak, color = mountainColor)

        // Right Peak
        val rightPeak = Path().apply {
            moveTo(w * 0.52f, h * 0.92f)
            lineTo(w * 0.72f, h * 0.38f)
            lineTo(w * 0.92f, h * 0.92f)
            close()
        }
        drawPath(rightPeak, color = mountainColor)

        // Middle Tall Peak (in foreground)
        val centerPeak = Path().apply {
            moveTo(w * 0.22f, h * 0.92f)
            lineTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.78f, h * 0.92f)
            close()
        }
        drawPath(centerPeak, color = mountainColor)

        // Left Snow Cap
        val leftSnow = Path().apply {
            moveTo(w * 0.28f, h * 0.38f)
            lineTo(w * 0.34f, h * 0.54f)
            lineTo(w * 0.31f, h * 0.52f)
            lineTo(w * 0.28f, h * 0.56f)
            lineTo(w * 0.25f, h * 0.52f)
            lineTo(w * 0.22f, h * 0.54f)
            close()
        }
        drawPath(leftSnow, color = snowColor)

        // Right Snow Cap
        val rightSnow = Path().apply {
            moveTo(w * 0.72f, h * 0.38f)
            lineTo(w * 0.78f, h * 0.54f)
            lineTo(w * 0.75f, h * 0.52f)
            lineTo(w * 0.72f, h * 0.56f)
            lineTo(w * 0.69f, h * 0.52f)
            lineTo(w * 0.66f, h * 0.54f)
            close()
        }
        drawPath(rightSnow, color = snowColor)

        // Center Tall Snow Cap
        val centerSnow = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.59f, h * 0.38f)
            lineTo(w * 0.55f, h * 0.34f)
            lineTo(w * 0.52f, h * 0.40f)
            lineTo(w * 0.48f, h * 0.35f)
            lineTo(w * 0.45f, h * 0.39f)
            lineTo(w * 0.41f, h * 0.38f)
            close()
        }
        drawPath(centerSnow, color = snowColor)
    }
}
