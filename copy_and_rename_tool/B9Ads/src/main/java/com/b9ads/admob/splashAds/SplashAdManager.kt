/**
 * ANH: xem lai khi man hinh bi khoa xong build, mo khoa ra khong chay duoc
 *
 */
package com.b9ads.admob.splashAds

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.b9ads.admob.splashAds.callback.SplashCallback
import com.b9ads.admob.splashAds.type.SplashType
import com.b9ads.dialog.LoadingAdsDialog
import com.b9ads.enums.LoadState
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9utils.util.CheckInternet
import com.b9utils.util.FirebaseLogEventUtils
import kotlin.random.Random

class SplashAdManager(

    private var activity: Activity,

    // id quảng cáo app open
    private var idAppOpen: String,

    // id quảng cáo inter
    private var idInter: String,

    // isShow App open
    private var isShowAppOpen: Boolean = false,

    // isShow Inter
    private var isShowInter: Boolean = true,

    //remote config splash - max_loading_time
    private var adMaxLoadingTime: Long = 25000L,

    //rate of app_open, sum 100
    private var rate: Int = 30,

    // lifecycle dùng để reload quảng cáo
    lifecycle: Lifecycle,

    // callback
    private var splashCallback: SplashCallback? = null,

    ) {
    // Log cat
    private val tag = "SplashManagerTag"
    private val adSplashEventName = "adSpl"

    //display loading ad screen, delay x(s) after call show ad
    private val TIME_DELAY_SHOW_ADS = 1000L

    //display loading ad screen, delay x(s) after close loading ad screen
    private val TIME_DELAY_CANCEL_DIALOG_LOADING = 2000L

    //loading dialog
    private var loadingAdsDialog: LoadingAdsDialog? = null

    // ad loading or not loading status
    //using both for ad open and inter
    private var loadState = LoadState.NOT_LOADING

    // inter ad
    private var interstitialAd: InterstitialAd? = null

    // app open loaded ad
    private var appOpenAd: AppOpenAd? = null

    // ad type
    private var splashType = SplashType.NOT_SET

    //for check call ad from load and show or from background
    private var isPassResume: Boolean = false
    private var isRunOnBackground: Boolean = false

    //private var isLoadedAd: Boolean = false
    //private var isShowedAd: Boolean = false


    // Handle 1s delay hiển thị quảng cáo
    private var handlerShowAds: Handler? = null

    // Handle time out dung de dem max properties timeOut(s)
    //het timeOut(s) set isTimeOut = true
    //relation adMaxLoadingTime
    //private var handlerTimeOutLoadAd: Handler? = null

    //max load ad timeOut(s)
    //relation adMaxLoadingTime
    private var isTimeOut = false

    private var interAdName = "splash_inter"
    private var openAdName = "splash_open"

    init {
        // regis observer to lifecycle
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                //lifecycle event
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        Log.d(tag, "ON_RESUME")
                        isPassResume = true
                        isRunOnBackground = false
                        checkShowAdsFromBackground()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d(tag, "ON_PAUSE")
                        //cancel handle show ad
                        isRunOnBackground = true
                        handlerShowAds?.removeCallbacksAndMessages(null)
                    }

                    Lifecycle.Event.ON_STOP -> {
                        Log.d(tag, "ON_STOP")
                        //cancel handle show ad
                        isRunOnBackground = true
                        handlerShowAds?.removeCallbacksAndMessages(null)
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        removeAll(source, this)
                        Log.d(tag, "ON_DESTROY")
                    }

                    else -> {}
                }
            }
        })
    }

    fun loadAndShowAdSplash() {

        val funNameLogEvent = "MngLdnSh"

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "BeginF"
        )

        //set ad type
        setAdType()

        //event for ad type
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "Tp${splashType}"
        )

        // Load quảng cáo
        when (splashType) {
            SplashType.APP_OPEN -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    funNameLogEvent, "LdNShOp"
                )
                // Khởi tạo time out
                //initTimeOut()
                loadAppOpen()
            }

            SplashType.INTER -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    funNameLogEvent, "LdNShIn"
                )
                // Khởi tạo time out
                //initTimeOut()
                loadInter()
            }

            else -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    funNameLogEvent, "LdNShNone"
                )
                splashCallback?.onNextAction(true)
            }
        }
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "EndF"
        )
    }

    private fun setAdType() {
        val funNameLogEvent = "MngGetType"

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "BeginF"
        )

        if (!isShowAppOpen && !isShowInter) {
            splashType = SplashType.NO_ADS
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                funNameLogEvent, "01NG_NotShAll"
            )

        } else if (isShowAppOpen && !isShowInter) {
            Log.d(tag, "Hiển thị app open")
            splashType = SplashType.APP_OPEN
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                funNameLogEvent, "CaseOP"
            )

        } else if (isShowInter && !isShowAppOpen) {
            Log.d(tag, "Hiển thị inter")
            splashType = SplashType.INTER
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                funNameLogEvent, "CaseInter"
            )

        } else if (isShowAppOpen && isShowInter) {
            Log.d(tag, "rate: $rate")
            val random = Random.nextInt(0, 101)
            Log.d(tag, "random: $random")
            splashType = if (random <= rate) {
                Log.d(tag, "Hiển thị app open")
                SplashType.APP_OPEN
            } else {
                Log.d(tag, "Hiển thị inter")
                SplashType.INTER
            }
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                funNameLogEvent, "CaseRan${splashType}"
            )
        } else {
            splashType = SplashType.NO_ADS
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                funNameLogEvent, "CaseNoAd"
            )
        }

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "Tp${splashType}"
        )

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            funNameLogEvent, "EndF"
        )
    }

//    private fun initTimeOut() {
//        handlerTimeOutLoadAd = Handler(Looper.getMainLooper())
//        FirebaseLogEventUtils.logEventAdCommon(
//            activity, adSplashEventName,
//            "MngTiOut", "Wait"
//        )
//        handlerTimeOutLoadAd?.postDelayed({
//            //het timeout ma van null thi set isTimeOut = true
//            if (interstitialAd == null && appOpenAd == null) {
//                FirebaseLogEventUtils.logEventAdCommon(
//                    activity, adSplashEventName,
//                    "MngTiOut", "LoadedNull"
//                )
//                isTimeOut = true
//                splashCallback?.onNextAction(true)
//
//            } else {
//                FirebaseLogEventUtils.logEventAdCommon(
//                    activity, adSplashEventName,
//                    "MngTiOut", "CallShAdFrTiOu"
//                )
//                checkShowAdsFromBackground("TiOu")
//            }
//        }, adMaxLoadingTime)
//    }

//    fun cancelHandlerTimeOut() {
//        try {
//            handlerTimeOutLoadAd?.removeCallbacksAndMessages(null)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    private fun loadInter() {
        val logEventFunctionName = "MngLdIn"
        FirebaseLogEventUtils.logEventAdCommon(
            activity,
            adSplashEventName,
            logEventFunctionName,
            "BeginF"
        )

        //check internet
        if (CheckInternet.haveNetworkConnection(activity).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity,
                adSplashEventName,
                logEventFunctionName,
                "NG_NoNet"
            )
            //splashCallback?.onNextAction(false)
            //return
        }
        //check can request
        val canRequestAds: Boolean =
            GoogleMobileAdsConsentManager.getInstance(activity).canRequestAds
        if (!canRequestAds) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity,
                adSplashEventName,
                logEventFunctionName,
                "NG_CannotReq"
            )
            //splashCallback?.onNextAction(false)
            //return
        }

        if (idInter.isEmpty()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity,
                adSplashEventName,
                logEventFunctionName,
                "NG_IDInNull"
            )
            //splashCallback?.onNextAction(false)
            //return
        }

        if (this.activity == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity,
                adSplashEventName,
                logEventFunctionName,
                "NG_ActyNull"
            )
            //splashCallback?.onNextAction(false)
            //return
        }
        FirebaseLogEventUtils.logEventAdCommon(
            activity,
            adSplashEventName,
            logEventFunctionName,
            "BgLd"
        )
        //begin loading
        loadState = LoadState.LOADING

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            idInter,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity,
                        adSplashEventName, logEventFunctionName,
                        "onAdLdOK"
                    )
                    //loaded ad processing
                    loadState = LoadState.NOT_LOADING
                    this@SplashAdManager.interstitialAd = interstitialAd

                    //callback
                    splashCallback?.onAdLoaded(null, interstitialAd, false)
                    Log.d(tag, "Quảng cáo $idInter tải thành công!")


                    if (!isPassResume) {
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity, adSplashEventName,
                            logEventFunctionName, "onAdLdOKNotPaRs"
                        )
                        return
                    }
                    if (isRunOnBackground) {
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity, adSplashEventName,
                            logEventFunctionName, "onAdLdOKRunOnBg"
                        )
                        return
                    }

                    //check internet
                    if (CheckInternet.haveNetworkConnection(activity).not()) {
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity, adSplashEventName,
                            logEventFunctionName, "onAdLdOKNoNet"
                        )
                    }

                    //event show ad
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "CallShInFrLd"
                    )

                    //Revenue
                    this@SplashAdManager.interstitialAd?.onPaidEventListener =
                        OnPaidEventListener { adValue ->
                            Log.d(tag,"Quảng cáo được tải ${adValue.precisionType}__ ${adValue.currencyCode}")
                            splashCallback?.onEarnRevenue(adValue)
                            AdjustTracking.trackRevenueInter(this@SplashAdManager.interstitialAd?.responseInfo?.loadedAdapterResponseInfo,adValue)
                            AnalyticTracking.trackingRevenue(activity, interAdName, adValue)
                            //event firebase log
                            FirebaseLogEventUtils.logEventAdCommon(
                                activity,
                                adSplashEventName,
                                logEventFunctionName,
                                "onPaid"
                            )
                        }
                    //show ad
                    showInter()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)

                    //log event
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdLdFail${loadAdError.code}"
                    )

                    //set load state
                    loadState = LoadState.NOT_LOADING
                    this@SplashAdManager.interstitialAd = null

                    splashCallback?.onAdFailedToLoad(loadAdError, false)
                    Log.d(tag, "Quảng cáo $idInter tải thất bại!")
                }
            })
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "EndF"
        )
    }

    private fun loadAppOpen() {
        val logEventFunctionName = "MngLdOp"

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "BeginF"
        )

        //check internet
        if (CheckInternet.haveNetworkConnection(activity).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NG_NoNet"
            )
            splashCallback?.onNextAction(true)
            return
        }
        //check can request
        val canRequestAds: Boolean =
            GoogleMobileAdsConsentManager.getInstance(activity).canRequestAds
        if (!canRequestAds) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NG_CannotReq"
            )
            splashCallback?.onNextAction(true)
            return
        }

        if (idAppOpen.isEmpty()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NG_IDInNull"
            )
            splashCallback?.onNextAction(true)
            return
        }

        if (this.activity == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity,
                adSplashEventName,
                logEventFunctionName,
                "NG_ActyNull"
            )
            splashCallback?.onNextAction(false)
            return
        }


        // set status is LOADING
        loadState = LoadState.LOADING

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(activity, idAppOpen, adRequest, object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunctionName, "onAdLdOK"
                )
                loadState = LoadState.NOT_LOADING

                // if timeout not show ad
                if (isTimeOut) {
//                    this@SplashAdManager.appOpenAd = null
//                    Log.d(tag, "Ad loaded but process was cancelled.")
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "OnLdOKTiOut"
                    )
                    return
                }
                //isLoadedAd = true

                //if not timeout, cancel timeout handler
                //cancelHandlerTimeOut()

                // loaded ad processing
                this@SplashAdManager.appOpenAd = appOpenAd
                //isLoadedAd = true
                splashCallback?.onAdLoaded(appOpenAd, null, true)
                Log.d(tag, "Quảng cáo $idAppOpen tải thành công!")



                //event show ad
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunctionName, "CallShOpFrLd"
                )
                //Revenue
                this@SplashAdManager.appOpenAd?.onPaidEventListener =
                    OnPaidEventListener { adValue ->
                        Log.d(tag, "Quảng cáo tra revenue")
                        splashCallback?.onEarnRevenue(adValue)
                        AdjustTracking.trackRevenueOpen(this@SplashAdManager.appOpenAd?.responseInfo?.loadedAdapterResponseInfo,adValue)
                        AnalyticTracking.trackingRevenue(activity, openAdName, adValue)
                        //event firebase log
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity,
                            adSplashEventName,
                            logEventFunctionName,
                            "onPaid"
                        )
                    }
                //show ad
                showAppOpen()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                loadState = LoadState.NOT_LOADING

                //log event
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunctionName, "onAdLdFail${loadAdError.code}"
                )

                // Nếu timeout
//                if (isTimeOut) {
//                    this@SplashAdManager.appOpenAd = null
//                    isLoadedAd = false
//                    isShowedAd = false
//                    Log.d(tag, "Đã hết thời gian đợi")
//                    return
//                }

//                this@SplashAdManager.appOpenAd = null
//                isLoadedAd = false
//                isShowedAd = false
                splashCallback?.onAdFailedToLoad(loadAdError, true)
                Log.d(tag, "Quảng cáo $idAppOpen tải thành công!")
            }
        })

        FirebaseLogEventUtils.logEventAdCommon(
            activity,
            adSplashEventName,
            logEventFunctionName,
            "EndF"
        )
    }

    private fun showInter() {

        val logEventFunctionName = "MngShIn"
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "BeginF"
        )

        if (interstitialAd == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGAdNull"
            )
            return
        }

        //check internet
        if (CheckInternet.haveNetworkConnection(activity).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGNoNet"
            )
            //return
        }


        if (!isPassResume) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGNotPassRs"
            )
            //return
        }

        if (isRunOnBackground) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGRunBG"
            )
            //return
        }

        //check finish
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "SettingSh"
        )

        // setting callback
        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    splashCallback?.onAdDismissedFullScreenContent(false)
                    splashCallback?.onNextAction(false)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdDis"
                    )
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    //reset param
                    interstitialAd = null
                    loadState = LoadState.NOT_LOADING

                    //callback
                    splashCallback?.onAdFailedToShowFullScreenContent(adError, false)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdFailSh${adError.code}"
                    )
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                    splashCallback?.onAdShowedFullScreenContent(false)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdShFull"
                    )
                }

                override fun onAdImpression() {
                    splashCallback?.onAdImpression(false)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdIm"
                    )
                    val info = interstitialAd?.responseInfo?.loadedAdapterResponseInfo
                    AnalyticTracking.trackFanImpression(activity, interAdName, info?.adSourceId)
                }

                override fun onAdClicked() {
                    splashCallback?.onAdClicked(false)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdClk"
                    )
                }
            }
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "CallDlgLd"
        )
        showDialogLoadingAds()

        handlerShowAds = Handler(Looper.getMainLooper())

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "CallShWa"
        )
        handlerShowAds?.postDelayed({
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "CallShDo"
            )
            interstitialAd?.show(activity)
            cancelDialogLoadingAds()
        }, TIME_DELAY_SHOW_ADS)

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "EndF"
        )

    }

    private fun showAppOpen() {
        val logEventFunctionName = "MngShOp"
        FirebaseLogEventUtils.logEventAdCommon(
            activity,
            adSplashEventName,
            logEventFunctionName,
            "BeginF"
        )

        if (appOpenAd == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGAdNull"
            )
            return
        }

        //check internet
        if (CheckInternet.haveNetworkConnection(activity).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGNoNet"
            )
            return
        }

        if (!isPassResume) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGNotPassRs"
            )
            return
        }

        if (isRunOnBackground) {
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "NGRunBG"
            )
            return
        }


        //check finish
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "SettingSh"
        )

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    splashCallback?.onAdDismissedFullScreenContent(true)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdDism"
                    )
                    splashCallback?.onNextAction(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    //reset
                    appOpenAd = null
                    loadState = LoadState.NOT_LOADING
//                    isLoadedAd = false
//                    isShowedAd = false

                    //callback
                    splashCallback?.onAdFailedToShowFullScreenContent(adError, true)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdFailSh${adError.code}"
                    )

                    //reload ad
                    //loadAndShowAdSplash()
                }

                override fun onAdShowedFullScreenContent() {
                    //isShowedAd = true
                    appOpenAd = null
                    splashCallback?.onAdShowedFullScreenContent(true)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdShFull"
                    )
                }

                override fun onAdImpression() {
                    //isShowedAd = true
                    splashCallback?.onAdImpression(true)
                    val info = appOpenAd?.responseInfo?.loadedAdapterResponseInfo
                    AnalyticTracking.trackFanImpression(activity, openAdName, info?.adSourceId)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdIm"
                    )
                }

                override fun onAdClicked() {
                    splashCallback?.onAdClicked(true)
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunctionName, "onAdClk"
                    )
                }
            }

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "CallDlgLd"
        )
        showDialogLoadingAds()

        handlerShowAds = Handler(Looper.getMainLooper())
        handlerShowAds?.postDelayed({
            FirebaseLogEventUtils.logEventAdCommon(
                activity, adSplashEventName,
                logEventFunctionName, "CallSh"
            )
            appOpenAd?.show(activity)
            cancelDialogLoadingAds()
        }, TIME_DELAY_SHOW_ADS)

        //log end function
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunctionName, "EndF"
        )
    }


    //this case load ad from background
    //case1: load ad finish but not move to resume yet
    //case2: back to splash screen from home screen ( splash loading=> press home => back to splash screen)
    private fun checkShowAdsFromBackground() {
        val logEventFunName = "MngShAdFrRs"
        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunName, "BeginF"
        )
        when (splashType) {
            SplashType.APP_OPEN -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunName, "Op"
                )
                showAppOpen()
            }

            SplashType.INTER -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunName, "In"
                )

                if (!isPassResume) {
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunName, "In_NGNotPassRs"
                    )
                    return
                }
                if (isRunOnBackground) {
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunName, "In_NGOnBg"
                    )
                    return
                }
                if (this.interstitialAd == null) {
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunName, "In_NGAdNull"
                    )
                    return
                }

                //check internet
                if (CheckInternet.haveNetworkConnection(activity).not()) {
                    FirebaseLogEventUtils.logEventAdCommon(
                        activity, adSplashEventName,
                        logEventFunName, "In_NGNoNet"
                    )
                }

                //event show ad
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunName, "In_CallShInFrRs"
                )
                showInter()
            }

            SplashType.NO_ADS -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunName, "NoAd"
                )
            }

            SplashType.NOT_SET -> {
                FirebaseLogEventUtils.logEventAdCommon(
                    activity, adSplashEventName,
                    logEventFunName, "NotSet"
                )
            }
        }

        FirebaseLogEventUtils.logEventAdCommon(
            activity, adSplashEventName,
            logEventFunName, "EndF"
        )
    }

    // Hiển thị dialog loading
    private fun showDialogLoadingAds() {
        try {
            activity.runOnUiThread {
                loadingAdsDialog = LoadingAdsDialog(activity)
                loadingAdsDialog?.show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Tắt dialog loading
    private fun cancelDialogLoadingAds() {
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
            }, TIME_DELAY_CANCEL_DIALOG_LOADING)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeAll(source: LifecycleOwner, lifecycleEventObserver: LifecycleEventObserver) {
        try {
            source.lifecycle.removeObserver(lifecycleEventObserver)
            handlerShowAds?.removeCallbacksAndMessages(null)
            //handlerTimeOutLoadAd?.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}