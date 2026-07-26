package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val fadeOutAnim = remember { Animatable(1f) }
    val entranceAnim = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Entrance animation
        entranceAnim.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    
    LaunchedEffect(Unit) {
        delay(600)
        subtitleAlpha.animateTo(1f, animationSpec = tween(800))
    }

    LaunchedEffect(Unit) {
        delay(2500)
        fadeOutAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        onFinish()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), label = "bg"
    )

    val outerRot by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "outerRot"
    )

    val innerRot by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "innerRot"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2250, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeOutAnim.value)
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        // Animated Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00FFFF).copy(alpha = 0.5f + 0.15f * bgShift),
                        Color(0xFF0055FF).copy(alpha = 0.8f),
                        Color(0xFF0A0F24)
                    ),
                    startY = 0f,
                    endY = h * (0.8f + 0.2f * bgShift)
                ),
                size = size
            )

            // Few tiny particles
            val random = java.util.Random(123)
            for (i in 0..12) {
                val px = random.nextFloat() * w
                val py = (random.nextFloat() * h + (bgShift * 60f)) % h
                val r = random.nextFloat() * 2f + 1f
                drawCircle(
                    color = Color(0xFF00FFFF).copy(alpha = 0.2f + random.nextFloat() * 0.3f),
                    radius = r.dp.toPx(),
                    center = Offset(px, py)
                )
            }
            
            // Subtle laser streaks
            for (i in 0..3) {
                val py = random.nextFloat() * h
                drawLine(
                    color = Color(0xFF00FFFF).copy(alpha = 0.1f * entranceAnim.value),
                    start = Offset(0f, py),
                    end = Offset(w, py),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        val introScale = 0.85f + (entranceAnim.value * 0.15f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (-24).dp)
                .scale(introScale)
                .alpha(entranceAnim.value)
        ) {
            // Logo Canvas
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val rOuter = size.width * 0.40f
                    val tOuter = size.width * 0.16f

                    fun drawSegment(startAngle: Float, sweepAngle: Float, radius: Float, thickness: Float) {
                        val innerR = radius - thickness / 2
                        val outerR = radius + thickness / 2
                        val path = Path().apply {
                            arcTo(androidx.compose.ui.geometry.Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR), startAngle, sweepAngle, forceMoveTo = true)
                            arcTo(androidx.compose.ui.geometry.Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR), startAngle + sweepAngle, -sweepAngle, forceMoveTo = false)
                            close()
                        }
                        
                        // Shadow
                        val translateAmount = 4.dp.toPx()
                        withTransform({
                            translate(left = translateAmount, top = translateAmount)
                        }) {
                            drawPath(path, Color.Black.copy(alpha = 0.4f), style = Fill)
                        }
                        
                        // Fill
                        val midA = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                        val gStartX = cx + innerR * kotlin.math.cos(midA).toFloat()
                        val gStartY = cy + innerR * kotlin.math.sin(midA).toFloat()
                        val gEndX = cx + outerR * kotlin.math.cos(midA).toFloat()
                        val gEndY = cy + outerR * kotlin.math.sin(midA).toFloat()
                        
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE0FFFF), Color(0xFF00FFFF), Color(0xFF0055FF)),
                                start = Offset(gStartX, gStartY), end = Offset(gEndX, gEndY)
                            ),
                            style = Fill
                        )
                        
                        // Inner border
                        val innerPath = Path().apply {
                            arcTo(androidx.compose.ui.geometry.Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR), startAngle, sweepAngle, forceMoveTo = true)
                        }
                        drawPath(innerPath, Color.White.copy(alpha = 0.7f), style = Stroke(width = 3f, cap = StrokeCap.Round))
                        
                        // Outer border highlight
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(gStartX, gStartY),
                            end = Offset((gStartX + gEndX)/2 + 5f, (gStartY + gEndY)/2 - 5f),
                            strokeWidth = 2f
                        )
                    }

                    rotate(outerRot, Offset(cx, cy)) {
                        drawSegment(0f, 100f, rOuter, tOuter)
                        drawSegment(110f, 80f, rOuter, tOuter)
                        drawSegment(200f, 150f, rOuter, tOuter)
                    }

                    // Inner static/opposite rotating details
                    rotate(innerRot, Offset(cx, cy)) {
                        drawArc(
                            color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                            startAngle = 0f, sweepAngle = 160f, useCenter = false,
                            topLeft = Offset(cx - size.width * 0.22f, cy - size.width * 0.22f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.width * 0.44f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                            startAngle = 180f, sweepAngle = 160f, useCenter = false,
                            topLeft = Offset(cx - size.width * 0.22f, cy - size.width * 0.22f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.width * 0.44f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Center glow
                    val orbR = (size.width * 0.12f) * pulse
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.8f), Color(0xFF00FFFF).copy(alpha = 0.6f), Color(0xFF0055FF).copy(alpha = 0.2f), Color.Transparent),
                            center = Offset(cx, cy), radius = orbR * 3f
                        ),
                        radius = orbR * 3f, center = Offset(cx, cy)
                    )
                    drawCircle(Color(0xFFE0FFFF), orbR, Offset(cx, cy))
                    drawCircle(Color.White, orbR * 0.5f, Offset(cx, cy))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LASER",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF0055FF).copy(alpha = 0.5f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = "BREAK",
                    color = Color(0xFF00FFFF),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF0055FF).copy(alpha = 0.8f),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    ),
                    modifier = Modifier.offset(y = (-14).dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "A TaskVexa Game",
                color = Color(0xFFE0FFFF).copy(alpha = subtitleAlpha.value),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}

