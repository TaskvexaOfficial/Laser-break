import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace('import com.example.viewmodel.GameViewModel', 'import com.example.viewmodel.GameViewModel\nimport com.example.ads.RewardedAdManager')
content = content.replace('val gemDataStore = remember { GemDataStore(context) }', 'val gemDataStore = remember { GemDataStore(context) }\n    val rewardedAdManager = remember { RewardedAdManager(context).apply { loadAd() } }')
content = content.replace('gemDataStore = gemDataStore,', 'gemDataStore = gemDataStore,\n                rewardedAdManager = rewardedAdManager,')

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)

