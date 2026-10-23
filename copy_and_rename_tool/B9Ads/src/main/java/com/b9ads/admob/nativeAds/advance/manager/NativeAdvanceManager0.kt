/**
 * load and show ads by common
 * Reload in case: resume, media play end, trigger by user
 * preload in other ads
 */

package com.b9ads.admob.nativeAds.advance.manager

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.b9ads.R
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
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

open class NativeAdvanceManager0(

    //ad id
    protected val adId: String,

    //ad name
    protected val adName: String,

    protected val isReloadWhenResume: Boolean = false,

    protected val isReloadWhenVideoEnd: Boolean = false,

    protected val reloadTimeWhenNotVideo: Long = 0,

    protected val maxPreload: Int = 0,

    // lifecycle
    lifecycle: Lifecycle? = null,

    ) {
    //log tag
    private val tag = "NativeAdvanceAdManager0"
    private val logEventAdName = makeKeyShortName(this.adName)
    private val logEventClassName = "Mng0"


    // layout của Native, co the lay trong lib hoạc trong app
    protected var nativeAdView: NativeAdView? = null

    // layout loading
    private var shimmerLayout: View? = null

    //native container in app
    protected var frameLayout: FrameLayout? = null

    // quảng cáo native hiện đang show
    protected var nativeAd: NativeAd? = null

    // quảng cáo native reload
    private var nativeAdPreload: NativeAd? = null

    // ad load status
    private var loadAdState = LoadState.NOT_LOADING

    //dang co 2 chuc nang la phan biet ad goi on_resume lan1,2 va show ad luon khi vao app
    //pending neu load truoc resume thi co the can them 1 parameter status nua de luu status
    private var isShowAdImmediately = false
    private var idNativeShimmer: Int? = null

    protected var callback: NativeCallback? = null

    //ad loaded time
    //for management preload ad
    private var loadedNTimes = 0
    protected var adAlreadyShow = false


    init {
        lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {

                    Lifecycle.Event.ON_RESUME -> {

                        if ((!isShowAdImmediately) && (isReloadWhenResume)) {
                            //confirm neu co ad resume thi 2 lan goi vao day
                            Log.v(tag, "Lifecycle.Event.ON_RESUME reloadNow")
                            refreshAdNative()
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d(tag, "ON_PAUSE")
                        //cancel handle show ad

                    }

                    Lifecycle.Event.ON_STOP -> {
                        Log.d(tag, "ON_STOP")
                        //cancel handle show ad

                    }


                    Lifecycle.Event.ON_DESTROY -> {
                        nativeAd?.destroy()
                        nativeAdPreload?.destroy()
                        source.lifecycle.removeObserver(this)
                    }

                    else -> {}
                }
            }
        })
    }


    protected open fun getAdLayout(nativeAd: NativeAd) = nativeAdView

    protected open fun setNativeAdView(idNativeLayout: Int) {
        val context = this.frameLayout?.context
        val nativeView = LayoutInflater.from(context).inflate(idNativeLayout, null)

        if (nativeView is NativeAdView) {
            nativeAdView = nativeView
        }
    }

    protected open fun adjustLayoutByFan() {
        return
    }

    /**
     * call to load and show native by using layout in app
     */
    open fun loadAndShowNative(
        screenFrameLayout: FrameLayout,
        idNativeLayout: Int,
        idNativeShimmer: Int,
        drawableBackground: GradientDrawable? = null,
        callback: NativeCallback? = null,
    ) {
        this.idNativeShimmer = idNativeShimmer
        val logEventFunctionName = "${logEventClassName}_LdNsh"
        val context = screenFrameLayout.context

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        this.callback = callback
        this.frameLayout = screenFrameLayout

        setNativeAdView(idNativeLayout)


        //adjust layout
        if (drawableBackground != null)
            nativeAdView?.findViewById<ConstraintLayout>(R.id.ad_unit_content)?.background =
                drawableBackground

        //adjust layout by Fan
        adjustLayoutByFan()

        //first time show ad after load ad
        isShowAdImmediately = true

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "ClLdNShCo"
        )

        //load and show
        loadAndShowNativeCore(context)
    }

    // 12/11/2025 Cuong fix #146
    protected open fun addShimmer(shimmer: View?) {
        frameLayout?.removeAllViews()
        frameLayout?.addView(shimmer)
    }

    private fun loadShimmer(context: Context) {
        idNativeShimmer?.let {
            shimmerLayout = LayoutInflater.from(context).inflate(it, null)
            shimmerLayout?.isVisible = true
            addShimmer(shimmerLayout)
        }
    }


    /**
     * Tải native về, neu OK goi hien thi
     */
    private fun loadAndShowNativeCore(context: Context) {
        val logEventFunctionName = "${logEventClassName}_LdNshCo"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        Log.v(tag, "Function loadAndShowNative")

        if (loadAdState == LoadState.LOADING) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGLoading"
            )
            return
        }
        adAlreadyShow = false
        loadShimmer(context)

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


        //condition load native OK
        Log.v(tag, "Function loadAndShowNative-load condition OK, load ad...")

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "Request"
        )
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "CkLdOk"
        )

        loadAdState = LoadState.LOADING
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                //log event
                FirebaseLogEventUtils.logEventAdCommon(
                    context, logEventAdName,
                    logEventFunctionName, "LdComp"
                )
                FirebaseLogEventUtils.logEventAdCommon(
                    context, logEventAdName,
                    logEventFunctionName, "ClOnLdCompF"
                )
                //check activity is running?
                //20250214 Dao fix
                (context as? Activity)?.let { activity ->
                    if (activity.isDestroyed || activity.isFinishing) {
                        FirebaseLogEventUtils.logEventAdCommon(
                            context, logEventAdName,
                            logEventFunctionName, "ActDestroyed"
                        )
                        ad.destroy()
                        return@forNativeAd
                    }
                }
                //reset load state
                loadAdState = LoadState.NOT_LOADING
                //call action on load complete
                onLoadComplete(context, ad)
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    loadAdState = LoadState.NOT_LOADING

                    //log event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "Loaded"
                    )

                    //Revenue
                    nativeAd?.setOnPaidEventListener { adValue ->
                        callback?.onEarnRevenue(adValue)
                        AdjustTracking.trackRevenueNative(
                            nativeAd?.responseInfo?.loadedAdapterResponseInfo,
                            adValue
                        )
                        AnalyticTracking.trackingRevenue(context, adName, adValue)
                        //event firebase log
                        FirebaseLogEventUtils.logEventAdCommon(
                            context,
                            logEventAdName,
                            logEventFunctionName,
                            "onPaid"
                        )
                    }

                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.v(tag, "Native ad failed to load: $error")
                    loadAdState = LoadState.NOT_LOADING
                    //log event
                    FirebaseLogEventUtils.logEventAdCommon(
                        context, logEventAdName,
                        logEventFunctionName, "LdFail${error.code}"
                    )
                    frameLayout?.removeAllViews()
                    callback?.onAdFailedToLoad(error)

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
            logEventFunctionName, "AdRequest"
        )
        Log.d(tag, "Sending ad request: $adLoader")

        adLoader.loadAd(AdRequest.Builder().build())
    }


    /**
     * for reload ad immediately
     */
    fun refreshAdNative() {

        //continue process
        val logEventFunctionName = "${logEventClassName}_RfAd"
        val context = this.nativeAdView?.context

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        //fix bug crash when using for native fullscreen
        // (call native when clicked button, not init screen)
        if (frameLayout == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGFrLayNull"
            )
            return
        }

        //if quota in preload ad, bellow code
        Log.v(tag, "Function reloadNow")
        Log.v(tag, "check already loaded ad: ${nativeAdPreload != null}")


        if (nativeAdPreload != null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "HasPreLd"
            )
            showAndPreloadAd()
        } else {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NoPreLd"
            )
            isShowAdImmediately = true
            frameLayout?.context?.let {
                loadAndShowNativeCore(it)
            }
        }

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )
    }

    /**
     * callback khi load ad success
     * assign loaded ads to preload ads
     * if 1st isShowAdImmediately =true then show ad
     * if not do nothing (wait to call showAndPreloadAd)
     */
    protected open fun onLoadComplete(context: Context, loadedNativeAd: NativeAd) {

        //for log
        val logEventFunctionName = "${logEventClassName}_OnLdComp"
        Log.v(tag, "Function onLoadComplete")
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        //increase loaded time for max preload
        loadedNTimes++
        Log.v(tag, "increased loadNtime, loadedNTime = $loadedNTimes")
        nativeAdPreload = loadedNativeAd

        if (isShowAdImmediately) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "ShImm"
            )
            isShowAdImmediately = false
            showAndPreloadAd()
        } else {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NotShImm"
            )
        }


        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )


        callback?.onNativeAdLoaded(loadedNativeAd)

    }

    /**
     * show ad and preload other native
     */
    open fun showAndPreloadAd() {

        //log event
        val logEventFunctionName = "${logEventClassName}_ShNPreLd"
        val context = frameLayout?.context
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        Log.v(tag, "Function showAndPreloadAd")

        if (nativeAdView == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNtVNu"
            )
            callback?.onNotShow()
            return
        }

        if (nativeAdPreload == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNtPreLdNu"
            )
            callback?.onNotShow()
            return
        }

        //show ad
        //destroy current native and assign preloaded native
        nativeAd?.destroy()
        nativeAd = nativeAdPreload
        nativeAdPreload = null

        //processing UI
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "ClPsNtAv"
        )

        val layout = nativeAd?.let { getAdLayout(it) }

        layout?.let {
            nativeAd?.let { native -> pushNativeAdView(native, it) }
            frameLayout?.removeAllViews()
            frameLayout?.addView(it)
            it.isVisible = true

            //callback
            callback?.onShowNative(it)
            adAlreadyShow = true
        }

        //continue preload ad if have preload quota
        if (loadedNTimes < maxPreload) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "ClPreLd"
            )
            nativeAdView?.context?.let {
                loadAndShowNativeCore(it)
            }
        }

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )

    }

    /**
     * push native ad data to adview to show
     */
    private fun pushNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        Log.v(tag, "pushNativeAdView")
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
        } else {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${logEventAdName}_nvd")
            Log.v(tag, "Function onCheckNativeNotMediaVideo")
            onCheckNativeNotMediaVideo()
        }

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )
    }


    /**
     * callback when native has no video
     */
    protected open fun onCheckNativeNotMediaVideo() {
        return
    }

    /**
     * callback khi end video media
     */
    protected open fun onNativeMediaVideoEnd() {
        if (isReloadWhenVideoEnd) {
            refreshAdNative()
        }
    }

}