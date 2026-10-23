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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.lang.ref.WeakReference

object RewardManager {
    const val TAG = "RewardAdManager"
    private var rewardedAd: RewardedAd? = null
    private var activityRef: WeakReference<Activity>? = null
    private var rewardAdCallback: RewardCallback? = null
    private var adUnitId: String = ""
    private var adName: String = ""


    fun initializeRewardAd(
        activity: Activity,
        adId: String,
        adName: String,
        rewardCallback: RewardCallback? = null,
    ) {
        activityRef = WeakReference(activity) // Giữ Activity một cách an toàn
        this.rewardAdCallback = rewardCallback
        this.adUnitId = adId
        this.adName = adName
        loadRewardedAdRequest()
    }

    fun showRewardAd(rewardCallback: RewardCallback? = null) {
        this.rewardAdCallback = rewardCallback
        val activity = activityRef?.get()
        if (activity == null || activity.isDestroyed) {
            Log.e(TAG, "Activity is null or destroyed. Cannot show ad.")
            rewardAdCallback?.onAdNotReady()
            return
        }

        if (rewardedAd == null) {
            Log.d(TAG, "The rewarded interstitial ad wasn't ready yet.")
            rewardAdCallback?.onAdNotReady()
            return
        }

        rewardedAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    Log.d(TAG, "Ad was clicked.")
                    FirebaseLogEventUtils.logEventCommonWithName(activity, "reward_click")
                    rewardAdCallback?.onAdClicked()
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    rewardedAd = null
                    rewardAdCallback?.onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    rewardAdCallback?.onAdFailedToShow(adError)
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Ad recorded an impression.")
                    rewardAdCallback?.onAdImpression()
                    val info = rewardedAd?.responseInfo?.loadedAdapterResponseInfo
                    AnalyticTracking.trackFanImpression(activity, adName, info?.adSourceId)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    rewardAdCallback?.onAdShowed()
                }
            }

        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned the reward: ${rewardItem.amount}")
            rewardAdCallback?.onUserEarnedReward(rewardItem)
        }
    }

    fun loadRewardedAdRequest() {
        val activity = activityRef?.get() ?: return

        if (rewardedAd != null) {
            Log.d(TAG, "Ad is already loaded.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                    rewardedAd = null
                    rewardAdCallback?.onAdFailedToLoad(adError)
                    // Tải lại quảng cáo sau 2 giây
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadRewardedAdRequest()
                    }, 2000)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad
                    rewardAdCallback?.onAdLoaded(ad)

                    rewardedAd?.setOnPaidEventListener { adValue ->
                        AdjustTracking.trackRevenueReward(
                            rewardedAd?.responseInfo?.loadedAdapterResponseInfo,
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
        rewardCallback: RewardCallback? = null,
    ) {
        activityRef = WeakReference(activity) // Giữ Activity một cách an toàn
        this.rewardAdCallback = rewardCallback
        this.adUnitId = adId
        this.adName = adName
        loadRewardedAndShow()
    }

    private fun loadRewardedAndShow() {
        val activity = activityRef?.get() ?: return

        if (rewardedAd != null) {
            Log.d(TAG, "Ad is already loaded.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                    rewardedAd = null
                    rewardAdCallback?.onAdFailedToLoad(adError)
                    // Tải lại quảng cáo sau 2 giây
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadRewardedAdRequest()
                    }, 2000)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad
                    rewardAdCallback?.onAdLoaded(ad)
                    rewardedAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                Log.d(TAG, "Ad was clicked.")
                                FirebaseLogEventUtils.logEventCommonWithName(
                                    activity,
                                    "reward_click"
                                )
                                rewardAdCallback?.onAdClicked()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad was dismissed.")
                                rewardedAd = null
                                rewardAdCallback?.onAdDismissed()
                                FirebaseLogEventUtils.logEventCommonWithName(
                                    activity,
                                    "reward_onAdDismissed"
                                )
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d(TAG, "Ad failed to show: ${adError.message}")
                                rewardedAd = null
                                rewardAdCallback?.onAdFailedToShow(adError)
                                FirebaseLogEventUtils.logEventCommonWithName(
                                    activity,
                                    "reward_onAdFailedToShow"
                                )
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Ad recorded an impression.")
                                FirebaseLogEventUtils.logEventCommonWithName(
                                    activity,
                                    "reward_onAdImpression"
                                )
                                rewardAdCallback?.onAdImpression()
                                val info = rewardedAd?.responseInfo?.loadedAdapterResponseInfo
                                AnalyticTracking.trackFanImpression(activity, adName, info?.adSourceId)
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.")
                                rewardAdCallback?.onAdShowed()
                                FirebaseLogEventUtils.logEventCommonWithName(
                                    activity,
                                    "reward_onAdShowedFullScreenContent"
                                )
                            }
                        }

                    rewardedAd?.show(activity) { rewardItem ->
                        Log.d(TAG, "User earned the reward: ${rewardItem.amount}")
                        rewardAdCallback?.onUserEarnedReward(rewardItem)
                    }

                    rewardedAd?.setOnPaidEventListener { adValue ->
                        AdjustTracking.trackRevenueReward(
                            rewardedAd?.responseInfo?.loadedAdapterResponseInfo,
                            adValue
                        )
                        AnalyticTracking.trackingRevenue(activity, adName, adValue)
                        FirebaseLogEventUtils.logEventCommonWithName(activity, "rewardedAd_onPaid")
                    }
                }
            })
    }

}