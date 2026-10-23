package com.b9ads.admob.interstitialAds.preload

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.SimpleAdmobAdManager
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.preload.model.InterPreloadModel
import com.b9ads.admob.loading.LoadingDialog
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.interstitial.InterstitialAdPreloader
import com.google.android.gms.ads.preload.PreloadCallbackV2
import com.google.android.gms.ads.preload.PreloadConfiguration

// 24/10/2025 Cuong fix #142
object InterPreload : LoadingDialog() {

    private const val TAG = "InterPreload"
    private var canShowNativeActivity: Boolean = false
    // intent chuyển màn sau khi show native full
    private var intentNextScreen: Intent? = null
    private var nativeFullActivity: String = ""

    fun setIntentNextScreen(intent: Intent?) {
        intentNextScreen = intent
    }

    fun setNativeFullActivity(cls: String) {
        nativeFullActivity = cls
    }

    fun startLoad(model: InterPreloadModel, interCallback: InterCallback? = null) {
        if (InterstitialAdPreloader.isAdAvailable(model.id)) {
            Log.v(TAG, "Inter preload before")
            interCallback?.onAdLoaded(null)
            return
        } else {
            InterstitialAdPreloader.pollAd(model.id)
        }
        if (model.isShow.not() || AdsHelper.checkShowAdByVersion().not()) {
            Log.v(TAG, "cancel load inter preload: ${model.isShow} ${AdsHelper.checkShowAdByVersion()}")
            interCallback?.onCancelToLoad()
            return
        }

        val configuration = PreloadConfiguration.Builder(model.id).apply {
            model.bufferSize?.let { setBufferSize(it) }
        }.build()
        val callback = object : PreloadCallbackV2() {
            override fun onAdPreloaded(p0: String, p1: ResponseInfo?) {
                super.onAdPreloaded(p0, p1)
                Log.v(TAG, "Inter preload loaded")
                interCallback?.onAdLoaded(null)
            }

            override fun onAdsExhausted(p0: String) {
                super.onAdsExhausted(p0)
                Log.v(TAG, "Inter preload Exhausted")
            }

            override fun onAdFailedToPreload(p0: String, p1: AdError) {
                super.onAdFailedToPreload(p0, p1)
                Log.v(TAG, "Inter preload fail to load: $p1")
                interCallback?.onAdFailedToLoad(null,p1)
            }
        }
        Log.v(TAG, "start load inter preload")
        InterstitialAdPreloader.start(model.id, configuration, callback)
    }

    fun showPreloadedAd(activity: Activity, rmcf: String, model: InterPreloadModel, interCallback: InterCallback? = null) {
        val ad = InterstitialAdPreloader.pollAd(model.id)
        if (ad == null || model.isShow.not() || AdsHelper.checkShowAdByVersion().not()) {
            Log.v(TAG, "cancel show $ad ${model.isShow} ${AdsHelper.checkShowAdByVersion()}")
            interCallback?.onCancelToShow()
            interCallback?.onNextAction()
            startNextActivity(activity, rmcf)
            return
        }
        ad.onPaidEventListener = OnPaidEventListener { adValue ->
            AdjustTracking.trackRevenueInter(ad.responseInfo.loadedAdapterResponseInfo, adValue)
            AnalyticTracking.trackingRevenue(activity, model.adName, adValue)
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                interCallback?.onAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.v(TAG, "callback onAdDismissedFullScreenContent")
                SimpleAdmobAdManager.isShowingInter = false
                interCallback?.onAdDismissedFullScreenContent()
                if (model.isShowNextActivityBeforeAd.not()) {
                    interCallback?.onNextAction()
                }
                startNextActivity(activity, rmcf)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.v(TAG, "callback onAdFailedToShowFullScreenContent: $adError")
                SimpleAdmobAdManager.isShowingInter = false
                interCallback?.onAdFailedToShowFullScreenContent(adError)
                if (model.isShowNextActivityBeforeAd.not()) {
                    interCallback?.onNextAction()
                }
                startNextActivity(activity, rmcf)
            }

            override fun onAdImpression() {
                Log.v(TAG, "callback onAdImpression")
                SimpleAdmobAdManager.isShowingInter = true
                SimpleAdmobAdManager.timeLastShowInter = System.currentTimeMillis()

                interCallback?.onAdImpression()
                AnalyticTracking.trackFanImpression(
                    activity,
                    model.adName,
                    ad.responseInfo.loadedAdapterResponseInfo?.adSourceId
                )
            }

            override fun onAdShowedFullScreenContent() {
                Log.v(TAG, "callback onAdShowedFullScreenContent")
                canShowNativeActivity = true
                SimpleAdmobAdManager.isShowingInter = true
                SimpleAdmobAdManager.timeLastShowInter = System.currentTimeMillis()

                interCallback?.onAdShowedFullScreenContent()
            }
        }
        showLoadingDialog(activity)
        Handler(Looper.getMainLooper()).postDelayed({
            if (model.isShowNextActivityBeforeAd) {
                interCallback?.onNextAction()
            }
            ad.show(activity)
        }, 1000L)

        Handler(Looper.getMainLooper()).postDelayed({
            hideLoadingDialog()
        }, 2000L)
    }

    private fun startNextActivity(context: Context, keyRemoteConfig: String) {
        Log.v(TAG, "startNextActivity: $canShowNativeActivity")
        if (canShowNativeActivity) {
            val success = startNativeActivity(context, intentNextScreen, keyRemoteConfig)
            if (success.not()) {
                intentNextScreen?.let { context.startActivity(it) }
            }
        } else {
            intentNextScreen?.let { context.startActivity(it) }
        }
        intentNextScreen = null
        canShowNativeActivity = false
    }

    private fun startNativeActivity(
        context: Context?,
        data: Intent?,
        keyRemoteConfig: String,
    ): Boolean {
        if (context == null) return false
        val cls = try {
            Class.forName(nativeFullActivity)
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "NativeFullActivity not found", e)
            return false
        }
        val intent = Intent(context, cls)
        intent.putExtra("KEY_NEXT_INTENT", data)
        intent.putExtra("KEY_INTER", keyRemoteConfig)
        context.startActivity(intent)
        return true
    }
}
