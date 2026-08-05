import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add loadAd to init block
    target1 = """    init {
        StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG)
    }"""
    
    replacement1 = """    init {
        StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG)
        loadAd()
    }"""
    
    if target1 in content:
        content = content.replace(target1, replacement1)
    
    # Fix onFailedToReceiveAd
    target2 = """            override fun onFailedToReceiveAd(ad: Ad?) {
                Log.d("RewardedAdManager", "Ad failed to load")
                isAdLoading = false
                _isAdReady.value = false
            }"""
            
    replacement2 = """            override fun onFailedToReceiveAd(ad: Ad?) {
                val errorMsg = ad?.errorMessage ?: "Unknown error"
                Log.d("RewardedAdManager", "Ad failed to load: $errorMsg")
                isAdLoading = false
                _isAdReady.value = false
                if (BuildConfig.DEBUG) {
                    android.widget.Toast.makeText(context, "Start.io load failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
            }"""

    if target2 in content:
        content = content.replace(target2, replacement2)

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ads/RewardedAdManager.kt')
