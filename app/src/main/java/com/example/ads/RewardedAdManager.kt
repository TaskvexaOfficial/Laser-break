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

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test ad unit

    fun loadAd() {
        if (rewardedAd != null) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("RewardedAdManager", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "Ad loaded")
                    rewardedAd = ad
                    
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadAd() // Preload the next ad
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            rewardedAd = null
                            loadAd()
                        }
                    }
                }
            }
        )
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit, onAdNotReady: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { _ ->
                onRewardEarned()
            }
        } else {
            Log.d("RewardedAdManager", "Ad wasn't ready yet.")
            onAdNotReady()
            loadAd()
        }
    }
}
