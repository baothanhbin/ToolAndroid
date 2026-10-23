package com.b9ads.admob.nativeAds.advance.base

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

interface NativeCallback {

    //ad listener callback
    fun onAdLoaded() {}

    fun onAdFailedToLoad(p0: LoadAdError) {}

    fun onAdClicked() {}

    fun onAdClosed() {}

    fun onAdImpression() {}

    fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}

    //OnNativeAdLoadedListener
    fun onNativeAdLoaded(nativeAd: NativeAd) {}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}

    //custom event
    fun onShowNative(view: NativeAdView) {}

    //ANH ad 20241125
    //custom UI when FAN
    fun onChangeCtrWhenFan(view: NativeAdView) {}

    //ANH ad 20240306
    fun onNotLoad() {}
    fun onNotShow() {}
}
