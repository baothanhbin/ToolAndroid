/**
 * CUONG
 * reload resume thi chua xu ly
 * reload by timer GG khong co, chua xu ly
 */
package com.b9ads.admob.bannerAds.collapsible

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.b9ads.admob.bannerAds.base.BannerManager
import com.b9ads.admob.bannerAds.base.BannerCallback
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9utils.util.CheckInternet

class CollapseBannerManager(
    id: String,
    lifecycle: Lifecycle? = null,

    //native khong de callback trong constructor nhung khong sao
    callback: BannerCallback? = null
) : BannerManager(id, lifecycle, callback) {

    private val collapsibleKey = "collapsible"

    // Gọi hàm để show collapse banner
    fun loadAndShowCollapBanner(
        container: FrameLayout,
        @ColorInt lineColor: Int = Color.GRAY,
        gravity: BannerGravity = BannerGravity.BOTTOM,
    ) {
        loadingLayout(container, lineColor)
        showLoading()
        loadBanner(container.context, gravity)
    }

    // load collapse banner
    private fun loadBanner(context: Context, gravity: BannerGravity) {
        if (CheckInternet.haveNetworkConnection(context).not() ||
            GoogleMobileAdsConsentManager.getInstance(context).canRequestAds.not()
        ) {
            return
        }
        createAdview(context,"collapsible")
        val extras = bundleOf(collapsibleKey to gravity.value)
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        adView?.loadAd(adRequest)
    }
}