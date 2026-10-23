package com.b9ads.admob.interstitialAds.simple

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.SimpleAdmobAdManager
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.dialog.LoadingAdsDialog
import com.b9ads.enums.LoadState
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9utils.util.CheckInternet
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

open class SimpleInter0(

    //ad id
    private var adId: String,

    //ad name
    private var adName: String,

    ) {
    //log tag
    private val tag = "b9ads_SimpleInterClass"
    private val logEventAdName = makeKeyShortName(this.adName)
    private val logEventClassName = "SpInCl0"

    // Đợi hiển thị dialog 1s thì hiển thị quảng cáo
    private val AD_LOADING_TIME_DELAY_SHOW_ADS = 1000L

    // hiển thị dialog loading ad 2s thì off
    private val AD_LOADING_TIME_DELAY_FINISH = 2000L

    // Dialog loading
    private var loadingAdsDialog: LoadingAdsDialog? = null

    //is inter displaying
    //private var isInterShowing = false

    //for Fan sourceID list
    //for test ad "7068401028668408324" (admob test)
    private var listMediationFanSourceID =
        mutableListOf("10568273599589928883", "11198165126854996598")

    //properties
    private var isShow: Boolean = true
    private var isShowNextActivityBeforeAd: Boolean = true
    private var callback: InterCallback? = null

    //other ad load status


    // loaded ad
    protected var interstitialAd: InterstitialAd? = null

    // ad load status
    private var loadAdState = LoadState.NOT_LOADING
    // 02/10/2025 Cuong fix #135
    private var isShowImmediately: Boolean = false


    init {
        interstitialAd = null
        loadAdState = LoadState.NOT_LOADING
    }


    open fun setProperties(
        isShow: Boolean = true,
        isShowNextActivityBeforeAd: Boolean = true,
        interCallback: InterCallback? = null,
    ) {
        this.isShow = isShow
        this.isShowNextActivityBeforeAd = isShowNextActivityBeforeAd
        this.callback = interCallback
    }

    /**
     * call to load and show native by using layout in app
     */
    open fun loadInter(
        context: Context,
        isShowImmediately: Boolean = false,
    ) {
        this.isShowImmediately = isShowImmediately
        Log.v(tag, "function loadInter, ver: ver2, name: $adName")
        val logEventFunctionName = "${logEventClassName}_Ld"


        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        if (!isShow) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NotSh"
            )
            callback?.onCancelToLoad()
            return
        }

        if (!AdsHelper.checkShowAdByVersion()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NShVer"
            )
            Log.v(tag, "...load inter return, not show ad by version")
            callback?.onCancelToLoad()
            return
        }

        if (loadAdState == LoadState.LOADING) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NG_Lding"
            )
            callback?.onCancelToLoad()
            return
        }


        if (GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGUmp"
            )
            //return
        }

        if (CheckInternet.haveNetworkConnection(context).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNoNet"
            )
            //return
        }


        //condition load OK
        Log.v(tag, "...check load condition OK")

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "RqSetting"
        )

        //preparing for request
        loadAdState = LoadState.LOADING

        //for logging event firebase
        //val adNameShort: String = makeKeyShortName(this@SimpleInter0.adName)
        //val scrName: String = makeActivityShortName(context.javaClass.simpleName)

        //load ad
        Log.v(tag, "...load ad")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.v(tag, "callback adLoaded")
                    // ad is loaded
                    this@SimpleInter0.interstitialAd = interstitialAd

                    // loading state
                    this@SimpleInter0.loadAdState = LoadState.NOT_LOADING
                    Log.d(tag, "...request $logEventAdName success")

                    //Revenue
                    this@SimpleInter0.interstitialAd?.onPaidEventListener =
                        OnPaidEventListener { adValue ->
                            Log.d(
                                tag,
                                "callback OnPaidEventListener, ad loaded ${adValue.precisionType}__ ${adValue.currencyCode}"
                            )
                            callback?.onEarnRevenue(adValue)
                            AdjustTracking.trackRevenueInter(
                                this@SimpleInter0.interstitialAd?.responseInfo?.loadedAdapterResponseInfo,
                                adValue
                            )
                            AnalyticTracking.trackingRevenue(context, adName, adValue)
                            //event firebase log
                            FirebaseLogEventUtils.logEventAdCommon(
                                context, logEventAdName,
                                logEventFunctionName, "onPaid"
                            )
                        }
                    callback?.onAdLoaded(interstitialAd)

                    //log firebase
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "onLoaded"
                    )

                    //show inter ad if load and show
                    if (this@SimpleInter0.isShowImmediately) {
                        Log.v(tag, "...show inter ad immediately")
                        showInter(context)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Quảng cáo tải thất bại
                    this@SimpleInter0.interstitialAd = null

                    // loading status
                    this@SimpleInter0.loadAdState = LoadState.NOT_LOADING
                    Log.d(tag, "callback onAdFailedToLoad, Quảng cáo tải thất bại! $loadAdError")
                    callback?.onAdFailedToLoad(loadAdError)

                    //event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "onLdFail" + loadAdError.code.toString()
                    )
                }
            })

    }


    /**
     * show ad and preload other native
     */
    open fun showInter(
        context: Context,
    ) {

        //log event
        val logEventFunctionName = "${logEventClassName}_Sh"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )
        Log.v(tag, "Function show inter simple 2.0")

        if (!isShow) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NotSh"
            )
            Log.v(tag, "...Show inter cancel by isShow")
            callback?.onCancelToShow()
            callback?.onNextAction()
            return
        }

        //not show by version
        if (!AdsHelper.checkShowAdByVersion()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NShVer"
            )
            Log.v(tag, "...Show inter cancel")
            callback?.onCancelToShow()
            callback?.onNextAction()
            return
        }

        //check loaded ad
        if (this.interstitialAd == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_AdNull"
            )
            Log.v(tag, "...Show inter cancel by ad null")
            callback?.onCancelToShow()
            callback?.onNextAction()
            return
        }

        //for logging event firebase
        //val adNameShort: String = makeKeyShortName(adName)
        //val scrName: String = makeActivityShortName(context.javaClass.simpleName)


        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdClicked() {
                    callback?.onAdClicked()
//                    FirebaseLogEventUtils.logEventAdCommon(
//                        context, logEventAdName,
//                        logEventFunctionName, "onClk"
//                    )
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.v(tag, "callback onAdDismissedFullScreenContent")
                    //set isShowInter = false -> not display inter ad
                    //isInterShowing = false
                    SimpleAdmobAdManager.isShowingInter = false
                    callback?.onAdDismissedFullScreenContent()
                    if (!isShowNextActivityBeforeAd) {
                        callback?.onNextAction()
                        Log.v(
                            tag,
                            "...call next action because not call it before (isShowNextActivityBeforeAd = false)"
                        )
                    }

                    // setting time interval
                    //timeBetween = System.currentTimeMillis()

                    Log.v(tag, "...ad $adName closed.")
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "onDism"
                    )

                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.v(tag, "callback onAdFailedToShowFullScreenContent")
                    //set isShowInter = false -> not display inter ad
                    //isInterShowing = false
                    SimpleAdmobAdManager.isShowingInter = false
                    callback?.onAdFailedToShowFullScreenContent(adError)
                    if (!isShowNextActivityBeforeAd) {
                        callback?.onNextAction()
                        Log.v(
                            tag,
                            "...call next action because not call it before (isShowNextActivityBeforeAd = false)"
                        )
                    }

                    // re setting interval time
//                    timeBetween = System.currentTimeMillis()

                    Log.d(tag, "...ad $adName fail to show: ${adError.message}")

                    //event logging
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "onShFail" + adError.code.toString()
                    )

                }

                override fun onAdImpression() {
                    Log.v(tag, "callback onAdImpression")
                    SimpleAdmobAdManager.isShowingInter = true
                    SimpleAdmobAdManager.timeLastShowInter = System.currentTimeMillis()

                    callback?.onAdImpression()
                    Log.d(tag, "...Quảng cáo $adName đã ghi nhận impression.")
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "onIm"
                    )
                    AnalyticTracking.trackFanImpression(
                        context,
                        adName,
                        interstitialAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId
                    )
                    if (isMediationFanSource()) {
                        FirebaseLogEventUtils.logEventAdCommon(
                            context, logEventAdName,
                            logEventFunctionName, "FFF"
                        )
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    SimpleAdmobAdManager.isShowingInter = true
                    SimpleAdmobAdManager.timeLastShowInter = System.currentTimeMillis()

                    callback?.onAdShowedFullScreenContent()
                    Log.d(tag, "...Quảng cáo $adName đang hiển thị.")
//                    FirebaseLogEventUtils.logEventAdCommon(
//                        context, logEventAdName,
//                        logEventFunctionName, "onShFull"
//                    )
                }
            }

        // Hiển thị dialog loading
        showDialogLoadingAds(context as Activity)

        Handler(Looper.getMainLooper()).postDelayed({

            //load next screen before
            if (isShowNextActivityBeforeAd) {
                callback?.onNextAction()
            }

            //roi hien thị quảng cáo
            interstitialAd?.show(context)

        }, AD_LOADING_TIME_DELAY_SHOW_ADS)
        cancelDialogLoadingAds(context)

        //interstitialAd?.show(context as Activity)
        //log firebase
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )

    }

    // 02/10/2025 Cuong fix #135
    open fun waitAndShowInter(context: Context) {
        if (interstitialAd != null) {
            showInter(context)
        } else if (loadAdState == LoadState.LOADING) {
            isShowImmediately = true
            showDialogLoadingAds(context)
        } else {
            callback?.onNextAction()
        }
    }

    private fun isMediationFanSource(): Boolean {
        val strAdSourceID =
            this.interstitialAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId ?: "0"

        return strAdSourceID in listMediationFanSourceID
        //test Fan
        //return true
    }


    open fun destroy() {
        interstitialAd = null
    }


    // show dialog loading
    // 02/10/2025 Cuong fix #135
    private fun showDialogLoadingAds(context: Context) {
        try {
            if (loadingAdsDialog?.isShowing == true) {
                return
            }
            loadingAdsDialog = LoadingAdsDialog(context)
            loadingAdsDialog?.show()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelDialogLoadingAds(activity: Activity) {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    loadingAdsDialog = if ((loadingAdsDialog != null
                                && loadingAdsDialog?.window != null
                                && loadingAdsDialog?.window?.decorView?.isAttachedToWindow == true)
                        && !activity.isDestroyed
                        && loadingAdsDialog?.isShowing == true
                    ) {
                        loadingAdsDialog?.dismiss()
                        null
                    } else {
                        null
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }, AD_LOADING_TIME_DELAY_FINISH)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}