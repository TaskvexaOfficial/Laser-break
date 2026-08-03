import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Change adUnitId
    content = content.replace(
        "private val adUnitId = \"ca-app-pub-3940256099942544/5224354917\" // Test ad unit",
        "private val adUnitId = com.example.BuildConfig.REWARDED_AD_UNIT_ID"
    )

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ads/RewardedAdManager.kt')
