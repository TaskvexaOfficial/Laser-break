import re

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "r") as f:
    content = f.read()

reward_state_code = """
    // Reward Tracking
    private var baseWinRewardCreditedRoundId: String? = null
    private var completed3XAds = 0
    private var bonus3XCreditedRoundId: String? = null
    private var lossAdRewardCreditedRoundId: String? = null

    fun claimBaseWinReward(roundId: String): Boolean {
        if (baseWinRewardCreditedRoundId == roundId) return false
        baseWinRewardCreditedRoundId = roundId
        return true
    }

    fun getCompleted3XAds(roundId: String): Int {
        if (bonus3XCreditedRoundId == roundId) return 3 // Already fully claimed
        // We need to tie completed ads to roundId. If roundId changes, reset.
        return completed3XAds
    }

    fun record3XAdCompletion(roundId: String): Int {
        completed3XAds++
        return completed3XAds
    }

    fun claim3XBonusReward(roundId: String): Boolean {
        if (bonus3XCreditedRoundId == roundId) return false
        bonus3XCreditedRoundId = roundId
        return true
    }

    fun claimLossAdReward(roundId: String): Boolean {
        if (lossAdRewardCreditedRoundId == roundId) return false
        lossAdRewardCreditedRoundId = roundId
        return true
    }
    
    fun resetRewardState() {
        completed3XAds = 0
    }
"""

content = content.replace('    fun startNewGame() {', reward_state_code + '\n    fun startNewGame() {')
content = content.replace('startGameLoop()', 'resetRewardState()\n        startGameLoop()')

with open("app/src/main/java/com/example/viewmodel/GameViewModel.kt", "w") as f:
    f.write(content)

