package com.b9ads.admob.resumeAds.callback

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError

interface OpenCallback {

    // Tải thành công ads
    fun onAdLoaded(){}

    // Tải quảng cáo thất bại
    fun onAdFailedToLoad(loadAdError: LoadAdError? = null, adError: AdError? = null){}

    //fullScreenContentCallback
    fun onAdClicked() {}

    fun onAdDismissedFullScreenContent() {}

    fun onAdFailedToShowFullScreenContent(error: AdError) {}

    fun onAdImpression() {}

    fun onAdShowedFullScreenContent() {}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}

    //custom
    //action next action
    fun onNextAction(){}

    fun onCancelLoad() {}

    fun onCancelShow() {}
}
