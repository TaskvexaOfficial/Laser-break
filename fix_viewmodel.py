import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('''            resetRewardState()
        startGameLoop()''', '''            startGameLoop()''')

content = content.replace('''    private fun resetRewardState()
        startGameLoop() {''', '''    private fun startGameLoop() {''')


with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

