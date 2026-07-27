import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('fun claimLossAdReward(roundId: String): Boolean {', 'fun hasClaimedLossReward(roundId: String): Boolean {\n        return lossAdRewardCreditedRoundId == roundId\n    }\n\n    fun claimLossAdReward(roundId: String): Boolean {')

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/Overlays.kt", "r") as f:
    content = f.read()

content = content.replace('var isLossClaimed by remember { mutableStateOf(false) }', 'var isLossClaimed by remember { mutableStateOf(gameViewModel.hasClaimedLossReward(roundId)) }')

with open("app/src/main/java/com/example/ui/Overlays.kt", "w") as f:
    f.write(content)

