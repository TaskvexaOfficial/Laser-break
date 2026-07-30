import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    content = f.read()

target = """        // Draw Particles
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
        }"""

replacement = """        // Draw Particles
        for (p in gameState.particles) {
            val alpha = (p.life).coerceIn(0f, 1f)
            
            if (p.id == -999) { // Special flash particle
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.9f),
                    radius = 60.dp.toPx() * (1f + (1f - alpha)), // expands slightly as it fades
                    center = Offset(cx + p.x, cy + p.y)
                )
                continue
            }
            
            val shrink = if (p.id <= -1000) alpha else 1f // If it's a burst particle, shrink it as it dies
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 4.dp.toPx() * shrink,
                center = Offset(cx + p.x, cy + p.y) // translate to structure center
            )
            drawCircle(
                color = p.color.copy(alpha = alpha * 0.5f),
                radius = 12.dp.toPx() * shrink,
                center = Offset(cx + p.x, cy + p.y) // translate to structure center
            )
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
        f.write(content)
    print("Patched GameRenderer.kt for particles")
else:
    print("Target not found in GameRenderer.kt")
