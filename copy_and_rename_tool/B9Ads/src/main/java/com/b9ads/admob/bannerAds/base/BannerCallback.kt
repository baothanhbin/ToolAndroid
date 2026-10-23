package com.b9ads.admob.bannerAds.base

import com.google.android.gms.ads.AdValue

interface BannerCallback {

    //ad listener callback
    fun onAdLoaded() {}

    fun onAdFailToLoad() {}

    fun onAdClicked() {}

    fun onAdClosed() {}

    fun onAdImpression() {}

    fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}

    //more custom event
}
