import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # 1. Add val isAdReady
    content = content.replace(
        "var visible by remember { mutableStateOf(false) }",
        "var visible by remember { mutableStateOf(false) }\n    val isAdReady by rewardedAdManager.isAdReady.collectAsState()"
    )
    
    # 2. Modify 3X button onClick
    content = content.replace(
        "if (activity != null && !isClaimed && !isLoadingAd) {",
        "if (activity != null && !isClaimed && !isLoadingAd && isAdReady) {"
    )

    # 3. Modify 3X button enabled
    content = content.replace(
        "enabled = !isClaimed && !isLoadingAd\n                        ) {",
        "enabled = !isClaimed && !isLoadingAd && isAdReady\n                        ) {"
    )
    
    # 4. Modify 3X button Text
    content = content.replace(
        """text = if (isLoadingAd) "LOADING..." else if (isClaimed) "REWARD CLAIMED" else "3X REWARD",""",
        """text = if (isLoadingAd || !isAdReady) "LOADING AD..." else if (isClaimed) "REWARD CLAIMED" else "3X REWARD","""
    )
    
    # 5. Modify 3X button small text visibility
    content = content.replace(
        "if (!isClaimed && !isLoadingAd) {",
        "if (!isClaimed && !isLoadingAd && isAdReady) {"
    )

    # 6. Modify Loss Reward button enabled
    content = content.replace(
        "enabled = !isLossClaimed && !isLoadingAd\n                        ) {",
        "enabled = !isLossClaimed && !isLoadingAd && isAdReady\n                        ) {"
    )
    
    # 7. Modify Loss Reward button Text
    content = content.replace(
        """text = if (isLoadingAd) "LOADING..." else if (isLossClaimed) "REWARD CLAIMED" else "GET 3",""",
        """text = if (isLoadingAd || !isAdReady) "LOADING AD..." else if (isLossClaimed) "REWARD CLAIMED" else "GET 3","""
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/Overlays.kt')
