import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Replacements
    content = content.replace(
        "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.GlobalScope\nimport kotlinx.coroutines.DelicateCoroutinesApi\nimport com.example.viewmodel.GameViewModel",
        "import kotlinx.coroutines.launch\nimport com.example.viewmodel.GameViewModel"
    )
    
    content = content.replace(
        "@OptIn(DelicateCoroutinesApi::class)\n@Composable\nfun ResultOverlay(",
        "@Composable\nfun ResultOverlay("
    )
    
    content = content.replace(
        """                                        onRewardEarned = {
                                            val newCount = gameViewModel.record3XAdCompletion(roundId)
                                            completedAds = newCount
                                            if (newCount >= 3 && gameViewModel.claim3XBonusReward(roundId)) {
                                                GlobalScope.launch { gemDataStore.addGems(9) }
                                                Toast.makeText(context, "3X Reward Claimed! +9 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },""",
        """                                        onRewardEarned = {
                                            val newCount = gameViewModel.record3XAdCompletion(roundId)
                                            completedAds = newCount
                                            if (newCount >= 3 && gameViewModel.claim3XBonusReward(roundId)) {
                                                scope.launch { gemDataStore.addGems(9) }
                                                Toast.makeText(context, "3X Reward Claimed! +9 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },"""
    )
    
    content = content.replace(
        """                        Button(
                            onClick = { 
                                GlobalScope.launch {
                                    if (gameViewModel.claimBaseWinReward(roundId)) {
                                        gemDataStore.addGems(3)
                                    }
                                }
                                onAction(GameAction.PlayAgain)
                            },""",
        """                        Button(
                            onClick = { 
                                scope.launch {
                                    if (gameViewModel.claimBaseWinReward(roundId)) {
                                        gemDataStore.addGems(3)
                                    }
                                    onAction(GameAction.PlayAgain)
                                }
                            },"""
    )
    
    content = content.replace(
        """                                        onRewardEarned = {
                                            if (gameViewModel.claimLossAdReward(roundId)) {
                                                isLossClaimed = true
                                                GlobalScope.launch { gemDataStore.addGems(3) }
                                                Toast.makeText(context, "Reward Claimed! +3 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },""",
        """                                        onRewardEarned = {
                                            if (gameViewModel.claimLossAdReward(roundId)) {
                                                isLossClaimed = true
                                                scope.launch { gemDataStore.addGems(3) }
                                                Toast.makeText(context, "Reward Claimed! +3 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },"""
    )
    
    content = content.replace(
        """                    TextButton(
                        onClick = { 
                             if (isWin) {
                                 GlobalScope.launch {
                                     if (gameViewModel.claimBaseWinReward(roundId)) {
                                         gemDataStore.addGems(3)
                                     }
                                 }
                             }
                             onAction(GameAction.Home)
                         }
                    )""",
        """                    TextButton(
                        onClick = { 
                             if (isWin) {
                                 scope.launch {
                                     if (gameViewModel.claimBaseWinReward(roundId)) {
                                         gemDataStore.addGems(3)
                                     }
                                     onAction(GameAction.Home)
                                 }
                             } else {
                                 onAction(GameAction.Home)
                             }
                         }
                    )"""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/Overlays.kt')
