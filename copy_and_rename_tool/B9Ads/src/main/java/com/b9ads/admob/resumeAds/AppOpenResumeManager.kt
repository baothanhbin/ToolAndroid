/**
 * remote config ad_id va is_show call function to setting
 * is_show already using for check before load and show function
 */

package com.b9ads.admob.resumeAds

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.SimpleAdmobAdManager
import com.b9ads.admob.interstitialAds.InterManager
import com.b9ads.admob.resumeAds.callback.OpenCallback
import com.b9ads.admob.resumeAds.model.ResumeModel
import com.b9ads.dialog.LoadingAdsDialog
import com.b9ads.enums.LoadState
import com.b9ads.fan.interstitialAds.InterFanManager
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.appopen.AppOpenAdPreloader
import com.google.android.gms.ads.preload.PreloadCallbackV2
import com.google.android.gms.ads.preload.PreloadConfiguration
import java.lang.ref.WeakReference
import java.util.Date

object AppOpenResumeManager {
    private const val TAG = "b9ads_AppOpenResumeManagerTag"

    // ad loading dialog showing time
    private const val AD_LOADING_TIME_DELAY_SHOW_ADS: Long = 1000L

    //ad timeout
    //after this time ad is unavailable
    private const val AD_LOADED_TIMEOUT_HOURS: Long = 4

    private var appOpenAd: AppOpenAd? = null

    private var loadState = LoadState.NOT_LOADING

    //is available to show
    //ex: splash is disable ad resume
    private var isEnable = true

    //check is showing ad
    var isShowingAd = false

    // time loaded ad resume
    private var timeAdLoaded: Long = 0

    //show ads after loaded
    private var showImmediately = false

    private var loadingAdsDialog: LoadingAdsDialog? = null

    private var activity: WeakReference<Activity>? = null

    private var resumeModel: ResumeModel? = null

    fun getIsShow(): Boolean {
        return resumeModel?.isShow == true
    }

    fun setProperties(
        model: ResumeModel
    ) {
        this.resumeModel = model
    }

    // Enable/ disable ad resume
    fun setEnableAdsResume(enable: Boolean) {
        this.isEnable = enable
    }

    // check ad is available
    private fun isAdLoadedAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(AD_LOADED_TIMEOUT_HOURS)
    }

    // check timeout for ad
    private fun wasLoadTimeLessThanNHoursAgo(hour: Long): Boolean {
        val dateDifference: Long = Date().time - timeAdLoaded
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * hour
    }

    // 27/10/2025 Cuong fix #147
    fun loadAd(context: Context, callback: OpenCallback? = null) {
        val resumeModel = this.resumeModel
        if (resumeModel == null) {
            callback?.onCancelLoad()
            return
        }
        if (resumeModel.preload.isUse) {
            preloadAdResume(resumeModel, callback)
        } else {
            loadAdResume(context, callback)
        }
    }

    // 27/10/2025 Cuong fix #147
    fun showAd(
        activity: Activity,
        showImmediately: Boolean = false,
        callback: OpenCallback? = null,
    ) {
        if (resumeModel?.preload?.isUse == true && appOpenAd == null) {
            appOpenAd = AppOpenAdPreloader.pollAd(resumeModel?.preload?.id ?: "")
        }
        showAdResume(activity, showImmediately, callback)
    }

    // load ad
    private fun loadAdResume(context: Context, callback: OpenCallback? = null) {

        val logEventAdName = makeKeyShortName("ad_resume")
        val logEventScreenName = makeActivityShortName(context.javaClass.simpleName.toString())
        val logFunName = "Ld"
        val logPrefix = "${logEventAdName}_${logEventScreenName}_${logFunName}"

        //log event
        FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_Begin")

        //UMP
        if (GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGUmp")
            callback?.onCancelLoad()
            return
        }

        if (resumeModel?.id.isNullOrBlank()) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGIdNull")
            callback?.onCancelLoad()
            return
        }

        if (AdsHelper.checkShowAdByVersion().not()) {
            callback?.onCancelLoad()
            return
        }

        //now show
        if (resumeModel?.isShow != true) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGNotSh")
            callback?.onCancelLoad()
            return
        }

        //is showing inter
        if (!SimpleAdmobAdManager.canRequestResume()) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGMngNotRq")
            callback?.onCancelLoad()
            return
        }


        //neu da load roi
        if (isAdLoadedAvailable()) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGNotLdOrTiOu")
            callback?.onCancelLoad()
            return
        }

        if (loadState == LoadState.LOADING) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logPrefix}_NGLoading")
            callback?.onCancelLoad()
            return
        }


        loadState = LoadState.LOADING
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            resumeModel?.id ?: "",
            request,
            object : AppOpenAdLoadCallback() {

                //load success cong
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadState = LoadState.NOT_LOADING
                    timeAdLoaded = Date().time
                    Log.d(TAG, "onAdLoaded.")
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventScreenName, "OnLoaded"
                    )
                    //Revenue
                    appOpenAd?.onPaidEventListener = OnPaidEventListener { adValue ->
                        Log.d(TAG, "onPaidEvent: $adValue")
                        callback?.onEarnRevenue(adValue)
                        AdjustTracking.trackRevenueOpen(
                            appOpenAd?.responseInfo?.loadedAdapterResponseInfo,
                            adValue
                        )
                        AnalyticTracking.trackingRevenue(context, resumeModel?.name ?: "", adValue)
                        //event firebase log
                        FirebaseLogEventUtils.logEventAdCommon(
                            context, logEventAdName,
                            logEventScreenName, "onPaid"
                        )
                    }
                    callback?.onAdLoaded()
                    val activity = activity?.get()
                    if (showImmediately && activity != null) {
                        showImmediately = false
                        showAdResume(activity, false, callback)
                    }
                }

                //load failed
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadState = LoadState.NOT_LOADING
                    Log.d(TAG, "onAdFailedToLoad: " + loadAdError.message)
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventScreenName, "onLdFail${loadAdError.code}"
                    )
                    callback?.onAdFailedToLoad(loadAdError)
                    hideLoadingDialog()
                }
            },
        )
    }

    // show ad
    private fun showAdResume(
        activity: Activity,
        showImmediately: Boolean = false,
        callback: OpenCallback? = null,
    ) {
        this.activity = WeakReference(activity)
        val logEventAdName = makeKeyShortName("ad_resume")
        val logEventScreenName = makeActivityShortName(activity.javaClass.simpleName.toString())
        val logFunName = "Sh"
        val logPrefix = "${logEventAdName}_${logEventScreenName}_${logFunName}"

        //log
        FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_Begin")

        //not enable
        if (isEnable.not()) {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGNotEnable")
            hideLoadingAndNextAction(callback)
            return
        }

        //not show
        if (resumeModel?.isShow != true) {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGNotSh")
            hideLoadingAndNextAction(callback)
            return
        }

        //if inter is showing then not show
        if (InterManager.isShowingInter() || InterFanManager.isShowingInter()) {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGInShowing")
            hideLoadingAndNextAction(callback)
            return
        }

        if (AdsHelper.checkShowAdByVersion().not()) {
            hideLoadingAndNextAction(callback)
            return
        }

        //other ad showing check by admob manager
        if (!SimpleAdmobAdManager.canShowResume()) {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGMngAdShowing")
            hideLoadingAndNextAction(callback)
            return
        }

        //if other ad resume is showing
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGRsShowing")
            hideLoadingAndNextAction(callback)
            return
        }

        // Đảm bảo chỉ khởi tạo 1 loading dialog
        if (loadingAdsDialog == null) {
            loadingAdsDialog = LoadingAdsDialog(activity)
        }

        //if not available then preload for next show
        if (isAdLoadedAvailable().not()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            FirebaseLogEventUtils.logEventCommonWithName(activity, "${logPrefix}_NGAdUnAvail")
            if (showImmediately && loadState == LoadState.LOADING) {
                this.showImmediately = true
                //Nếu quảng cáo chưa kịp tải thì sẽ hiện loading chờ quảng cáo tải xong thì hiện
                showLoadingDialog()
            } else {
                hideLoadingAndNextAction(callback)
            }
            return
        }

        //check ok, begin show ad
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked.")
                callback?.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(TAG, "onAdImpression.")
                callback?.onAdImpression()
                AnalyticTracking.trackFanImpression(
                    activity,
                    resumeModel?.name ?: "",
                    appOpenAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId
                )
                FirebaseLogEventUtils.logEventAdCommon(
                    activity,
                    logEventAdName,
                    logEventScreenName,
                    "onIm"
                )
            }

            // khi tat quang cao
            override fun onAdDismissedFullScreenContent() {
                //end then other processing
                super.onAdDismissedFullScreenContent()
                //preload for next show

                appOpenAd = null
                isShowingAd = false

                Log.d(TAG, "onAdDismissedFullScreenContent.")

                //23/09/2025 fix crash firebase
                hideLoadingAndNextAction(callback)
                callback?.onAdDismissedFullScreenContent()
            }

            // fail to show
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)

                //reload ad
                appOpenAd = null
                isShowingAd = false

                Log.d(TAG, "onAdFailedToShowFullScreenContent: ${p0.code}")

                hideLoadingAndNextAction(callback)
                callback?.onAdFailedToShowFullScreenContent(p0)

                //event logging
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, logEventAdName,
                    logEventScreenName, "onShFail${p0.code}"
                )

            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.d(TAG, "onAdShowedFullScreenContent.")
                Handler(Looper.getMainLooper()).postDelayed({
                    hideLoadingDialog()

                    callback?.onAdShowedFullScreenContent()
                }, AD_LOADING_TIME_DELAY_SHOW_ADS)
            }
        }

        showLoadingDialog()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentState = ProcessLifecycleOwner.get().lifecycle.currentState
            val activityShowed = currentState.isAtLeast(Lifecycle.State.STARTED).not()
            if (activityShowed) {
                Log.d(TAG, "The app is not in a state to show ads.")
                FirebaseLogEventUtils.logEventCommonWithName(
                    activity,
                    "${logPrefix}_NGAppNotStarted"
                )
                hideLoadingAndNextAction(callback)
            } else if (!(activity.isFinishing || activity.isDestroyed)) {
                isShowingAd = true
                appOpenAd?.show(activity)
            }

        }, 1000)
    }

    // 27/10/2025 Cuong fix #147
    private fun preloadAdResume(resumeModel: ResumeModel, callback: OpenCallback? = null) {
        if (AppOpenAdPreloader.isAdAvailable(resumeModel.id)) {
            Log.v(TAG, "Open preload before")
            callback?.onAdLoaded()
            return
        } else {
            AppOpenAdPreloader.pollAd(resumeModel.id)
        }
        if (resumeModel.isShow.not()) {
            callback?.onCancelLoad()
            Log.v(TAG, "cancel preload open: ${resumeModel.isShow}")
            return
        }

        if (AdsHelper.checkShowAdByVersion().not()) {
            callback?.onCancelLoad()
            Log.v(TAG, "cancel load by version")
            return
        }

        val config = PreloadConfiguration.Builder(resumeModel.preload.id).apply {
            resumeModel.preload.bufferSize?.let { setBufferSize(it) }
        }.build()
        val callback = object : PreloadCallbackV2() {
            override fun onAdPreloaded(p0: String, p1: ResponseInfo?) {
                super.onAdPreloaded(p0, p1)
                Log.v(TAG, "open ad preloaded: ")
                timeAdLoaded = Date().time
                callback?.onAdLoaded()
            }

            override fun onAdFailedToPreload(p0: String, p1: AdError) {
                super.onAdFailedToPreload(p0, p1)
                Log.v(TAG, "open ad failed to preload: $p1")
                callback?.onAdFailedToLoad(adError = p1)
            }
        }
        AppOpenAdPreloader.start(resumeModel.preload.id, config, callback)
    }

    private fun hideLoadingAndNextAction(callback: OpenCallback?) {
        hideLoadingDialog()
        callback?.onNextAction()
    }

    private fun showLoadingDialog() {
        try {
            if (loadingAdsDialog?.isShowing == false) {
                loadingAdsDialog?.show()
            }
        } catch (_: Exception) {

        }
    }

    private fun hideLoadingDialog() {
        try {
            if (loadingAdsDialog?.isShowing == true) {
                loadingAdsDialog?.dismiss()
            }
            loadingAdsDialog = null
        } catch (_: Exception) {

        }
    }
}
