package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production-ready and beginner-friendly manager for Unity Ads Rewarded Video Ads.
 *
 * Ad Unit ID: "Rewarded_Android"
 *
 * To connect to your "Watch Ad" button:
 * ```kotlin
 * UnityAdsManager.getInstance(context).showRewardedAd(
 *     activity = activity,
 *     onRewardEarned = {
 *         // Grant reward to player (called ONLY when ad is fully watched)
 *     }
 * )
 * ```
 */
class UnityAdsManager private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "UnityAdsManager"

        /**
         * The Unity Ads Rewarded Ad Unit ID specified for this project.
         */
        const val AD_UNIT_ID = "Rewarded_Android"

        /**
         * Unity Game ID for Android.
         */
        var unityGameId: String = "800368057"

        @Volatile
        private var instance: UnityAdsManager? = null

        /**
         * Get the singleton instance of [UnityAdsManager].
         */
        fun getInstance(context: Context): UnityAdsManager {
            return instance ?: synchronized(this) {
                instance ?: UnityAdsManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val isShowingAd = AtomicBoolean(false)

    // Observable states for UI observation if needed
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()
    val isAdReady: StateFlow<Boolean> get() = _isAdLoaded.asStateFlow()

    private val _adLoadState = MutableStateFlow(AdLoadState.LOADING)
    val adLoadState: StateFlow<AdLoadState> = _adLoadState.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    /**
     * Initializes the Unity Ads SDK.
     *
     * @param context Application or Activity context.
     * @param gameId The Unity Game ID (defaults to [unityGameId]).
     * @param testMode True for test ads (defaults to true), false for production ads.
     * @param onComplete Optional callback when initialization finishes.
     */
    fun initialize(
        context: Context = appContext,
        gameId: String = unityGameId,
        testMode: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        unityGameId = gameId

        if (UnityAds.isInitialized) {
            Log.d(TAG, "Unity Ads SDK is already initialized.")
            _isInitialized.value = true
            loadAd()
            onComplete?.invoke()
            return
        }

        Log.d(TAG, "Initializing Unity Ads SDK with Game ID: $gameId (testMode: $testMode)...")
        UnityAds.initialize(
            context.applicationContext,
            gameId,
            testMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    mainHandler.post {
                        Log.d(TAG, "Unity Ads SDK initialization succeeded.")
                        _isInitialized.value = true
                        _lastErrorMessage.value = null
                        onComplete?.invoke()
                        // Automatically preload the first rewarded ad
                        loadAd()
                    }
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    val errorDesc = "Initialization failed: ${error?.name} - $message"
                    mainHandler.post {
                        Log.e(TAG, errorDesc)
                        _isInitialized.value = false
                        _adLoadState.value = AdLoadState.FAILED
                        _lastErrorMessage.value = errorDesc
                    }
                }
            }
        )
    }

    /**
     * Preloads a rewarded ad for the Ad Unit ID [AD_UNIT_ID].
     */
    fun loadAd() {
        if (!UnityAds.isInitialized) {
            Log.w(TAG, "Cannot load ad: Unity Ads is not yet initialized.")
            _adLoadState.value = AdLoadState.LOADING
            return
        }

        if (_isAdLoaded.value) {
            Log.d(TAG, "Rewarded ad is already loaded and ready.")
            _adLoadState.value = AdLoadState.READY
            return
        }

        _adLoadState.value = AdLoadState.LOADING
        _lastErrorMessage.value = null

        Log.d(TAG, "Loading Unity Ads rewarded ad for placement: $AD_UNIT_ID...")
        UnityAds.load(AD_UNIT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                mainHandler.post {
                    Log.d(TAG, "Unity Ads rewarded ad loaded successfully for placement: $placementId")
                    _isAdLoaded.value = true
                    _adLoadState.value = AdLoadState.READY
                    _lastErrorMessage.value = null
                }
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                val errorDesc = "Ad load failed ($placementId): ${error?.name} - $message"
                mainHandler.post {
                    Log.e(TAG, errorDesc)
                    _isAdLoaded.value = false
                    _adLoadState.value = AdLoadState.FAILED
                    _lastErrorMessage.value = errorDesc
                }
            }
        })
    }

    /**
     * Simple public function to show the rewarded ad.
     * Connect this function to your "Watch Ad" button.
     *
     * The [onRewardEarned] callback is triggered ONLY if the player watches the
     * video to the very end without skipping.
     *
     * @param activity The host Activity to display the ad over.
     * @param onRewardEarned Called ONLY when the user fully completes the rewarded ad.
     * @param onAdDismissed Called when the ad is closed (either completed, skipped, or dismissed).
     * @param onAdFailed Called if the ad fails to show or is not ready.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: ((errorMessage: String) -> Unit)? = null
    ) {
        if (!isShowingAd.compareAndSet(false, true)) {
            Log.d(TAG, "An ad is already being displayed, ignoring duplicate show call.")
            return
        }

        if (!UnityAds.isInitialized) {
            isShowingAd.set(false)
            val msg = "Unity Ads is not initialized yet."
            Log.w(TAG, msg)
            onAdFailed?.invoke(msg)
            return
        }

        if (!_isAdLoaded.value) {
            isShowingAd.set(false)
            val msg = "Unity Ads rewarded ad is not ready yet."
            Log.w(TAG, msg)
            onAdFailed?.invoke(msg)
            // Trigger a reload attempt
            loadAd()
            return
        }

        Log.d(TAG, "Showing Unity Ads rewarded ad for placement: $AD_UNIT_ID...")
        // Reset ad loaded status while showing
        _isAdLoaded.value = false
        _adLoadState.value = AdLoadState.LOADING

        // Prevent reward from being granted more than once per ad display
        val rewardGranted = AtomicBoolean(false)

        UnityAds.show(
            activity,
            AD_UNIT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowStart(placementId: String?) {
                    Log.d(TAG, "Unity Ads rewarded ad playback started for placement: $placementId")
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    Log.d(TAG, "Unity Ads rewarded ad clicked for placement: $placementId")
                }

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    isShowingAd.set(false)
                    mainHandler.post {
                        Log.d(TAG, "Unity Ads rewarded ad finished with completion state: $state")

                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            if (rewardGranted.compareAndSet(false, true)) {
                                Log.d(TAG, "Rewarded ad watched to completion. Granting reward to player!")
                                onRewardEarned()
                            }
                        } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
                            Log.d(TAG, "Rewarded ad was skipped. Reward NOT granted.")
                        } else {
                            Log.w(TAG, "Rewarded ad finished without COMPLETED status ($state). Reward NOT granted.")
                        }

                        onAdDismissed?.invoke()
                        // Preload the next rewarded ad for the player
                        loadAd()
                    }
                }

                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    isShowingAd.set(false)
                    val errorDesc = "Failed to show ad: ${error?.name} - $message"
                    mainHandler.post {
                        Log.e(TAG, errorDesc)
                        _lastErrorMessage.value = errorDesc
                        _adLoadState.value = AdLoadState.FAILED
                        onAdFailed?.invoke(errorDesc)
                        onAdDismissed?.invoke()
                        // Attempt to reload
                        loadAd()
                    }
                }
            }
        )
    }

    /**
     * Convenience method to show ad with onAdNotReady callback.
     */
    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdNotReady: () -> Unit
    ) {
        showRewardedAd(
            activity = activity,
            onRewardEarned = onRewardEarned,
            onAdDismissed = onAdDismissed,
            onAdFailed = { onAdNotReady() }
        )
    }

    /**
     * Check if a rewarded ad is currently ready to be shown.
     */
    fun isReady(): Boolean = _isAdLoaded.value && UnityAds.isInitialized
}
