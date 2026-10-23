package com.b9ads.admob.interstitialAds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.base.InterSingle
import com.b9ads.dialog.LoadingAdsDialog
import com.b9ads.enums.LoadState
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterManager {
    private const val TAG = "InterManagerTag"

    // Đợi hiển thị dialog 1s thì hiển thị quảng cáo
    private const val AD_LOADING_TIME_DELAY_SHOW_ADS = 1000L

    // hiển thị dialog loading ad 2s thì off
    private const val AD_LOADING_TIME_DELAY_FINISH = 2000L

    // Quản lý các inter
    private val interHashMap = HashMap<String, InterSingle>()

    // Dialog loading
    private var loadingAdsDialog: LoadingAdsDialog? = null

    //is inter displaying
    private var isInterShowing = false

    //open app start time
    private var timeStart: Long = System.currentTimeMillis()

    // last time display inter ad
    //for interval
    private var timeBetween: Long = 0L

    // time interval config firebase
    private var interStartInterval = 5000L

    private var interBetweenInterval = 15000L

    // intent chuyển màn sau khi show native full
    private var intentNextScreen: Intent? = null

    private var canShowNativeActivity: Boolean = false

    private var nativeFullActivity: String = ""

    fun setIntentNextScreen(intent: Intent?) {
        intentNextScreen = intent
    }

    fun setNativeFullActivity(cls: String) {
        nativeFullActivity = cls
    }

    //open app start time gian bat dau vào app
    //for interval
    fun init(timeStart: Long) {
        this.timeStart = timeStart
        timeBetween = 0L
        isInterShowing = false
    }

    fun setTimeInterval(interInterval: Long = 15000L, interStartInterval: Long = 5000L) {
        this.interBetweenInterval = interInterval
        this.interStartInterval = interStartInterval
    }

    // Thêm một inter config để thêm vào interHashMap
    //them 1 single inter vao hashmap cua inter manager
//    fun addInterSingle(keyRemoteConfig: String, interModel: InterSingle) {
//        if (!interHashMap.containsKey(keyRemoteConfig)) {
//            interHashMap[keyRemoteConfig] = interModel
//        } else {
//            Log.d(TAG, "Key $keyRemoteConfig đã tồn tại trong interHashMap")
//        }
//    }

    // get inter single
//    fun getInterSingle(keyRemoteConfig: String): InterSingle? {
//        return interHashMap[keyRemoteConfig]
//    }

    // is another inter ad displaying
    //using for ad_resume check only
    fun isShowingInter(): Boolean {
        return isInterShowing
    }

    // Tải 1 quảng cáo inter
    fun loadInter(
        //context
        context: Context,

        // Key remote
        keyRemoteConfig: String,

        // id quảng cáo
        adUnitId: String = "",

        //is show
        isShow: Boolean = true,

        //20250219
        //load_and_show or load_after_show
        adMode: String = "",

        // is preload after show ad
        isPreloadAfterShowAd: Boolean = false,

        // true: display next screen, show ad
        //false: show ad, display next screen
        isShowNextActivityBeforeAd: Boolean = true,

        //callback
        interCallback: InterCallback? = null,
    ) {
        // check key in interHashMap
        // if not, add key to hashmap
        val interModel = interHashMap[keyRemoteConfig] ?: run {
            val interSingle = InterSingle()
            interSingle.adUnitId = adUnitId
            interSingle.isShow = isShow
            interSingle.adMode = adMode
            interSingle.isPreloadAfterShowAd = isPreloadAfterShowAd
            interSingle.isShowNextActivityBeforeAd = isShowNextActivityBeforeAd
            interSingle.interCallback = interCallback
            interHashMap[keyRemoteConfig] = interSingle

            //return
            interSingle
        }

        // is available to load ad
        if (checkAvailableLoadInter(context, interModel)) {

            // loading state
            interModel.loadState = LoadState.LOADING

            //for logging event firebase
            val adName: String = makeKeyShortName(keyRemoteConfig)
            val scrName: String = makeActivityShortName(context.javaClass.simpleName)

            //load ad
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // ad is loaded
                        interModel.interstitialAd = interstitialAd

                        // loading state
                        interModel.loadState = LoadState.NOT_LOADING
                        Log.d(TAG, "Quảng cáo $adUnitId tải thành công!")

                        //Revenue
                        interModel.interstitialAd?.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                Log.d(
                                    TAG,
                                    "Quảng cáo được tải ${adValue.precisionType}__ ${adValue.currencyCode}"
                                )
                                interModel.interCallback?.onEarnRevenue(adValue)
                                AdjustTracking.trackRevenueInter(
                                    interModel.interstitialAd?.responseInfo?.loadedAdapterResponseInfo,
                                    adValue
                                )
                                AnalyticTracking.trackingRevenue(context, adName, adValue)
                                //event firebase log
                                FirebaseLogEventUtils.logEventAdCommon(
                                    context,
                                    adName,
                                    scrName,
                                    "onPaid"
                                )
                            }
                        interModel.interCallback?.onAdLoaded(interstitialAd)

                        //log firebase
                        FirebaseLogEventUtils.logEventAdCommon(
                            context,
                            adName,
                            scrName,
                            "onLoaded"
                        )

                        //show inter ad if load and show
                        if (interModel.adMode.lowercase() == "load_and_show") {
                            showInter(context as Activity, keyRemoteConfig, interCallback)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Quảng cáo tải thất bại
                        interModel.interstitialAd = null

                        // loading status
                        interModel.loadState = LoadState.NOT_LOADING
                        Log.d(TAG, "Quảng cáo $adUnitId tải thất bại!")
                        interModel.interCallback?.onAdFailedToLoad(loadAdError)

                        //event
                        FirebaseLogEventUtils.logEventAdCommon(
                            context,
                            adName,
                            scrName,
                            "onLdFail" + loadAdError.code.toString()
                        )
                    }
                })

        } else {
            Log.d(TAG, "$adUnitId is not match load condition.")
            if (adMode.lowercase() == "load_and_show") {
                interCallback?.onNextAction()
                startNextActivity(context, keyRemoteConfig)
            }
        }
    }

    // Hiển thị 1 quảng cáo inter
    fun showInter(
        activity: Activity, // Activity của inter cần show
        keyRemoteConfig: String, // Key remote
        interCallback: InterCallback? = null,// call back
    ) {

        // search InterModel by keyRemoteConfig
        val interSingle = interHashMap[keyRemoteConfig] ?: run {
            Log.d(TAG, "Không tìm thấy InterModel cho keyRemoteConfig: $keyRemoteConfig")
            interCallback?.onNextAction()
            startNextActivity(activity, keyRemoteConfig)
            return
        }

        // check inter ad is loaded and available to display
        if (checkAvailableShowInter(activity, interSingle)) {

            // set isInterShowing = true ->  inter ad are displaying
            isInterShowing = true

            //for logging event firebase
            val adName: String = makeKeyShortName(keyRemoteConfig)
            val scrName: String = makeActivityShortName(activity.javaClass.simpleName)
            // fullScreenContentCallback for ad
            interSingle.interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdClicked() {
                        interCallback?.onAdClicked()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã được nhấn.")
                        FirebaseLogEventUtils.logEventAdCommon(activity, adName, scrName, "onClk")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        //set isShowInter = false -> not display inter ad
                        isInterShowing = false
                        interCallback?.onAdDismissedFullScreenContent()
                        // setting time interval
                        timeBetween = System.currentTimeMillis()

                        processPreloadAndNextActivity(
                            activity,
                            interSingle,
                            keyRemoteConfig,
                            interCallback
                        )
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã bị đóng.")
                        FirebaseLogEventUtils.logEventAdCommon(activity, adName, scrName, "onDism")

                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        //set isShowInter = false -> not display inter ad
                        isInterShowing = false
                        interCallback?.onAdFailedToShowFullScreenContent(adError)
                        // re setting interval time
                        timeBetween = System.currentTimeMillis()

                        processPreloadAndNextActivity(
                            activity,
                            interSingle,
                            keyRemoteConfig,
                            interCallback
                        )
                        Log.d(
                            TAG,
                            "Quảng cáo $keyRemoteConfig không thể hiển thị: ${adError.message}"
                        )

                        //event logging
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity,
                            adName,
                            scrName,
                            "onShFail" + adError.code.toString()
                        )

                    }

                    override fun onAdImpression() {
                        interCallback?.onAdImpression()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã ghi nhận impression.")
                        val info = interSingle.interstitialAd?.responseInfo?.loadedAdapterResponseInfo
                        AnalyticTracking.trackFanImpression(activity, adName, info?.adSourceId)
                        FirebaseLogEventUtils.logEventAdCommon(activity, adName, scrName, "onIm")

                    }

                    override fun onAdShowedFullScreenContent() {
                        interCallback?.onAdShowedFullScreenContent()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đang hiển thị.")
                        FirebaseLogEventUtils.logEventAdCommon(
                            activity,
                            adName,
                            scrName,
                            "onShFull"
                        )
                        canShowNativeActivity = true
                    }
                }
            // Hiển thị dialog loading
            showDialogLoadingAds(activity)

            Handler(Looper.getMainLooper()).postDelayed({

                //load next screen before
                if (interSingle.isShowNextActivityBeforeAd) {
                    interCallback?.onNextAction()
                }

                //roi hien thị quảng cáo
                interSingle.interstitialAd?.show(activity)

            }, AD_LOADING_TIME_DELAY_SHOW_ADS)
            cancelDialogLoadingAds(activity)
        } else {
            Log.d(TAG, "Quảng cáo $keyRemoteConfig chưa được tải hoặc không sẵn sàng để hiển thị.")
            interCallback?.onNextAction()
            startNextActivity(activity, keyRemoteConfig)
        }
    }

    // action after show ad
    //preload and move next screen
    private fun processPreloadAndNextActivity(
        activity: Activity,
        interSingle: InterSingle,
        keyRemoteConfig: String,
        interCallback: InterCallback? = null,
    ) {

        // Đặt quảng cáo thành null sau khi bị đóng
        interSingle.interstitialAd = null
        interSingle.loadState = LoadState.NOT_LOADING

        //preload
        if (interSingle.isPreloadAfterShowAd) {
            loadInter(
                activity,
                keyRemoteConfig,
                interSingle.adUnitId,
                interSingle.isShow,
                interSingle.adMode,
                interSingle.isPreloadAfterShowAd,
                interSingle.isShowNextActivityBeforeAd,
                interSingle.interCallback,
            )
        }
        //next action
        if (!interSingle.isShowNextActivityBeforeAd) {
            interCallback?.onNextAction()
        }

        startNextActivity(activity, keyRemoteConfig)
    }

    private fun startNextActivity(context: Context, keyRemoteConfig: String) {
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

    // Hiển thị dialog loading
    private fun showDialogLoadingAds(activity: Activity) {
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

    private fun checkAvailableShowInter(
        context: Context,
        interSingle: InterSingle,
    ): Boolean {

        // check remote
//        return !(interSingle.interstitialAd == null
//                //network not connected
//                //|| CheckInternet.haveNetworkConnection(context).not()
//                //UPM not allow
//                //|| GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()
//                // time interval
//                //|| interStartInterval > (System.currentTimeMillis() - timeStart)
//                // time interval
//                || interBetweenInterval > (System.currentTimeMillis() - timeBetween))
        return (interSingle.interstitialAd != null)
    }

    private fun checkAvailableLoadInter(context: Context, interSingle: InterSingle): Boolean {
        return !(interSingle.loadState == LoadState.LOADING
                || interSingle.interstitialAd != null
                || !interSingle.isShow
                //|| CheckInternet.haveNetworkConnection(context).not()
                //|| GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()
                )
    }
}