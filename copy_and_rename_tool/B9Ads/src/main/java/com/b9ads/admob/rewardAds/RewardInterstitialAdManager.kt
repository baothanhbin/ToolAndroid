package com.b9ads.admob.rewardAds

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.b9ads.admob.rewardAds.callback.RewardCallback
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9utils.util.FirebaseLogEventUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.lang.ref.WeakReference

object RewardInterstitialAdManager {
    const val TAG = "RewardInterstitialAdManager"
    private var rewardedAdInterstitialAd: RewardedInterstitialAd? = null
    private var activityRef: WeakReference<Activity>? = null
    private var rewardAdCallback: RewardCallback? = null
    private var adUnitId: String = ""
    private var adName = ""


    fun initialize(
        activity: Activity,
        adId: String,
        adName: String,
        rewardCallback: RewardCallback? = null
    ) {
        activityRef = WeakReference(activity) // Giữ Activity một cách an toàn
        this.rewardAdCallback = rewardCallback
        this.adUnitId = adId
        this.adName = adName
        loadRewardedInterstitialAdRequest()
    }

    fun showRewardInterstitialAd(rewardCallback: RewardCallback? = null) {
        this.rewardAdCallback = rewardCallback
        val activity = activityRef?.get()
        if (activity == null || activity.isDestroyed) {
            Log.e(TAG, "Activity is null or destroyed. Cannot show ad.")
            rewardAdCallback?.onAdNotReady()
            return
        }

        if (rewardedAdInterstitialAd == null) {
            Log.d(TAG, "The rewarded interstitial ad wasn't ready yet.")
            rewardAdCallback?.onAdNotReady()
            return
        }

        rewardedAdInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
                FirebaseLogEventUtils.logEventCommonWithName(activity, "reward_inter_click")
                rewardAdCallback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                rewardedAdInterstitialAd = null
                rewardAdCallback?.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show: ${adError.message}")
                rewardedAdInterstitialAd = null
                rewardAdCallback?.onAdFailedToShow(adError)
                FirebaseLogEventUtils.logEventCommonWithName(activity, "onAdFailedToShow_${adError.message}")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
                rewardAdCallback?.onAdImpression()
                val info = rewardedAdInterstitialAd?.responseInfo?.loadedAdapterResponseInfo
                AnalyticTracking.trackFanImpression(activity, adName, info?.adSourceId)
                FirebaseLogEventUtils.logEventCommonWithName(activity, "onAdImpression")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                rewardAdCallback?.onAdShowed()
                FirebaseLogEventUtils.logEventCommonWithName(activity, "onAdImpression")
            }
        }

        rewardedAdInterstitialAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned the reward: ${rewardItem.amount}")
            rewardAdCallback?.onUserEarnedReward(rewardItem)
        }
    }

    fun loadRewardedInterstitialAdRequest() {
        val activity = activityRef?.get() ?: return

        if (rewardedAdInterstitialAd != null) {
            Log.d(TAG, "Ad is already loaded.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(activity, adUnitId, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                rewardedAdInterstitialAd = null
                rewardAdCallback?.onAdFailedToLoad(adError)
                // Tải lại quảng cáo sau 2 giây
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRewardedInterstitialAdRequest()
                }, 2000)
            }

            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAdInterstitialAd = ad
                rewardAdCallback?.onAdLoaded(ad)

                rewardedAdInterstitialAd?.setOnPaidEventListener { adValue ->
                    AdjustTracking.trackRevenueReward(
                        rewardedAdInterstitialAd?.responseInfo?.loadedAdapterResponseInfo,
                        adValue
                    )
                    AnalyticTracking.trackingRevenue(activity, adName, adValue)
                    FirebaseLogEventUtils.logEventCommonWithName(activity, "onPaid")
                }
            }
        })
    }




    /*Khởi tạo load and show */

    fun initializeLoadAndShow(
        activity: Activity,
        adId: String,
        adName: String,
        rewardCallback: RewardCallback? = null
    ) {
        activityRef = WeakReference(activity) // Giữ Activity một cách an toàn
        this.rewardAdCallback = rewardCallback
        this.adUnitId = adId
        this.adName = adName

        loadRewardedInterstitialAndShow()
    }

    private fun loadRewardedInterstitialAndShow() {
        val activity = activityRef?.get() ?: return

        if (rewardedAdInterstitialAd != null) {
            Log.d(TAG, "Ad is already loaded.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(activity, adUnitId, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                rewardedAdInterstitialAd = null
                rewardAdCallback?.onAdFailedToLoad(adError)
                // Tải lại quảng cáo sau 2 giây
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRewardedInterstitialAdRequest()
                }, 2000)
            }

            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAdInterstitialAd = ad
                rewardAdCallback?.onAdLoaded(ad)
                rewardedAdInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        Log.d(TAG, "Ad was clicked.")
                        FirebaseLogEventUtils.logEventCommonWithName(activity, "reward_inter_click")
                        rewardAdCallback?.onAdClicked()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        rewardedAdInterstitialAd = null
                        rewardAdCallback?.onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "Ad failed to show: ${adError.message}")
                        rewardedAdInterstitialAd = null
                        rewardAdCallback?.onAdFailedToShow(adError)
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Ad recorded an impression.")
                        rewardAdCallback?.onAdImpression()
                        val info = rewardedAdInterstitialAd?.responseInfo?.loadedAdapterResponseInfo
                        AnalyticTracking.trackFanImpression(activity, adName, info?.adSourceId)
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        rewardAdCallback?.onAdShowed()
                    }
                }

                rewardedAdInterstitialAd?.show(activity) { rewardItem ->
                    Log.d(TAG, "User earned the reward: ${rewardItem.amount}")
                    rewardAdCallback?.onUserEarnedReward(rewardItem)
                }

                rewardedAdInterstitialAd?.setOnPaidEventListener { adValue ->
                    AdjustTracking.trackRevenueReward(
                        rewardedAdInterstitialAd?.responseInfo?.loadedAdapterResponseInfo,
                        adValue
                    )
                    AnalyticTracking.trackingRevenue(activity, adName, adValue)
                    FirebaseLogEventUtils.logEventCommonWithName(activity, "rewardedInter_onPaid")
                }
            }
        })
    }

}
