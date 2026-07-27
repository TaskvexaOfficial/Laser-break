import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace('GameScreen(\n                gameState = gameState,', 'GameScreen(\n                gameViewModel = gameViewModel,\n                gameState = gameState,')

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)

