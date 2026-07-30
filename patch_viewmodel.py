import sys
import re

with open('app/src/main/java/com/example/viewmodel/GameViewModel.kt', 'r') as f:
    content = f.read()

# 1. Update Particle mapping logic to include shrinking/slowing
target1 = """        // Update particles
        val newParticles = state.particles.map { it.copy(
            x = it.x + it.vx * dt,
            y = it.y + it.vy * dt,
            life = it.life - dt * 2f
        ) }.filter { it.life > 0 }.toMutableList()"""

replacement1 = """        // Update particles
        val newParticles = state.particles.map { it.copy(
            x = it.x + it.vx * dt,
            y = it.y + it.vy * dt,
            vx = it.vx * kotlin.math.max(1f - 2.5f * dt, 0f),
            vy = it.vy * kotlin.math.max(1f - 2.5f * dt, 0f),
            life = it.life - dt * 2f
        ) }.filter { it.life > 0 }.toMutableList()"""

# 2. Add progressive damage and burst
target2 = """                    if (segment.isDangerous) {
                        gameOver = true
                    } else {
                        // Damage segment
                        segment.health -= 250f * dt // Takes ~0.4s to break
                        if (segment.health <= 0) {
                            segment.isDestroyed = true
                        }
                    }"""

replacement2 = """                    if (segment.isDangerous) {
                        gameOver = true
                    } else {
                        // Damage segment
                        if (segment.health > 0f) {
                            segment.health -= 250f * dt // Takes ~0.4s to break
                            if (segment.health <= 0f) {
                                segment.isDestroyed = true
                                
                                // One final bright impact flash
                                newParticles.add(Particle(
                                    id = -999,
                                    x = 0f,
                                    y = hitRadius,
                                    vx = 0f,
                                    vy = 0f,
                                    life = 1f,
                                    color = structure.colorTheme.safeColorGlow
                                ))
                                
                                // Burst of 14-20 fragments
                                val burstColor = structure.colorTheme.safeColorGlow
                                val numBurstParticles = (14..20).random()
                                for (p in 0 until numBurstParticles) {
                                    if (newParticles.size < 60) {
                                        val angle = kotlin.random.Random.nextFloat() * 2 * Math.PI
                                        val speed = kotlin.random.Random.nextFloat() * 250f + 100f
                                        newParticles.add(Particle(
                                            id = -1000 - kotlin.random.Random.nextInt(1000), // indicate burst particle
                                            x = 0f, 
                                            y = hitRadius,
                                            vx = (Math.cos(angle) * speed).toFloat(),
                                            vy = (Math.sin(angle) * speed).toFloat(),
                                            life = 1f + kotlin.random.Random.nextFloat() * 0.5f,
                                            color = burstColor
                                        ))
                                    }
                                }
                            }
                        }
                    }"""

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Patched GameViewModel.kt target1")
else:
    print("Target 1 not found")

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Patched GameViewModel.kt target2")
else:
    print("Target 2 not found")

with open('app/src/main/java/com/example/viewmodel/GameViewModel.kt', 'w') as f:
    f.write(content)
