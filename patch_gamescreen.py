import re

with open("app/src/main/java/com/example/ui/GameScreen.kt", "r") as f:
    content = f.read()

content = content.replace('fun GameScreen(\n    gameState: GameState,', 'fun GameScreen(\n    gameViewModel: com.example.viewmodel.GameViewModel,\n    gameState: GameState,')

old_effect = """    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.WON) {
            scope.launch {
                gemDataStore.addGems(10)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } else if (gameState.status == GameStatus.LOST) {
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 200), -1)
            }
        }
    }"""

new_effect = """    LaunchedEffect(gameState.status, gameState.roundId) {
        if (gameState.status == GameStatus.WON) {
            if (gameViewModel.claimBaseWinReward(gameState.roundId)) {
                scope.launch {
                    gemDataStore.addGems(3) // Exact 3 coins
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } else if (gameState.status == GameStatus.LOST) {
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 200), -1)
            }
        }
    }"""

content = content.replace(old_effect, new_effect)

content = content.replace('GameStatus.WON -> ResultOverlay(true, gemCount, onAction)', 'GameStatus.WON -> ResultOverlay(true, gemCount, gameState.roundId, gameViewModel, gemDataStore, onAction)')
content = content.replace('GameStatus.LOST -> ResultOverlay(false, gemCount, onAction)', 'GameStatus.LOST -> ResultOverlay(false, gemCount, gameState.roundId, gameViewModel, gemDataStore, onAction)')

with open("app/src/main/java/com/example/ui/GameScreen.kt", "w") as f:
    f.write(content)

