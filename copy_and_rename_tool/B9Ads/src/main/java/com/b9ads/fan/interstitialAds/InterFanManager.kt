package com.b9ads.fan.interstitialAds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.b9ads.dialog.LoadingAdsDialog
import com.b9ads.enums.LoadState
import com.b9ads.fan.interstitialAds.base.InterFanCallback
import com.b9ads.fan.interstitialAds.base.InterFanConfig
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

object InterFanManager {
    private const val TAG = "InterFanManagerTag"

    // Đợi hiển thị dialog 1s thì hiển thị quảng cáo
    private const val AD_LOADING_TIME_DELAY_SHOW_ADS = 1000L

    // hiển thị dialog loading ad 2s thì off
    private const val AD_LOADING_TIME_DELAY_FINISH = 2000L

    // Quản lý các inter
    private val interHashMap = HashMap<String, InterFanConfig>()

    // Dialog loading
    private var loadingAdsDialog: LoadingAdsDialog? = null

    //is inter displaying
    private var isInterShowing = false

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

        //load_and_show or load_after_show
        adMode: String = "",

        //callback
        interCallback: InterFanCallback? = null
    ) {
        // check key in interHashMap
        // if not, add key to hashmap
        val interModel = interHashMap[keyRemoteConfig] ?: run {
            val interSingle = InterFanConfig()
            interSingle.adMode = adMode
            interSingle.isShow = isShow
            interSingle.adUnitId = adUnitId
            interSingle.interFanCallback = interCallback
            interHashMap[keyRemoteConfig] = interSingle

            //return
            interSingle
        }

        // is available to load ad
        if (checkAvailableLoadInter(interModel)) {

            // loading state
            interModel.loadState = LoadState.LOADING

            //for logging event firebase
            val adName: String = makeKeyShortName(keyRemoteConfig)
            val scrName: String = makeActivityShortName(context.javaClass.simpleName)

            //load ad
            val interAd = InterstitialAd(context, adUnitId)
            val adListener = object : InterstitialAdListener {
                override fun onError(ad: Ad?, error: AdError?) {
                    Log.v(TAG, "$keyRemoteConfig error ${error?.errorCode}: ${error?.errorMessage}")
                    interModel.interFanAd = null
                    interModel.loadState = LoadState.NOT_LOADING
                    interModel.interFanCallback?.onError(ad, error)
                    //event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context,
                        adName,
                        scrName,
                        "onLdFail" + error?.errorCode.toString()
                    )
                }

                override fun onAdLoaded(ad: Ad?) {
                    Log.v(TAG, "$keyRemoteConfig ad loaded")
                    // ad is loaded
                    interModel.interFanAd = interAd

                    // loading state
                    interModel.loadState = LoadState.NOT_LOADING
                    interModel.interFanCallback?.onAdLoaded(ad)

                    //log firebase
                    FirebaseLogEventUtils.logEventAdCommon(
                        context,
                        adName,
                        scrName,
                        "onLoaded"
                    )

                    //show inter ad if load and show
                    if (interModel.adMode?.lowercase() == "load_and_show") {
                        showInter(context as? Activity, keyRemoteConfig, interCallback)
                    }
                }

                override fun onAdClicked(ad: Ad?) {
                    Log.v(TAG, "$keyRemoteConfig Ad Clicked")
                    interModel.interFanCallback?.onAdClicked(ad)
                    FirebaseLogEventUtils.logEventAdCommon(context, adName, scrName, "onClk")
                }

                override fun onLoggingImpression(ad: Ad?) {
                    Log.v(TAG, "$keyRemoteConfig Logging Impression")
                    interModel.interFanCallback?.onLoggingImpression(ad)
                    FirebaseLogEventUtils.logEventAdCommon(context, adName, scrName, "onIm")
                }

                override fun onInterstitialDisplayed(ad: Ad?) {
                    Log.v(TAG, "$keyRemoteConfig Interstitial Displayed")
                    interModel.interFanCallback?.onInterstitialDisplayed(ad)
                    FirebaseLogEventUtils.logEventAdCommon(context, adName, scrName,"onShFull")
                    canShowNativeActivity = true
                }

                override fun onInterstitialDismissed(ad: Ad?) {
                    Log.v(TAG, "$keyRemoteConfig Interstitial Dismissed")
                    interModel.interFanCallback?.onInterstitialDismissed(ad)

                    isInterShowing = false
                    interCallback?.onInterstitialDismissed(ad)
                    processEndInter(context, keyRemoteConfig)
                    Log.d(TAG, "Quảng cáo $keyRemoteConfig đã bị đóng.")
                    FirebaseLogEventUtils.logEventAdCommon(context, adName, scrName, "onDism")
                }

            }
            val loadConfig = interAd.buildLoadAdConfig().withAdListener(adListener).build()
            interAd.loadAd(loadConfig)
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
        activity: Activity?, // Activity của inter cần show
        keyRemoteConfig: String, // Key remote
        interCallback: InterFanCallback? = null// call back
    ) {

        // search InterModel by keyRemoteConfig
        val interSingle = interHashMap[keyRemoteConfig] ?: run {
            Log.d(TAG, "Không tìm thấy InterModel cho keyRemoteConfig: $keyRemoteConfig")
            intentNextScreen?.let { activity?.startActivity(it) }
            interCallback?.onNextAction()
            startNextActivity(activity, keyRemoteConfig)
            return
        }
        interSingle.customShowCallback = interCallback
        // check inter ad is loaded and available to display
        if (checkAvailableShowInter(interSingle) && activity != null) {
            // set isInterShowing = true ->  inter ad are displaying
            isInterShowing = true

            // Hiển thị dialog loading
            showDialogLoadingAds(activity)

            Handler(Looper.getMainLooper()).postDelayed({
                //roi hien thị quảng cáo
                if (interSingle.interFanAd?.isAdInvalidated != true) {
                    interSingle.interFanAd?.show()
                } else {
                    processEndInter(activity, keyRemoteConfig)
                }

            }, AD_LOADING_TIME_DELAY_SHOW_ADS)
            cancelDialogLoadingAds(activity)
        } else {
            Log.d(TAG, "Quảng cáo $keyRemoteConfig chưa được tải hoặc không sẵn sàng để hiển thị.")
            processEndInter(activity, keyRemoteConfig)
        }
    }

    // action after show ad
    //preload and move next screen
    private fun processEndInter(
        context: Context?,
        keyRemoteConfig: String
    ) {
        val interSingle = interHashMap[keyRemoteConfig] ?: return
        // Đặt quảng cáo thành null sau khi bị đóng
        interSingle.interFanAd = null
        interSingle.loadState = LoadState.NOT_LOADING
        interSingle.customShowCallback?.onNextAction() ?: interSingle.interFanCallback?.onNextAction()
        startNextActivity(context, keyRemoteConfig)
    }

    private fun startNextActivity(context: Context?, keyRemoteConfig: String) {
        if (canShowNativeActivity) {
            val success = startNativeActivity(context, intentNextScreen, keyRemoteConfig)
            if (success.not()) {
                intentNextScreen?.let { context?.startActivity(it) }
            }
        } else {
            intentNextScreen?.let { context?.startActivity(it) }
        }
        intentNextScreen = null
        canShowNativeActivity = false
    }

    private fun startNativeActivity(context: Context?, data: Intent?, keyRemoteConfig: String): Boolean {
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
                    val dialogIsAttach = loadingAdsDialog != null
                            && loadingAdsDialog?.window != null
                            && loadingAdsDialog?.window?.decorView?.isAttachedToWindow == true

                    val isShowing = loadingAdsDialog?.isShowing == true
                    if (dialogIsAttach && !activity.isDestroyed && isShowing) {
                        loadingAdsDialog?.dismiss()
                    }
                    loadingAdsDialog = null
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }, AD_LOADING_TIME_DELAY_FINISH)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAvailableShowInter(
        interSingle: InterFanConfig
    ): Boolean {
        return (interSingle.interFanAd != null)
    }

    private fun checkAvailableLoadInter(interSingle: InterFanConfig): Boolean {
        return !(interSingle.loadState == LoadState.LOADING || interSingle.isShow != true)
    }
}