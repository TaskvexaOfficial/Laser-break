import sys

with open('app/src/main/java/com/example/ui/Overlays.kt', 'r') as f:
    content = f.read()

# Add import for AdLoadState if not present
if 'import com.example.ads.AdLoadState' not in content:
    content = content.replace('import com.example.ads.RewardedAdManager', 'import com.example.ads.RewardedAdManager\nimport com.example.ads.AdLoadState')

# Observe adLoadState
content = content.replace(
    'val isAdReady by rewardedAdManager.isAdReady.collectAsState()',
    'val isAdReady by rewardedAdManager.isAdReady.collectAsState()\n    val adLoadState by rewardedAdManager.adLoadState.collectAsState()'
)

# For 3X REWARD block:
old_button_1_enabled = "enabled = !isClaimed && !isLoadingAd && isAdReady"
new_button_1_enabled = "enabled = !isClaimed && !isLoadingAd && (isAdReady || adLoadState == AdLoadState.FAILED)"

old_button_1_click = """                                if (activity != null && !isClaimed && !isLoadingAd && isAdReady) {
                                    isLoadingAd = true"""
new_button_1_click = """                                if (adLoadState == AdLoadState.FAILED) {
                                    rewardedAdManager.loadAd()
                                    return@OutlinedButton
                                }
                                if (activity != null && !isClaimed && !isLoadingAd && isAdReady) {
                                    isLoadingAd = true"""
                                    
old_button_1_text = """text = if (isLoadingAd || !isAdReady) "LOADING AD..." else if (isClaimed) "REWARD CLAIMED" else "3X REWARD","""
new_button_1_text = """text = if (isClaimed) "REWARD CLAIMED" else if (isLoadingAd) "LOADING AD..." else if (adLoadState == AdLoadState.FAILED) "RETRY AD" else if (!isAdReady) "LOADING AD..." else "3X REWARD","""

# For GET 3 block:
old_button_2_enabled = "enabled = !isLossClaimed && !isLoadingAd && isAdReady"
new_button_2_enabled = "enabled = !isLossClaimed && !isLoadingAd && (isAdReady || adLoadState == AdLoadState.FAILED)"

old_button_2_click = """                                if (activity != null && !isLossClaimed && !isLoadingAd && isAdReady) {
                                    isLoadingAd = true"""
new_button_2_click = """                                if (adLoadState == AdLoadState.FAILED) {
                                    rewardedAdManager.loadAd()
                                    return@OutlinedButton
                                }
                                if (activity != null && !isLossClaimed && !isLoadingAd && isAdReady) {
                                    isLoadingAd = true"""

old_button_2_text = """text = if (isLoadingAd || !isAdReady) "LOADING AD..." else if (isLossClaimed) "REWARD CLAIMED" else "GET 3","""
new_button_2_text = """text = if (isLossClaimed) "REWARD CLAIMED" else if (isLoadingAd) "LOADING AD..." else if (adLoadState == AdLoadState.FAILED) "RETRY AD" else if (!isAdReady) "LOADING AD..." else "GET 3","""

content = content.replace(old_button_1_enabled, new_button_1_enabled)
content = content.replace(old_button_1_click, new_button_1_click)
content = content.replace(old_button_1_text, new_button_1_text)

content = content.replace(old_button_2_enabled, new_button_2_enabled)
content = content.replace(old_button_2_click, new_button_2_click)
content = content.replace(old_button_2_text, new_button_2_text)


with open('app/src/main/java/com/example/ui/Overlays.kt', 'w') as f:
    f.write(content)
