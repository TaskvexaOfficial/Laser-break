import sys

with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'r') as f:
    content = f.read()

target = """                            val shakeX = if (isHeavyDamage && gameState.isFiring) (rng.nextFloat() * 4f - 2f) else 0f
                            val shakeY = if (isHeavyDamage && gameState.isFiring) (rng.nextFloat() * 4f - 2f) else 0f"""

replacement = """                            val shakeX = if (isHeavyDamage && gameState.isFiring) (kotlin.random.Random.nextFloat() * 4f - 2f) else 0f
                            val shakeY = if (isHeavyDamage && gameState.isFiring) (kotlin.random.Random.nextFloat() * 4f - 2f) else 0f"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/GameRenderer.kt', 'w') as f:
        f.write(content)
    print("Patched GameRenderer.kt for vibration")
else:
    print("Target not found in GameRenderer.kt")
