import sys

with open('app/src/main/java/com/example/ads/RewardedAdManager.kt', 'r') as f:
    content = f.read()

# Add AdLoadState enum
enum_code = """import java.util.concurrent.atomic.AtomicBoolean

enum class AdLoadState {
    LOADING, READY, FAILED
}
"""
content = content.replace('import java.util.concurrent.atomic.AtomicBoolean', enum_code)

# Replace fields
fields_old = """    private var rewardedVideo: StartAppAd? = null
    private var isAdLoading = false
    private val isShowingAd = AtomicBoolean(false)
    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()"""
fields_new = """    private var rewardedVideo: StartAppAd? = null
    private val isShowingAd = AtomicBoolean(false)
    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()
    private val _adLoadState = MutableStateFlow(AdLoadState.LOADING)
    val adLoadState: StateFlow<AdLoadState> = _adLoadState.asStateFlow()"""
content = content.replace(fields_old, fields_new)

# Replace loadAd()
load_ad_old = """    fun loadAd() {
        if (rewardedVideo != null && rewardedVideo!!.isReady) {
            _isAdReady.value = true
            return
        }
        
        if (isAdLoading) return
        isAdLoading = true
        _isAdReady.value = false
        
        if (rewardedVideo == null) {
            rewardedVideo = StartAppAd(context)
        }

        rewardedVideo?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d("RewardedAdManager", "Ad loaded")
                isAdLoading = false
                _isAdReady.value = true
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                val errorMsg = ad?.errorMessage ?: "Unknown error"
                Log.d("RewardedAdManager", "Ad failed to load: $errorMsg")
                isAdLoading = false
                _isAdReady.value = false
                if (BuildConfig.DEBUG) {
                    android.widget.Toast.makeText(context, "Start.io load failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        })
    }"""
    
load_ad_new = """    fun loadAd() {
        if (rewardedVideo != null && rewardedVideo!!.isReady) {
            _isAdReady.value = true
            _adLoadState.value = AdLoadState.READY
            return
        }
        
        if (_adLoadState.value == AdLoadState.LOADING && rewardedVideo != null) return
        
        _adLoadState.value = AdLoadState.LOADING
        _isAdReady.value = false
        
        if (rewardedVideo == null) {
            rewardedVideo = StartAppAd(context)
        }

        rewardedVideo?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d("RewardedAdManager", "Ad loaded")
                _isAdReady.value = true
                _adLoadState.value = AdLoadState.READY
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                val errorMsg = ad?.errorMessage ?: "UNKNOWN START.IO LOAD ERROR"
                Log.e("RewardedAdManager", "Ad failed to load: $errorMsg")
                _isAdReady.value = false
                _adLoadState.value = AdLoadState.FAILED
                if (BuildConfig.DEBUG) {
                    android.widget.Toast.makeText(context, "Start.io load failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        })
    }"""

content = content.replace(load_ad_old, load_ad_new)

with open('app/src/main/java/com/example/ads/RewardedAdManager.kt', 'w') as f:
    f.write(content)
