import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Modify Loss Reward button onClick
    content = content.replace(
        "if (activity != null && !isLossClaimed && !isLoadingAd) {",
        "if (activity != null && !isLossClaimed && !isLoadingAd && isAdReady) {"
    )

    # Modify Loss Reward button small text visibility
    content = content.replace(
        "if (!isLossClaimed && !isLoadingAd) {\n                                    Text(\n                                        text = \"Watch Ad\",",
        "if (!isLossClaimed && !isLoadingAd && isAdReady) {\n                                    Text(\n                                        text = \"Watch Ad\","
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/Overlays.kt')
