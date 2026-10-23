package com.b9ads.admob.splashAds.callback

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd

interface SplashCallback {
    //InterstitialAdLoadCallback
    // Tải thành công ads
    fun onAdLoaded(appOpenAd: AppOpenAd?, interstitialAd: InterstitialAd?, isAppOpen:Boolean){}

    // Tải quảng cáo thất bại
    fun onAdFailedToLoad(loadAdError: LoadAdError, isAppOpen:Boolean){}

    //fullScreenContentCallback
    // Quảng cáo đã được nhấp vào
    fun onAdClicked(isAppOpen:Boolean){}

    // Quảng cáo đã bị đóng
    fun onAdDismissedFullScreenContent(isAppOpen:Boolean){}

    // Không thể hiển thị quảng cáo
    fun onAdFailedToShowFullScreenContent(adError: AdError, isAppOpen:Boolean){}

    // Ghi nhận impression của quảng cáo
    fun onAdImpression(isAppOpen:Boolean){}

    //  Quảng cáo đã hiển thị
    fun onAdShowedFullScreenContent(isAppOpen:Boolean){}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}


    //confirm change param isAppOpen to ad_type
    fun onNextAction(isAppOpen:Boolean){}
}