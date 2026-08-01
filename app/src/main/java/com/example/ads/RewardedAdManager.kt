package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.os.Handler
import android.os.Looper

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test ad unit
    
    private var isAdLoading = false
    private var retryAttempt = 0
    private val maxRetries = 3

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    fun loadAd() {
        if (rewardedAd != null || isAdLoading) return
        
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("RewardedAdManager", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isAdLoading = false
                    _isAdReady.value = false
                    
                    if (retryAttempt < maxRetries) {
                        retryAttempt++
                        val delayMillis = (Math.pow(2.0, retryAttempt.toDouble()) * 1000).toLong() // Exponential backoff
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadAd()
                        }, delayMillis)
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "Ad loaded")
                    rewardedAd = ad
                    isAdLoading = false
                    retryAttempt = 0
                    _isAdReady.value = true
                }
            }
        )
    }

    fun showAd(
        activity: Activity, 
        onRewardEarned: () -> Unit, 
        onAdDismissed: () -> Unit, 
        onAdNotReady: () -> Unit
    ) {
        if (rewardedAd != null) {
            var rewardEarned = false
            
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isAdReady.value = false
                    onAdDismissed()
                    loadAd() // Preload the next ad
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isAdReady.value = false
                    onAdDismissed()
                    loadAd() // Attempt to reload if it failed to show
                }
            }
            
            rewardedAd?.show(activity) { _ ->
                if (!rewardEarned) {
                    rewardEarned = true
                    onRewardEarned()
                }
            }
        } else {
            Log.d("RewardedAdManager", "Ad wasn't ready yet.")
            onAdNotReady()
            loadAd()
        }
    }
}
