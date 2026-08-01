import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add imports
    content = content.replace(
        "import com.example.ads.RewardedAdManager",
        "import com.example.ads.RewardedAdManager\nimport com.example.ads.ConsentManager"
    )

    # Change signature
    content = content.replace(
        "fun AppNavigation(soundManager: SoundManager)",
        "fun AppNavigation(soundManager: SoundManager, consentManager: ConsentManager)"
    )

    # Change RewardedAdManager initialization
    content = content.replace(
        "val rewardedAdManager = remember { RewardedAdManager(context).apply { loadAd() } }",
        """val rewardedAdManager = remember { RewardedAdManager(context) }
    
    val canRequestAds by consentManager.canRequestAds.collectAsState()
    LaunchedEffect(canRequestAds) {
        if (canRequestAds) {
            rewardedAdManager.loadAd()
        }
    }
    
    val isPrivacyOptionsRequired by consentManager.isPrivacyOptionsRequired.collectAsState()"""
    )
    
    # Change MainMenuScreen call
    content = content.replace(
        """MainMenuScreen(
                gemCount = gemCount,
                soundEnabled = soundEnabled,
                onSoundToggle = { 
                    soundManager.setSoundEnabled(it); scope.launch { gemDataStore.setSoundEnabled(it) }
                },
                onPlayClick = {
                    gameViewModel.startNewGame()
                    navController.navigate("game")
                }
            )""",
        """MainMenuScreen(
                gemCount = gemCount,
                soundEnabled = soundEnabled,
                onSoundToggle = { 
                    soundManager.setSoundEnabled(it); scope.launch { gemDataStore.setSoundEnabled(it) }
                },
                onPlayClick = {
                    gameViewModel.startNewGame()
                    navController.navigate("game")
                },
                isPrivacyOptionsRequired = isPrivacyOptionsRequired,
                onPrivacyOptionsClick = {
                    consentManager.showPrivacyOptionsForm {
                        // Optional: Handle dismiss
                    }
                }
            )"""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/AppNavigation.kt')
