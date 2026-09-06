package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.UnityAdsLoadOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * State representing rewarded ad loading status.
 */
enum class AdLoadState {
    NOT_LOADED,
    LOADING,
    READY,
    FAILED
}

/**
 * Dedicated, production-ready manager for Unity Ads Rewarded Ads.
 *
 * Configured with:
 * - Unity Game ID: "800368057"
 * - Rewarded Ad Unit ID: "Rewarded_Android"
 * - Test Mode: true
 */
class UnityAdsManager private constructor(private val appContext: Context) {

    companion object {
        private const val TAG = "UnityAdsManager"

        /**
         * Unity Rewarded Ad Unit ID for Android.
         */
        const val AD_UNIT_ID = "Rewarded_Android"

        /**
         * Real Unity Game ID for Android.
         */
        const val DEFAULT_GAME_ID = "800368057"

        @Volatile
        private var instance: UnityAdsManager? = null

        /**
         * Retrieves or creates the singleton instance of [UnityAdsManager].
         */
        fun getInstance(context: Context): UnityAdsManager {
            return instance ?: synchronized(this) {
                instance ?: UnityAdsManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val isInitializing = AtomicBoolean(false)
    private val isLoadingAdInProgress = AtomicBoolean(false)
    private val isShowingAd = AtomicBoolean(false)

    var unityGameId: String = DEFAULT_GAME_ID
        private set

    // Observable states
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()
    val isAdReady: StateFlow<Boolean> get() = _isAdLoaded.asStateFlow()

    private val _adLoadState = MutableStateFlow(AdLoadState.NOT_LOADED)
    val adLoadState: StateFlow<AdLoadState> = _adLoadState.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    /**
     * Initializes the Unity Ads SDK.
     *
     * @param context Application or Activity context.
     * @param gameId Unity Game ID (defaults to 800368057).
     * @param testMode Enables Unity Ads test mode (defaults to true).
     * @param onComplete Optional callback when initialization finishes successfully.
     */
    fun initialize(
        context: Context = appContext,
        gameId: String = DEFAULT_GAME_ID,
        testMode: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        unityGameId = gameId

        if (UnityAds.isInitialized) {
            Log.i(TAG, "Unity Ads SDK is already initialized for Game ID: $gameId")
            _isInitialized.value = true
            onComplete?.invoke()
            if (!_isAdLoaded.value && !isLoadingAdInProgress.get()) {
                loadAd()
            }
            return
        }

        if (!isInitializing.compareAndSet(false, true)) {
            Log.d(TAG, "Unity Ads SDK initialization is already in progress, awaiting result...")
            return
        }

        Log.i(TAG, "Initializing Unity Ads SDK (Game ID: $gameId, testMode: $testMode)...")
        _adLoadState.value = AdLoadState.LOADING

        UnityAds.initialize(
            context.applicationContext,
            gameId,
            testMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitializing.set(false)
                    mainHandler.post {
                        Log.i(TAG, "=== [UNITY ADS DIAGNOSTIC] 1. Initialization: SUCCESS ===")
                        Log.i(TAG, "[UNITY ADS DIAGNOSTIC] Game ID: $gameId | testMode: $testMode | isInitialized: ${UnityAds.isInitialized}")
                        _isInitialized.value = true
                        _lastErrorMessage.value = null
                        onComplete?.invoke()
                        // Automatically preload the rewarded ad upon successful initialization
                        loadAd()
                    }
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    isInitializing.set(false)
                    val errorEnum = error?.name ?: "UNKNOWN"
                    val errorDesc = "Unity Ads initialization FAILED: [Error: $errorEnum] $message"
                    mainHandler.post {
                        Log.e(TAG, "=== [UNITY ADS DIAGNOSTIC] 1. Initialization: FAILED ===")
                        Log.e(TAG, "[UNITY ADS DIAGNOSTIC] Game ID: $gameId | testMode: $testMode")
                        Log.e(TAG, "[UNITY ADS DIAGNOSTIC] Error Enum: $errorEnum")
                        Log.e(TAG, "[UNITY ADS DIAGNOSTIC] Error Message: $message")
                        _isInitialized.value = false
                        _adLoadState.value = AdLoadState.FAILED
                        _lastErrorMessage.value = errorDesc
                    }
                }
            }
        )
    }

    /**
     * Preloads a rewarded ad for the placement [AD_UNIT_ID].
     * If Unity Ads is not initialized, it will initialize first and then load.
     */
    fun loadAd() {
        if (!UnityAds.isInitialized) {
            Log.w(TAG, "Unity Ads is not initialized yet. Triggering initialization before loading ad...")
            _adLoadState.value = AdLoadState.LOADING
            initialize(appContext, unityGameId, testMode = true)
            return
        }

        if (_isAdLoaded.value) {
            Log.i(TAG, "Unity Ads rewarded ad '$AD_UNIT_ID' is already loaded and ready.")
            _adLoadState.value = AdLoadState.READY
            return
        }

        if (!isLoadingAdInProgress.compareAndSet(false, true)) {
            Log.d(TAG, "Unity Ads rewarded ad '$AD_UNIT_ID' is already loading, skipping duplicate request.")
            return
        }

        _adLoadState.value = AdLoadState.LOADING
        _lastErrorMessage.value = null

        Log.i(TAG, "=== [UNITY ADS DIAGNOSTIC] 2. Starting UnityAds.load() ===")
        Log.i(TAG, "[UNITY ADS DIAGNOSTIC] Placement ID passed to UnityAds.load(): '$AD_UNIT_ID'")
        Log.i(TAG, "[UNITY ADS DIAGNOSTIC] UnityAds.isInitialized(): ${UnityAds.isInitialized}")
        UnityAds.load(AD_UNIT_ID, UnityAdsLoadOptions(), object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                isLoadingAdInProgress.set(false)
                mainHandler.post {
                    Log.i(TAG, "=== [UNITY ADS DIAGNOSTIC] Ad Load Result: SUCCESS ===")
                    Log.i(TAG, "[UNITY ADS DIAGNOSTIC] Loaded Placement ID: '$placementId'")
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
                isLoadingAdInProgress.set(false)
                val errorEnumName = error?.name ?: "UNKNOWN"
                val errorDesc = "Unity Ads Load Failed\nError: $errorEnumName\nMessage: $message\nPlacement: $placementId"
                mainHandler.post {
                    Log.e(TAG, "=== [UNITY ADS DIAGNOSTIC] Ad Load Result: FAILED ===")
                    Log.e(TAG, "[UNITY ADS DIAGNOSTIC] 2. Exact Placement ID: '$placementId'")
                    Log.e(TAG, "[UNITY ADS DIAGNOSTIC] 3. Exact UnityAdsLoadError Enum: $errorEnumName")
                    Log.e(TAG, "[UNITY ADS DIAGNOSTIC] 4. Exact Error Message: $message")
                    _isAdLoaded.value = false
                    _adLoadState.value = AdLoadState.FAILED
                    _lastErrorMessage.value = errorDesc
                }
            }
        })
    }

    /**
     * Displays the rewarded ad.
     *
     * Rewards the player ONLY when the ad completion callback returns COMPLETED.
     * If skipped, dismissed, or failed, no reward is granted.
     *
     * @param activity Host Activity to display the ad over.
     * @param onRewardEarned Triggered ONLY when the user fully completes the rewarded ad.
     * @param onAdDismissed Triggered when the ad is closed (either completed, skipped, or failed).
     * @param onAdFailed Triggered if the ad fails to show or is not ready.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: ((errorMessage: String) -> Unit)? = null
    ) {
        if (!isShowingAd.compareAndSet(false, true)) {
            Log.d(TAG, "An ad is already being shown. Ignoring duplicate show call.")
            return
        }

        if (!UnityAds.isInitialized) {
            isShowingAd.set(false)
            val msg = "Unity Ads is not initialized yet."
            Log.w(TAG, msg)
            onAdFailed?.invoke(msg)
            initialize(appContext, unityGameId, testMode = true)
            return
        }

        if (!_isAdLoaded.value) {
            isShowingAd.set(false)
            val msg = "Unity Ads rewarded ad is not ready yet."
            Log.w(TAG, msg)
            onAdFailed?.invoke(msg)
            loadAd()
            return
        }

        Log.i(TAG, "Showing Unity Ads rewarded ad for placement: $AD_UNIT_ID...")
        // Reset ad loaded state
        _isAdLoaded.value = false
        _adLoadState.value = AdLoadState.LOADING

        val rewardGranted = AtomicBoolean(false)

        UnityAds.show(
            activity,
            AD_UNIT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowStart(placementId: String?) {
                    Log.i(TAG, "Unity Ads show started for placement: $placementId")
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    Log.i(TAG, "Unity Ads ad clicked for placement: $placementId")
                }

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    isShowingAd.set(false)
                    mainHandler.post {
                        Log.i(TAG, "Unity Ads show completed for placement '$placementId' with state: $state")

                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            if (rewardGranted.compareAndSet(false, true)) {
                                Log.i(TAG, "Rewarded ad COMPLETED. Granting reward to player!")
                                onRewardEarned()
                            }
                        } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
                            Log.w(TAG, "Rewarded ad was SKIPPED by player. Reward NOT granted.")
                        } else {
                            Log.w(TAG, "Rewarded ad finished with state ($state). Reward NOT granted.")
                        }

                        onAdDismissed?.invoke()
                        // Automatically preload the next rewarded ad
                        loadAd()
                    }
                }

                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    isShowingAd.set(false)
                    val errorDesc = "Unity Ads show FAILED for placement '$placementId': [Error: ${error?.name ?: "UNKNOWN"}] $message"
                    mainHandler.post {
                        Log.e(TAG, errorDesc)
                        _lastErrorMessage.value = errorDesc
                        _adLoadState.value = AdLoadState.FAILED
                        onAdFailed?.invoke(errorDesc)
                        onAdDismissed?.invoke()
                        // Attempt to reload for next time
                        loadAd()
                    }
                }
            }
        )
    }

    /**
     * Checks whether a rewarded ad is loaded and ready to be shown.
     */
    fun isReady(): Boolean = _isAdLoaded.value && UnityAds.isInitialized
}
