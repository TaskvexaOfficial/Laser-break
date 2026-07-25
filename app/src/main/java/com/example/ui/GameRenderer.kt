package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.example.model.GameState
import com.example.model.StructureType

@Composable
fun GameRenderer(gameState: GameState, isMenu: Boolean = false) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2 + gameState.shakeOffsetX
        // If it's the menu, center the structure exactly in the box.
        val cy = if (isMenu) size.height / 2 else (size.height / 2 - 100f + gameState.shakeOffsetY)
        
        val cannonY = size.height - 64.dp.toPx() // approximate cannon center
        
        gameState.structure?.let { structure ->
            // Draw layers
            for (layer in structure.layers.reversed()) { // draw from inner to outer or vice versa
                rotate(degrees = layer.currentRotation, pivot = Offset(cx, cy)) {
                    for (segment in layer.segments) {
                        if (segment.isDestroyed) continue
                        
                        // New Segmented Solid Style
                        val isDanger = segment.isDangerous
                        
                        // Base colors
                        val baseColor = if (isDanger) Color(0xFF1E293B) else structure.colorTheme.safeColorPrimary
                        val topColor = if (isDanger) Color(0xFF334155) else structure.colorTheme.safeColorGlow
                        val darkEdgeColor = if (isDanger) Color(0xFF0F172A) else structure.colorTheme.safeColorSecondary

                        val startRad = Math.toRadians(segment.startAngle.toDouble())
                        val endRad = Math.toRadians((segment.startAngle + segment.sweepAngle).toDouble())
                        
                        val innerRadius = layer.radius - layer.thickness / 2
                        val outerRadius = layer.radius + layer.thickness / 2

                        // Create Path for the segment
                        val path = Path().apply {
                            if (structure.type == StructureType.SEGMENTED_CIRCLE) {
                                // Inner arc
                                arcTo(
                                    rect = androidx.compose.ui.geometry.Rect(
                                        cx - innerRadius, cy - innerRadius,
                                        cx + innerRadius, cy + innerRadius
                                    ),
                                    startAngleDegrees = segment.startAngle,
                                    sweepAngleDegrees = segment.sweepAngle,
                                    forceMoveTo = true
                                )
                                // Outer arc
                                arcTo(
                                    rect = androidx.compose.ui.geometry.Rect(
                                        cx - outerRadius, cy - outerRadius,
                                        cx + outerRadius, cy + outerRadius
                                    ),
                                    startAngleDegrees = segment.startAngle + segment.sweepAngle,
                                    sweepAngleDegrees = -segment.sweepAngle,
                                    forceMoveTo = false
                                )
                                close()
                            } else {
                                // Draw as a straight thick line (polygon segment)
                                val startXInner = cx + innerRadius * Math.cos(startRad).toFloat()
                                val startYInner = cy + innerRadius * Math.sin(startRad).toFloat()
                                val endXInner = cx + innerRadius * Math.cos(endRad).toFloat()
                                val endYInner = cy + innerRadius * Math.sin(endRad).toFloat()
                                
                                val startXOuter = cx + outerRadius * Math.cos(startRad).toFloat()
                                val startYOuter = cy + outerRadius * Math.sin(startRad).toFloat()
                                val endXOuter = cx + outerRadius * Math.cos(endRad).toFloat()
                                val endYOuter = cy + outerRadius * Math.sin(endRad).toFloat()
                                
                                moveTo(startXInner, startYInner)
                                lineTo(endXInner, endYInner)
                                lineTo(endXOuter, endYOuter)
                                lineTo(startXOuter, startYOuter)
                                close()
                            }
                        }
                        
                        // Draw shadow
                        drawPath(
                            path = path,
                            color = Color.Black.copy(alpha = 0.5f),
                            style = Fill,
                            blendMode = androidx.compose.ui.graphics.BlendMode.Darken
                        )
                        // Translate path slightly for shadow effect - wait, it's easier to just draw a slightly larger/offset path or just use gradient for 3D effect.
                        
                        // Gradient fill for 3D block look
                        val segmentCenterRad = Math.toRadians((segment.startAngle + segment.sweepAngle / 2).toDouble())
                        val gradStartX = cx + innerRadius * Math.cos(segmentCenterRad).toFloat()
                        val gradStartY = cy + innerRadius * Math.sin(segmentCenterRad).toFloat()
                        val gradEndX = cx + outerRadius * Math.cos(segmentCenterRad).toFloat()
                        val gradEndY = cy + outerRadius * Math.sin(segmentCenterRad).toFloat()

                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(topColor, baseColor, darkEdgeColor),
                                start = Offset(gradStartX, gradStartY),
                                end = Offset(gradEndX, gradEndY)
                            ),
                            style = Fill
                        )
                        
                        // Highlight border
                        drawPath(
                            path = path,
                            color = if (isDanger) Color(0xFF475569) else Color.White.copy(alpha = 0.5f),
                            style = Stroke(width = 2f)
                        )
                        
                        // If health is low, draw some crack lines (simple lines for now)
                        if (!isDanger && segment.health < 50f) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(gradStartX, gradStartY),
                                end = Offset((gradStartX + gradEndX) / 2 + 10f, (gradStartY + gradEndY) / 2 - 10f),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // Draw Core
            drawCircle(
                color = Color.White,
                radius = 24f,
                center = Offset(cx, cy)
            )
            // Core Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, structure.colorTheme.safeColorGlow, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 80f
                ),
                radius = 80f,
                center = Offset(cx, cy)
            )
            // Core rays/sparks if firing
            if (gameState.isFiring) {
                for (i in 0..7) {
                    val angle = Math.toRadians((i * 45 + (System.currentTimeMillis() % 360)).toDouble())
                    val rx = cx + 40f * Math.cos(angle).toFloat()
                    val ry = cy + 40f * Math.sin(angle).toFloat()
                    drawLine(
                        color = structure.colorTheme.safeColorGlow,
                        start = Offset(cx, cy),
                        end = Offset(rx, ry),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        
        // Draw Laser and Cannon only if not in Menu
        if (!isMenu) {
            // Draw Laser Beam
            if (gameState.isFiring) {
                val laserTopY = if (gameState.laserTipY > 0) cy + gameState.laserTipY else cy
                
                // Beam glow
                drawLine(
                    color = Color(0xFFFF1493).copy(alpha = 0.5f), // Pinkish glow
                    start = Offset(cx, cannonY),
                    end = Offset(cx, laserTopY),
                    strokeWidth = 24.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Main beam
                drawLine(
                    color = Color.White,
                    start = Offset(cx, cannonY),
                    end = Offset(cx, laserTopY),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Impact point
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = Offset(cx, laserTopY)
                )
                drawCircle(
                    color = Color(0xFFFF1493).copy(alpha = 0.8f),
                    radius = 32f,
                    center = Offset(cx, laserTopY)
                )
            }
            
            // Draw Cannon Base (over the laser)
            val baseRadius = 40.dp.toPx()
            
            // Under glow
            drawCircle(
                color = Color(0xFF00BFFF).copy(alpha = 0.3f),
                radius = baseRadius * 1.5f,
                center = Offset(cx, cannonY)
            )
            
            // Outer body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0055FF), Color(0xFF0F172A)),
                    center = Offset(cx, cannonY),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = Offset(cx, cannonY)
            )
            // Inner metallic ring
            drawCircle(
                color = Color(0xFF334155),
                radius = baseRadius * 0.7f,
                center = Offset(cx, cannonY),
                style = Stroke(width = 8.dp.toPx())
            )
            // Lens glow
            drawCircle(
                color = Color(0xFFFF4444), // Red lens
                radius = baseRadius * 0.4f,
                center = Offset(cx, cannonY)
            )
            // Nozzle
            val nozzlePath = Path().apply {
                moveTo(cx - 16f, cannonY - baseRadius * 0.8f)
                lineTo(cx + 16f, cannonY - baseRadius * 0.8f)
                lineTo(cx + 10f, cannonY - baseRadius * 1.2f)
                lineTo(cx - 10f, cannonY - baseRadius * 1.2f)
                close()
            }
            drawPath(
                path = nozzlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BFFF), Color(0xFF1E293B)),
                    startY = cannonY - baseRadius * 1.2f,
                    endY = cannonY - baseRadius * 0.8f
                )
            )
        }
        
        // Draw Particles
        for (p in gameState.particles) {
            val alpha = (p.life).coerceIn(0f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 4.dp.toPx() * alpha,
                center = Offset(cx + p.x, cy + p.y) // translate to structure center
            )
            drawCircle(
                color = p.color.copy(alpha = alpha * 0.5f),
                radius = 12.dp.toPx() * alpha,
                center = Offset(cx + p.x, cy + p.y) // translate to structure center
            )
        }
    }
}


