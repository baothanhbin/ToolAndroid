package com.b9ads.admob.interstitialAds.base

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd

interface InterCallback {
    //InterstitialAdLoadCallback
    // Tải thành công ads
    fun onAdLoaded(interstitialAd: InterstitialAd?){}

    // Tải quảng cáo thất bại
    fun onAdFailedToLoad(loadAdError: LoadAdError? = null, adError: AdError? = null){}

    //fullScreenContentCallback
    // Quảng cáo đã được nhấp vào
    fun onAdClicked(){}

    // Quảng cáo đã bị đóng
    fun onAdDismissedFullScreenContent(){}

    // Không thể hiển thị quảng cáo
    fun onAdFailedToShowFullScreenContent(adError: AdError){}

    // Ghi nhận impression của quảng cáo
    fun onAdImpression(){}

    //  Quảng cáo đã hiển thị
    fun onAdShowedFullScreenContent(){}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}

    //custom
    //action chuyển màn sau khi show quảng cáo
    fun onNextAction(){}

    fun onCancelToLoad(){}

    fun onCancelToShow(){}

}