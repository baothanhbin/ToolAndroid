package com.b9ads.fan.interstitialAds.base

import com.facebook.ads.Ad
import com.facebook.ads.AdError

interface InterFanCallback {

    fun onError(ad: Ad?, error: AdError?) {}
    fun onAdLoaded(ad: Ad?) {}
    fun onAdClicked(ad: Ad?) {}
    fun onLoggingImpression(ad: Ad?) {}
    fun onInterstitialDisplayed(ad: Ad?) {}
    fun onInterstitialDismissed(ad: Ad?) {}
    fun onNextAction() {}
    fun onStartNativeActivity() {}
}