/**
 * native manager special defend on mediation adsource
 * adsource: Fan
 */

package com.b9ads.admob.nativeAds.advance.manager

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import com.b9ads.R
import com.b9ads.admob.nativeAds.advance.base.NativeLayout
import com.b9utils.util.FirebaseLogEventUtils
import com.google.android.gms.ads.nativead.NativeAdView

open class NativeAdvanceManager2(
    adId: String,
    //ad name
    adName: String,
    isReloadWhenResume: Boolean = false,
    isReloadWhenVideoEnd: Boolean = false,
    reloadTimeWhenNotVideo: Long = 0,
    maxPreload: Int = 0,
    // lifecycle dùng để reload quảng cáo
    lifecycle: Lifecycle? = null

) : NativeAdvanceManager1(
    adId,
    adName,
    isReloadWhenResume,
    isReloadWhenVideoEnd,
    reloadTimeWhenNotVideo,
    maxPreload,
    lifecycle
) {

    private val tag = "NativeAdvanceAdManager2"

    //for Fan sourceID list
    //for test ad "7068401028668408324" (admob test)
    private var listMediationFanSourceID =
        mutableListOf<String>("10568273599589928883", "11198165126854996598")

    //if source is FAN, is disable click in ad?
    private var isChangeButtonClickOnlyWhenFan = true

    //ANH add 20241125
    private var isChangeDisableClickAllWhenFan = false

    //Anh ad 20240306
    private var layoutWhenFan: String? = ""

    //ANH ad 20240312
    private var bgColorWhenFan: String? = ""
    private var borderColorWhenFan: String? = ""
    private var buttonColorWhenFan: String? = ""

    private fun isMediationFanSource(): Boolean {
        val strAdSourceID =
            this.nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId ?: "0"
        return strAdSourceID in listMediationFanSourceID
        //return true
    }

    //disable click in adNativeView
    private fun changeCtrWhenFan() {

        //ANH edit 20221125
        //ad disable click all ad when fan

//        if((!isChangeButtonClickOnlyWhenFan) && (!isChangeDisableClickAllWhenFan))
//            return
//
//        if (isMediationFanSource()){
//            this.nativeAdView?.let {
//                setClickButtonOnly(it,true)
//                var context = nativeAdView?.context
//                FirebaseLogEventUtils.logEventAdNativeChangeButtonClickOnly(context, adName, adId)
//            }
//        }
        val isFan: Boolean = isMediationFanSource()

        //for test
        //confirm comment out when release
        //isFan = false


        var changeRegion = 0 //not change
        if (isFan && isChangeButtonClickOnlyWhenFan)
            changeRegion = 1 //button only

        if (isFan && isChangeDisableClickAllWhenFan)
            changeRegion = 2 //all ad

        if (changeRegion == 1) {
            this.nativeAdView?.let { it ->
                setClickButtonOnly(it, true)
                callback?.onChangeCtrWhenFan(it)
                val context = nativeAdView?.context
                //FirebaseLogEventUtils.logEventAdNativeChangeAdCtr(context, adName,"btnCngClk")
                FirebaseLogEventUtils.logEventAdCommon(context, adName, "Mng2", "btnCngClk")
            }
        } else if (changeRegion == 2) {
            this.nativeAdView?.let {
                setClickDisableAll(it, true)
                callback?.onChangeCtrWhenFan(it)
                val context = nativeAdView?.context

                //FirebaseLogEventUtils.logEventAdNativeChangeAdCtr(context, adName, "allCngClk")
                FirebaseLogEventUtils.logEventAdCommon(context, adName, "Mng2", "allCngClk")
            }
        }

        //end edit
    }

    private fun setClickButtonOnly(adView: NativeAdView, onlyClickBtn: Boolean = true) {
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
        elements.forEach {
            it?.isClickable = onlyClickBtn.not()
        }
    }

    private fun setClickDisableAll(adView: NativeAdView, clickDisable: Boolean = true) {
        val elements = listOf(
            adView.mediaView,
            adView.headlineView,
            adView.bodyView,
            adView.iconView,
            adView.priceView,
            adView.starRatingView,
            adView.storeView,
            adView.advertiserView,
            adView.callToActionView,
        )
        elements.forEach {
            it?.isClickable = clickDisable.not()
        }
    }


    fun setChangeAdCtrWhenFan(
        isChangeAll: Boolean = false,
        isChangeButtonOnly: Boolean = true,
        layoutWhenFan: String? = "",
        bgColorWhenFan: String? = "",
        borderColorWhenFan: String? = "",
        buttonColorWhenFan: String? = ""

    ) {
        isChangeDisableClickAllWhenFan = isChangeAll
        isChangeButtonClickOnlyWhenFan = isChangeButtonOnly
        this.layoutWhenFan = layoutWhenFan
        this.bgColorWhenFan = bgColorWhenFan
        this.borderColorWhenFan = borderColorWhenFan
        this.buttonColorWhenFan = buttonColorWhenFan
    }

    /**
     * override here to change click enable status in adview
     */
    override fun showAndPreloadAd() {
        super.showAndPreloadAd()
        changeCtrWhenFan()
    }

    override fun setNativeAdView(idNativeLayout: Int) {
        if (isMediationFanSource()) {
            val enumNativeLayout =
                NativeLayout.entries.toTypedArray().find { it.key == this.layoutWhenFan }
            val newIDLayOut = enumNativeLayout?.layoutNativeId
            if (newIDLayOut == null)
                super.setNativeAdView(idNativeLayout)
            else
                super.setNativeAdView(newIDLayOut)
        } else {
            super.setNativeAdView(idNativeLayout)
        }
    }

    override fun adjustLayoutByFan() {
        //return
        val drawableBackground = getDrawableFileBG()
        Log.v(tag, "adjust layout by Fan, drawable background $drawableBackground")
        Log.v(tag, "is fan ${isMediationFanSource()}")
        Log.v(tag, "button color $buttonColorWhenFan")

        if ((drawableBackground != null) && isMediationFanSource())
            nativeAdView?.findViewById<ConstraintLayout>(R.id.ad_unit_content)?.background =
                drawableBackground

        if ((this.buttonColorWhenFan != "") && isMediationFanSource()) {
            nativeAdView?.findViewById<Button>(R.id.ad_call_to_action)
                ?.setBackgroundColor(Color.parseColor(this.buttonColorWhenFan))
        }

    }

    private fun getDrawableFileBG(): GradientDrawable? {

        try {
            val drawable: GradientDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f

                if (this@NativeAdvanceManager2.bgColorWhenFan != "")
                    setColor(Color.parseColor(this@NativeAdvanceManager2.bgColorWhenFan))
                if (this@NativeAdvanceManager2.borderColorWhenFan != "")
                    setStroke(1, Color.parseColor(borderColorWhenFan))
            }

            return drawable
        } catch (e: Exception) {
            return null
        }
    }
}