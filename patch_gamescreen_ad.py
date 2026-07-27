import re

with open("app/src/main/java/com/example/ui/GameScreen.kt", "r") as f:
    content = f.read()

content = content.replace('fun GameScreen(\n    gameViewModel: com.example.viewmodel.GameViewModel,\n    gameState: GameState,\n    gemCount: Int,\n    gemDataStore: GemDataStore,\n    onAction: (GameAction) -> Unit\n)', 'fun GameScreen(\n    gameViewModel: com.example.viewmodel.GameViewModel,\n    gameState: GameState,\n    gemCount: Int,\n    gemDataStore: GemDataStore,\n    rewardedAdManager: com.example.ads.RewardedAdManager,\n    onAction: (GameAction) -> Unit\n)')

content = content.replace('ResultOverlay(true, gemCount, gameState.roundId, gameViewModel, gemDataStore, onAction)', 'ResultOverlay(true, gemCount, gameState.roundId, gameViewModel, gemDataStore, rewardedAdManager, onAction)')
content = content.replace('ResultOverlay(false, gemCount, gameState.roundId, gameViewModel, gemDataStore, onAction)', 'ResultOverlay(false, gemCount, gameState.roundId, gameViewModel, gemDataStore, rewardedAdManager, onAction)')

with open("app/src/main/java/com/example/ui/GameScreen.kt", "w") as f:
    f.write(content)

