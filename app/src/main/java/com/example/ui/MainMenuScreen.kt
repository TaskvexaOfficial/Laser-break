package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.model.GameStatus
import com.example.model.GameState
import com.example.model.Layer
import com.example.model.Segment
import com.example.model.Structure
import com.example.model.StructureType
import com.example.model.ThemeBlue

@Composable
fun MainMenuScreen(
    gemCount: Int,
    soundEnabled: Boolean = true,
    onSoundToggle: (Boolean) -> Unit = {},
    onPlayClick: () -> Unit
) {
    // soundEnabled handled by param
    var isPlayPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    GameBackground {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .border(1.dp, Color(0xFF00FFFF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiamondIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = gemCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            IconButton(
                onClick = { onSoundToggle(!soundEnabled) },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                    .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), CircleShape)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = Color.White
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val coreSize = (maxWidth * 0.45f).coerceIn(145.dp, 170.dp)
                AnimatedMenuCore(
                    modifier = Modifier.size(coreSize),
                    isPlaying = isPlayPressed
                )
            }

            Text(
                text = "LASER",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF000080),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 8f
                    )
                )
            )
            Text(
                text = "BREAK",
                color = Color(0xFF00FFFF),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.offset(y = (-16).dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF000080),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 8f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val playButtonTransition = rememberInfiniteTransition(label = "play_btn")
            val playScale by playButtonTransition.animateFloat(
                initialValue = 1f, targetValue = 1.015f,
                animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "play_scale"
            )
            val playAlpha by playButtonTransition.animateFloat(
                initialValue = 0.85f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "play_alpha"
            )
            
            Button(
                onClick = {
                    if (!isPlayPressed) {
                        isPlayPressed = true
                        coroutineScope.launch {
                            delay(250)
                            onPlayClick()
                            isPlayPressed = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .width(240.dp)
                    .height(72.dp)
                    .scale(playScale)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00FFFF).copy(alpha = playAlpha))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF00FFFF).copy(alpha = playAlpha), Color(0xFF0055FF).copy(alpha = playAlpha))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(2.dp, Color(0xFFE0FFFF).copy(alpha = playAlpha), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Hold to break. Release to stay safe.",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == 1) 12.dp else 10.dp)
                            .background(
                                if (i == 1) Color(0xFFFF4444) else Color.White.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .shadow(if (i == 1) 8.dp else 0.dp, spotColor = Color.Red)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A TaskVexa Game",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnimatedMenuCore(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core")
    
    val outerRot by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)), label = "outer"
    )
    val innerRot by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "inner"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2250, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )

    val playAnim = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            playAnim.animateTo(1f, animationSpec = tween(250, easing = LinearOutSlowInEasing))
        } else {
            playAnim.snapTo(0f)
        }
    }
    val playProgress = playAnim.value

    Canvas(modifier = modifier.offset(y = floatOffset.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        
        val rOuter = size.width * 0.40f
        val tOuter = size.width * 0.16f

        fun drawSegment(
            startAngle: Float, sweepAngle: Float,
            radius: Float, thickness: Float
        ) {
            val innerR = radius - thickness / 2
            val outerR = radius + thickness / 2
            
            val path = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                    startAngleDegrees = startAngle, sweepAngleDegrees = sweepAngle, forceMoveTo = true
                )
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR),
                    startAngleDegrees = startAngle + sweepAngle, sweepAngleDegrees = -sweepAngle, forceMoveTo = false
                )
                close()
            }
            
            val translateAmount = 4.dp.toPx()
            withTransform({
                translate(left = translateAmount, top = translateAmount)
            }) {
                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = 0.4f),
                    style = Fill
                )
            }
            
            val midA = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val gStartX = cx + innerR * kotlin.math.cos(midA).toFloat()
            val gStartY = cy + innerR * kotlin.math.sin(midA).toFloat()
            val gEndX = cx + outerR * kotlin.math.cos(midA).toFloat()
            val gEndY = cy + outerR * kotlin.math.sin(midA).toFloat()
            
            val colors = listOf(Color(0xFFE0FFFF), Color(0xFF00FFFF), Color(0xFF0055FF))
            
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(gStartX, gStartY),
                    end = Offset(gEndX, gEndY)
                ),
                style = Fill
            )
            
            val innerPath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR),
                    startAngleDegrees = startAngle, sweepAngleDegrees = sweepAngle, forceMoveTo = true
                )
            }
            drawPath(
                path = innerPath,
                color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            drawPath(
                path = path,
                color = Color(0xFF87CEFA).copy(alpha = 0.5f),
                style = Stroke(width = 2f)
            )
            
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(gStartX, gStartY),
                end = Offset((gStartX + gEndX)/2 + 5f, (gStartY + gEndY)/2 - 5f),
                strokeWidth = 2f
            )
        }
        
        val extraOuter = playProgress * 45f

        rotate(outerRot + extraOuter, Offset(cx, cy)) {
            drawSegment(0f, 100f, rOuter, tOuter)
            drawSegment(110f, 80f, rOuter, tOuter)
            drawSegment(200f, 150f, rOuter, tOuter)
        }
        
        if (playProgress > 0) {
            drawCircle(
                color = Color.White.copy(alpha = 1f - playProgress),
                radius = rOuter * playProgress * 2f,
                center = Offset(cx, cy),
                style = Stroke(width = 16.dp.toPx())
            )
        }

        val currentPulse = pulse + (playProgress * 0.5f)
        
        rotate(innerRot, Offset(cx, cy)) {
            drawArc(
                color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                startAngle = 0f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - size.width * 0.22f, cy - size.width * 0.22f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.width * 0.44f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                startAngle = 180f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - size.width * 0.22f, cy - size.width * 0.22f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.width * 0.44f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        val orbR = (size.width * 0.12f) * currentPulse
        val glowColor = if (playProgress > 0) Color.White else Color(0xFF00FFFF)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    glowColor.copy(alpha = 0.6f),
                    Color(0xFF0055FF).copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = orbR * 3f
            ),
            radius = orbR * 3f,
            center = Offset(cx, cy)
        )
        
        drawCircle(
            color = Color(0xFFE0FFFF),
            radius = orbR,
            center = Offset(cx, cy)
        )
        
        drawCircle(
            color = Color.White,
            radius = orbR * 0.5f,
            center = Offset(cx, cy)
        )
    }
}
