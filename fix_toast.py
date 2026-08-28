import sys

with open('app/src/main/java/com/example/ads/RewardedAdManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                if (BuildConfig.DEBUG) {
                    android.widget.Toast.makeText(context, "Start.io load failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }""",
"""                // Show toast even in release as requested for debugging
                android.widget.Toast.makeText(context, "Start.io load failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()"""
)

with open('app/src/main/java/com/example/ads/RewardedAdManager.kt', 'w') as f:
    f.write(content)
