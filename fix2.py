import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Add atomic boolean import
    import_atomic = "import java.util.concurrent.atomic.AtomicBoolean\n"
    content = content.replace("import com.example.BuildConfig\n", "import com.example.BuildConfig\n" + import_atomic)

    # Add isShowingAd
    content = content.replace("    private var isAdLoading = false\n", "    private var isAdLoading = false\n    private val isShowingAd = AtomicBoolean(false)\n")

    # Update showAd
    show_ad_orig = """    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdNotReady: () -> Unit
    ) {
        if (rewardedVideo != null && rewardedVideo!!.isReady) {
            var rewardEarned = false

            rewardedVideo?.setVideoListener {
                if (!rewardEarned) {
                    rewardEarned = true
                    onRewardEarned()
                }
            }
            
            _isAdReady.value = false // Set false when showing

            val displayed = rewardedVideo?.showAd(object : AdDisplayListener {
                override fun adHidden(ad: Ad?) {
                    onAdDismissed()
                    loadAd()
                }

                override fun adDisplayed(ad: Ad?) {}

                override fun adClicked(ad: Ad?) {}

                override fun adNotDisplayed(ad: Ad?) {
                    onAdDismissed()
                    loadAd()
                }
            })

            if (displayed == false) {
                Log.d("RewardedAdManager", "Ad failed to show")
                onAdNotReady()
                loadAd()
            }
        } else {
            Log.d("RewardedAdManager", "Ad wasn't ready yet.")
            onAdNotReady()
            loadAd()
        }
    }"""
    
    show_ad_new = """    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdNotReady: () -> Unit
    ) {
        if (!isShowingAd.compareAndSet(false, true)) {
            Log.d("RewardedAdManager", "Ad is already showing, ignoring duplicate call.")
            return
        }

        if (rewardedVideo != null && rewardedVideo!!.isReady) {
            val rewardDelivered = AtomicBoolean(false)

            rewardedVideo?.setVideoListener {
                if (rewardDelivered.compareAndSet(false, true)) {
                    onRewardEarned()
                }
            }
            
            _isAdReady.value = false // Set false when showing

            var displayed = false
            try {
                displayed = rewardedVideo?.showAd(object : AdDisplayListener {
                    override fun adHidden(ad: Ad?) {
                        isShowingAd.set(false)
                        onAdDismissed()
                        loadAd()
                    }

                    override fun adDisplayed(ad: Ad?) {}

                    override fun adClicked(ad: Ad?) {}

                    override fun adNotDisplayed(ad: Ad?) {
                        isShowingAd.set(false)
                        onAdDismissed()
                        loadAd()
                    }
                }) ?: false
            } catch (e: Exception) {
                Log.e("RewardedAdManager", "Exception while showing ad", e)
                displayed = false
            }

            if (!displayed) {
                isShowingAd.set(false)
                Log.d("RewardedAdManager", "Ad failed to show")
                onAdNotReady()
                loadAd()
            }
        } else {
            isShowingAd.set(false)
            Log.d("RewardedAdManager", "Ad wasn't ready yet.")
            onAdNotReady()
            loadAd()
        }
    }"""
    
    content = content.replace(show_ad_orig, show_ad_new)
    
    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ads/RewardedAdManager.kt')
