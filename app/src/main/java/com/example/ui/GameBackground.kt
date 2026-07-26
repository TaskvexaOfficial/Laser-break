package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GameBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_shift"
    )

    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_offset"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00FFFF).copy(alpha = 0.8f), // Cyan
                        Color(0xFF0055FF), // Blue
                        Color(0xFF1E1B4B)  // Purple/Dark Blue
                    ),
                    start = Offset(w * bgShift, 0f), 
                    end = Offset(w, h * (0.8f + 0.2f * bgShift))
                ),
                size = size
            )

            // 5-8 small glowing particles, moving upward very slowly
            val random = java.util.Random(42) 
            for (i in 0..7) {
                val px = random.nextFloat() * size.width
                val basePy = random.nextFloat() * size.height
                val speed = random.nextFloat() * 0.3f + 0.2f
                
                // Move upward and wrap around
                val py = (basePy - (particleOffset * size.height * speed)) % size.height
                val finalPy = if (py < 0) py + size.height else py
                
                // Fade smoothly based on vertical position
                val alpha = if (finalPy < size.height * 0.2f) {
                    finalPy / (size.height * 0.2f)
                } else if (finalPy > size.height * 0.8f) {
                    (size.height - finalPy) / (size.height * 0.2f)
                } else 1f
                
                val radius = random.nextFloat() * 3f + 2f
                val color = if (random.nextBoolean()) Color(0xFF00FFFF) else Color(0xFF8A2BE2)
                
                drawCircle(
                    color = color.copy(alpha = (0.15f + random.nextFloat() * 0.2f) * alpha),
                    radius = radius,
                    center = Offset(px, finalPy)
                )
            }
        }
        
        content()
    }
}
