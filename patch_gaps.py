import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

old_gaps = """            val visualGap = when (difficulty) {
                Difficulty.EASY -> 16f
                Difficulty.MEDIUM -> 12f
                Difficulty.HARD -> 8f
            }"""

new_gaps = """            val visualGap = when (difficulty) {
                Difficulty.EASY -> 24f
                Difficulty.MEDIUM -> 18f
                Difficulty.HARD -> 12f // 8f laser width + 4f safety margin
            }"""

content = content.replace(old_gaps, new_gaps)

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

