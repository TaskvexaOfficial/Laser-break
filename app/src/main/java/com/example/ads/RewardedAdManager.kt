package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

class RewardedAdManager(private val context: Context) {
    private var rewardedVideo: StartAppAd? = null

    private var isAdLoading = false
    private val isShowingAd = AtomicBoolean(false)
    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    init {
        StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG)
        loadAd()
    }

    fun loadAd() {
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
    }

    fun showAd(
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
    }
}
