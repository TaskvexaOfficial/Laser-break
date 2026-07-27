import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('import kotlin.random.Random', 'import kotlin.random.Random\nimport java.util.UUID')

old_code = """    fun startNewGame() {
        val structure = generateRandomStructure()
        _gameState.update {
            it.copy(
                status = GameStatus.PLAYING,
                structure = structure,
                isFiring = false,
                particles = emptyList()
            )
        }
        startGameLoop()
    }"""

new_code = """    fun startNewGame() {
        val structure = generateRandomStructure()
        _gameState.update {
            it.copy(
                status = GameStatus.PLAYING,
                structure = structure,
                isFiring = false,
                particles = emptyList(),
                roundId = java.util.UUID.randomUUID().toString()
            )
        }
        startGameLoop()
    }"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

