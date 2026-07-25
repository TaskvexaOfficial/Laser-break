package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DiamondIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            lineTo(w * 0.95f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.95f)
            lineTo(w * 0.05f, h * 0.35f)
            close()
        }

        // Base fill
        drawPath(
            path = path,
            color = Color(0xFF00BFFF),
            style = Fill
        )

        // Top highlight
        val topPath = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            lineTo(w * 0.95f, h * 0.35f)
            lineTo(w * 0.05f, h * 0.35f)
            close()
        }
        drawPath(
            path = topPath,
            color = Color(0xFF87CEFA), // lighter
            style = Fill
        )

        // Lines for facets
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * 0.25f, h * 0.2f),
            end = Offset(w * 0.75f, h * 0.2f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * 0.25f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.95f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * 0.75f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.95f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(w * 0.05f, h * 0.35f),
            end = Offset(w * 0.95f, h * 0.35f),
            strokeWidth = 2f
        )

        // Border
        drawPath(
            path = path,
            color = Color(0xFF00FFFF),
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
