package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun GameBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00FFFF), // Bright cyan at the top
                        Color(0xFF0055FF), // Electric blue in the middle
                        Color(0xFF1E1B4B)  // Deep blue/purple at the bottom
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Subtle circular energy lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2 - 100f
            
            // Radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00BFFF).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(cx, cy)
            )

            // Rings
            for (i in 1..4) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = i * 150f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )
            }
            
            // Few random particles in background
            val random = java.util.Random(42) // Fixed seed for stable background
            for (i in 0..20) {
                val px = random.nextFloat() * size.width
                val py = random.nextFloat() * size.height
                val radius = random.nextFloat() * 4f + 2f
                val color = if (random.nextBoolean()) Color(0xFF00FFFF) else Color(0xFFFF69B4)
                drawCircle(
                    color = color.copy(alpha = 0.3f + random.nextFloat() * 0.4f),
                    radius = radius,
                    center = Offset(px, py)
                )
            }
        }
        
        content()
    }
}
