import re

with open("app/src/main/java/com/example/model/GameModels.kt", "r") as f:
    content = f.read()

content = content.replace('val particles: List<Particle> = emptyList()', 'val particles: List<Particle> = emptyList(),\n    val roundId: String = ""')

with open("app/src/main/java/com/example/model/GameModels.kt", "w") as f:
    f.write(content)

