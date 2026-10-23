package com.b9ads.admob.nativeAds.advance.simple

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.b9ads.R
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.base.NativeLayout
import com.b9ads.enums.LoadState
import com.b9ads.tracking.adjust.AdjustTracking
import com.b9ads.tracking.analytic.AnalyticTracking
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9utils.util.CheckInternet
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

open class SimpleNativeAdvanceManager0(

    //ad id
    private var adId: String,

    //ad name
    private var adName: String,

    ) {
    //log tag
    private val tag = "V1Lib_b9ads_SimpleNativeAdvanceAdManager0"
    private val logEventAdName = makeKeyShortName(this.adName)
    private val logEventClassName = "Mng0V2"

    //for Fan sourceID list
    //for test ad "7068401028668408324" (admob test)
    private var listMediationFanSourceID =
        mutableListOf<String>("10568273599589928883", "11198165126854996598")

    //properties
    protected var isShow: Boolean = true

    protected var layoutKey: String = "ads_native_1_button_2_media_3_info"

    protected var backgroundColor: String? = null

    protected var borderColor: String? = null

    @ColorInt
    protected var btnAdsColor: Int? = null

    //if source is FAN, is disable click in ad?
    protected var isChangeButtonClickOnlyWhenFan: Boolean = true
    protected var isChangeDisableClickAllWhenFan: Boolean = false
    protected var adLayoutKeyWhenFan: String = ""
    protected var bgColorWhenFan: String = ""
    protected var borderColorWhenFan: String = ""
    protected var buttonColorWhenFan: String = ""

    //callback
    protected var callback: NativeCallback? = null

    //other ad load status


    // loaded ad
    protected var nativeAd: NativeAd? = null

    // ad load status
    private var loadAdState = LoadState.NOT_LOADING

    // show ads ngay khi load được
    private var isShowImmediately = false

    init {
        nativeAd?.destroy()
        nativeAd = null
        loadAdState = LoadState.NOT_LOADING
    }


    open fun setProperties(
        isShow: Boolean = true,
        adLayoutKey: String = "",
        backgroundColor: String? = null,
        borderColor: String? = null,
        callback: NativeCallback? = null,
        @ColorInt btnAdsColor: Int? = null,
    ) {
        this.isShow = isShow
        this.layoutKey = adLayoutKey
        this.backgroundColor = backgroundColor
        this.borderColor = borderColor
        this.callback = callback
        this.btnAdsColor = btnAdsColor
    }

    open fun setPropertiesForFan(
        isChangeButtonClickOnlyWhenFan: Boolean = true,
        isChangeDisableClickAllWhenFan: Boolean = false,
        adLayoutKeyWhenFan: String = "",
        bgColorWhenFan: String = "",
        borderColorWhenFan: String = "",
        buttonColorWhenFan: String = "",
    ) {
        this.isChangeButtonClickOnlyWhenFan = isChangeButtonClickOnlyWhenFan
        this.isChangeDisableClickAllWhenFan = isChangeDisableClickAllWhenFan
        this.adLayoutKeyWhenFan = adLayoutKeyWhenFan
        this.bgColorWhenFan = bgColorWhenFan
        this.borderColorWhenFan = borderColorWhenFan
        this.buttonColorWhenFan = buttonColorWhenFan
    }

    /**
     * call to load and show native by using layout in app
     */
    open fun loadNative(
        context: Context,
        adFrameLayout: FrameLayout? = null,
        isShowImmediately: Boolean = false,
    ) {
        this.isShowImmediately = isShowImmediately
        Log.v(tag, "load native ad simple ver2, name: $adName")
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
            callback?.onNotLoad()
            hideShimmerLoadAd(adFrameLayout)
            return
        }

        if (!AdsHelper.checkShowAdByVersion()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NotShVer"
            )
            Log.v(tag, "load native return, not show ad by version")
            callback?.onNotLoad()
            hideShimmerLoadAd(adFrameLayout)
            return
        }

        if (loadAdState == LoadState.LOADING) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_Lding"
            )
            callback?.onNotLoad()
            hideShimmerLoadAd(adFrameLayout)
            return
        }


        if (GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NG_Ump"
            )
            //return
        }

        if (CheckInternet.haveNetworkConnection(context).not()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NG_NoNet"
            )
            //return
        }


        //condition load native OK
        Log.v(tag, "...Function loadAndShowNative-load condition OK, load ad...")

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "Rq_Setting"
        )

        //preparing for request

        loadAdState = LoadState.LOADING

        addShimmer(context, adFrameLayout)

        Log.v(tag, "...setting for request")
        //setting
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                Log.v(tag, "callback forNativeAd")
                //log event
                FirebaseLogEventUtils.logEventAdCommon(
                    context, logEventAdName,
                    logEventFunctionName, "LdComp"
                )

                //reset load state
                loadAdState = LoadState.NOT_LOADING
                this.nativeAd = ad

                //call action on load complete
                if (this.isShowImmediately) {
                    showNative(context, adFrameLayout)
                }

                //log event when Fan
                if (isMediationFanSource()) {
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "Fan"
                    )
                }

            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    Log.v(tag, "callback onAdLoaded")
                    super.onAdLoaded()
                    loadAdState = LoadState.NOT_LOADING

                    //log event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "AdLoaded"
                    )

                    //Revenue
                    nativeAd?.setOnPaidEventListener { adValue ->
                        callback?.onEarnRevenue(adValue)
                        AdjustTracking.trackRevenueNative(
                            nativeAd?.responseInfo?.loadedAdapterResponseInfo,
                            adValue
                        )
                        //event firebase log
                        AnalyticTracking.trackingRevenue(context, adName, adValue)
                        FirebaseLogEventUtils.logEventAdCommon(
                            context, logEventAdName,
                            logEventFunctionName, "onPaid"
                        )

                        if (adValue.valueMicros <= 0) {
                            FirebaseLogEventUtils.logEventAdCommon(
                                context, logEventAdName,
                                logEventFunctionName, "onPaid_0"
                            )
                        }
                    }
                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.v(tag, "callback onAdFailedToLoad $error")
                    super.onAdFailedToLoad(error)
                    loadAdState = LoadState.NOT_LOADING
                    nativeAd = null
                    //log event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "LdFail${error.code}"
                    )
                    callback?.onAdFailedToLoad(error)
                    hideShimmerLoadAd(adFrameLayout)
                    Log.v(tag, "...onAdFailedToLoad: ${error.message}")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    callback?.onAdClicked()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    callback?.onAdClosed()
                }

                override fun onAdImpression() {
                    Log.v(tag, "callback onAdImpression")
                    super.onAdImpression()
                    loadAdState = LoadState.NOT_LOADING
                    AnalyticTracking.trackFanImpression(
                        context,
                        adName,
                        nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId
                    )
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "OnIm"
                    )
                    callback?.onAdImpression()

                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    callback?.onAdOpened()
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                    callback?.onAdSwipeGestureClicked()
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "OnAdSwipe"
                    )
                }
                //20250214 Dao fix
            }).withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(
                        VideoOptions.Builder()
                            .setStartMuted(false)
                            .setCustomControlsRequested(true)
                            .build()
                    )
                    .build()
            )
            .build()
        //20250214 Dao fix
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "Request"
        )
        Log.d(tag, "...Sending ad request: $adLoader")

        adLoader.loadAd(AdRequest.Builder().build())
        Log.v(tag, "...sent ad request")

    }


    /**
     * show ad and preload other native
     */
    open fun showNative(
        context: Context,
        adFrameLayout: FrameLayout?,
    ) {

        //log event
        val logEventFunctionName = "${logEventClassName}_Sh"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )
        Log.v(tag, "Function showNative")

        if (!isShow) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NotSh"
            )
            callback?.onNotShow()
            hideShimmerLoadAd(adFrameLayout)
            return
        }
        Log.v(tag, "...check is show OK")

        //not show by version
        if (!AdsHelper.checkShowAdByVersion()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NotShVer"
            )
            Log.v(
                tag,
                "....show ad return, not show ad by version"
            )
            callback?.onNotShow()
            hideShimmerLoadAd(adFrameLayout)
            return
        }
        Log.v(tag, "...check notShowAdVersion OK")

        //check loaded ad
        if (this.nativeAd == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_AdNull"
            )
            callback?.onNotShow()
            hideShimmerLoadAd(adFrameLayout)
            return
        }

        //check layout
        val enumNativeLayout = NativeLayout.entries.toTypedArray().find { it.key == layoutKey }
        val idNativeLayout = enumNativeLayout?.layoutNativeId

        val nativeView = idNativeLayout?.let { LayoutInflater.from(context).inflate(it, null) }
        val nativeAdView = nativeView as NativeAdView?

        if (nativeAdView !is NativeAdView) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_NtvErr"
            )
            callback?.onNotShow()
            hideShimmerLoadAd(adFrameLayout)
            return
        }


        //adjust layout
        Log.v(tag, "...custom layout for special setting")
        customLayout(nativeAdView)

        //adjust custom layout when Fan
        Log.v(tag, "...custom layout for Fan")
        val isFan = isMediationFanSource()
        if (isFan)
            customLayoutWhenFan(nativeAdView)

        //show ad
        Log.v(tag, "...show ad")
        nativeAdView.let {
            nativeAd?.let { native -> pushNativeAdView(native, it) }
            adFrameLayout?.removeAllViews()
            adFrameLayout?.addView(it)
            it.isVisible = true

            //callback
            callback?.onShowNative(it)
        }

        //change ctr when Fan
        Log.v(tag, "...custom click for Fan")
        setClickAreaWhenFan(nativeAdView)

        if (isFan) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "FFF"
            )
        }

        //log firebase
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )

    }

    // chờ quảng cáo load xong rồi mới show
    fun waitAndShowNative(
        context: Context,
        adFrameLayout: FrameLayout?,
        needAddShimmer: Boolean = false,
        callback: NativeCallback? = null,
    ) {
        this.callback = callback ?: this.callback
        if (loadAdState == LoadState.LOADING) {
            if (needAddShimmer) {
                addShimmer(context, adFrameLayout)
            }
        } else if (this.nativeAd == null) {
            this.callback?.onNotShow()
        } else {
            showNative(context, adFrameLayout)
        }
    }

    private fun addShimmer(context: Context, adFrameLayout: FrameLayout?) {
        val logEventFunctionName = "${logEventClassName}_Ld"
        //set shimmer layout loading
        val enumNativeLayout = getLayoutSet()
        if (enumNativeLayout == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGR_LayoutNull"
            )
            callback?.onNotLoad()
            hideShimmerLoadAd(adFrameLayout)
            return
        }

        val idNativeShimmerLayout = enumNativeLayout.shimmerLayoutId

        val shimmerNativeAdView =
            idNativeShimmerLayout.let { LayoutInflater.from(context).inflate(it, null) }
        shimmerNativeAdView?.isVisible = true
        adFrameLayout?.addView(shimmerNativeAdView)
    }

    private fun isMediationFanSource(): Boolean {
        val strAdSourceID =
            this.nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId ?: "0"

        return strAdSourceID in listMediationFanSourceID
//        test Fan
//        return true
    }

    private fun setClickAreaWhenFan(adView: NativeAdView) {
        val elements = listOf(
            adView.mediaView,
            adView.headlineView,
            adView.bodyView,
            adView.iconView,
            adView.priceView,
            adView.starRatingView,
            adView.storeView,
            adView.advertiserView
        )
        val isFan = isMediationFanSource()

        if (isFan && isChangeButtonClickOnlyWhenFan) {
            elements.forEach {
                it?.isClickable = false
            }
            adView.callToActionView?.isClickable = true
            Log.v(tag, "button only can click")
        }

        if (isFan && isChangeDisableClickAllWhenFan) {
            elements.forEach {
                it?.isClickable = false
            }
            adView.callToActionView?.isClickable = false
            Log.v(tag, "all ad can not click")
        }

    }

    private fun getLayoutSet(): NativeLayout? {
        val enumNativeLayout = NativeLayout.entries.toTypedArray().find { it.key == layoutKey }
        val enumNativeLayoutFan =
            NativeLayout.entries.toTypedArray().find { it.key == adLayoutKeyWhenFan }

        if (isMediationFanSource() && this.adLayoutKeyWhenFan.isNotEmpty() && (enumNativeLayoutFan != null))
            return enumNativeLayoutFan
        else
            return enumNativeLayout


    }

    /**
     * push native ad data to adview to show
     */
    private fun pushNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        Log.v(tag, "function pushNativeAdView")
        val context = adView.context
        val logEventFunctionName = "${logEventClassName}_PsNtAv"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as? MediaView

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as? TextView)?.text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as? TextView)?.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as? Button)?.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as? ImageView)?.setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as? TextView)?.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as? TextView)?.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating?.toFloat() ?: 0f
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as? TextView)?.text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)


        val vc = nativeAd.mediaContent?.videoController

        if (nativeAd.mediaContent != null && nativeAd.mediaContent?.hasVideoContent() == true) {
            Log.v(tag, "...ad has video")
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logEventAdName}_yvd")

            vc?.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {

                override fun onVideoEnd() {
                    super.onVideoEnd()
                    //Log.v(TAG, "onVideoEnd")
                    onNativeMediaVideoEnd()
                }

                override fun onVideoStart() {
                    super.onVideoStart()
                    //Log.v(TAG, "onVideoStart")
                }

                override fun onVideoPlay() {
                    super.onVideoPlay()
                    //Log.v(TAG, "onVideoPlay")
                }

                override fun onVideoPause() {
                    super.onVideoPause()
                    //Log.v(TAG, "onVideoPause")
                }

                override fun onVideoMute(p0: Boolean) {
                    super.onVideoMute(p0)
                    //Log.v(TAG, "onVideoMute")
                }

            }
            if (vc?.isCustomControlsEnabled == true) {
                vc.play()
            }
            Log.v(tag, "...call onCheckNativeHasMediaVideo")
            onCheckNativeHasMediaVideo()
        } else {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logEventAdName}_nvd")
            Log.v(tag, "...ad no video")
            Log.v(tag, "...call onCheckNativeNotMediaVideo")
            onCheckNativeNotMediaVideo()
        }

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )
    }

    private fun customLayout(nativeAdView: NativeAdView?) {
        val drawable: GradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            if (!this@SimpleNativeAdvanceManager0.backgroundColor.isNullOrEmpty()) {
                setColor(Color.parseColor(this@SimpleNativeAdvanceManager0.backgroundColor))
            }
            if (!this@SimpleNativeAdvanceManager0.borderColor.isNullOrEmpty()) {
                setStroke(1, Color.parseColor(this@SimpleNativeAdvanceManager0.borderColor))
            }
        }
        if ((this@SimpleNativeAdvanceManager0.backgroundColor != null) ||
            (this@SimpleNativeAdvanceManager0.borderColor != null)
        ) {
            nativeAdView?.findViewById<ConstraintLayout>(R.id.ad_unit_content)?.background =
                drawable
        }
        btnAdsColor?.let {
            nativeAdView?.findViewById<View>(R.id.ad_call_to_action)?.backgroundTintList =
                ColorStateList.valueOf(it)
        }
    }

    private fun customLayoutWhenFan(nativeAdView: NativeAdView?) {

        //background color
        val drawable: GradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            if (this@SimpleNativeAdvanceManager0.bgColorWhenFan.isNotEmpty()) {
                setColor(Color.parseColor(this@SimpleNativeAdvanceManager0.bgColorWhenFan))
            }
            if (this@SimpleNativeAdvanceManager0.borderColorWhenFan.isNotEmpty()) {
                setStroke(1, Color.parseColor(this@SimpleNativeAdvanceManager0.borderColorWhenFan))
            }
        }
        if ((this@SimpleNativeAdvanceManager0.bgColorWhenFan.isNotEmpty()) ||
            (this@SimpleNativeAdvanceManager0.borderColorWhenFan.isNotEmpty())
        ) {
            nativeAdView?.findViewById<ConstraintLayout>(R.id.ad_unit_content)?.background =
                drawable
        }

        //button color
        if (this@SimpleNativeAdvanceManager0.buttonColorWhenFan.isNotEmpty()) {
            val color = Color.parseColor(this@SimpleNativeAdvanceManager0.buttonColorWhenFan)
            nativeAdView?.findViewById<Button>(R.id.ad_call_to_action)
                ?.setBackgroundColor(color)
        }
    }

    /**
     * callback when native has no video
     */
    protected open fun onCheckNativeNotMediaVideo() {
        return
    }

    protected open fun onCheckNativeHasMediaVideo() {
        return
    }

    /**
     * callback khi end video media
     */
    protected open fun onNativeMediaVideoEnd() {
        return
    }

    open fun destroy() {
        nativeAd?.destroy()
        nativeAd = null
    }

    private fun hideShimmerLoadAd(adFrameLayout: FrameLayout?) {
        adFrameLayout?.removeAllViews()
        adFrameLayout?.visibility = View.GONE
    }
}