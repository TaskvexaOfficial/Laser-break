import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    content = f.read()

target = """                        // If health is low, draw some crack lines (simple lines for now)
                        if (!isDanger && segment.health < 50f) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(gradStartX, gradStartY),
                                end = Offset((gradStartX + gradEndX) / 2 + 10f, (gradStartY + gradEndY) / 2 - 10f),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }"""

replacement = """                        // Progressive damage cracks
                        if (!isDanger && segment.health < 100f && segment.health > 0f) {
                            val rng = java.util.Random(segment.id.toLong())
                            
                            val isHeavyDamage = segment.health < 35f
                            val isMediumDamage = segment.health < 75f
                            
                            val shakeX = if (isHeavyDamage && gameState.isFiring) (rng.nextFloat() * 4f - 2f) else 0f
                            val shakeY = if (isHeavyDamage && gameState.isFiring) (rng.nextFloat() * 4f - 2f) else 0f
                            
                            androidx.compose.ui.graphics.drawscope.withTransform({
                                translate(left = shakeX, top = shakeY)
                                clipPath(path)
                            }) {
                                val numCracks = if (isHeavyDamage) 4 else if (isMediumDamage) 3 else 1
                                val glowAlpha = if (isHeavyDamage) 0.3f else 0f
                                
                                if (glowAlpha > 0f) {
                                    drawPath(
                                        path = path,
                                        color = structure.colorTheme.safeColorGlow.copy(alpha = glowAlpha),
                                        style = Fill
                                    )
                                }
                                
                                for (i in 0 until numCracks) {
                                    var currentX = gradStartX + (rng.nextFloat() * 20f - 10f)
                                    var currentY = gradStartY + (rng.nextFloat() * 20f - 10f)
                                    
                                    val segmentsInCrack = rng.nextInt(3) + 2
                                    for (j in 0 until segmentsInCrack) {
                                        val nextX = currentX + (rng.nextFloat() * 60f - 30f)
                                        val nextY = currentY + (rng.nextFloat() * 60f - 30f)
                                        
                                        if (isHeavyDamage) {
                                            drawLine(
                                                color = structure.colorTheme.safeColorGlow,
                                                start = Offset(currentX, currentY),
                                                end = Offset(nextX, nextY),
                                                strokeWidth = 8f,
                                                cap = StrokeCap.Round,
                                                blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                                            )
                                        }
                                        
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.9f),
                                            start = Offset(currentX, currentY),
                                            end = Offset(nextX, nextY),
                                            strokeWidth = 3f,
                                            cap = StrokeCap.Round
                                        )
                                        
                                        currentX = nextX
                                        currentY = nextY
                                    }
                                }
                            }
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
        f.write(content)
    print("Patched GameRenderer.kt")
else:
    print("Target not found in GameRenderer.kt")
