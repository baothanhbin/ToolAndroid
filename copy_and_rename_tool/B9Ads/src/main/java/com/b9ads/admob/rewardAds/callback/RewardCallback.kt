package com.b9ads.admob.rewardAds.callback

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

abstract class RewardCallback {

    /*User click quảng cáo*/
    open fun onAdClicked() {}

    /*User click close quảng cáo*/
    open fun onAdDismissed() {}

    /*Quảng cáo tải thất bại*/
    open fun onAdFailedToShow(adError: AdError) {}

    /*Nhận 1 quảng cáo mới*/
    open fun onAdImpression() {}

    /*Quảng cáo đang show*/
    open fun onAdShowed() {}

    /*Trả vể value*/
    open fun onUserEarnedReward(rewardItem: RewardItem) {}

    /*Quảng cáo chưa sẵn sàng để tải*/
    open fun onAdNotReady() {}

    /*Quảng cáo tải thất bại*/
    open fun onAdFailedToLoad(adError: LoadAdError) {}

    /*Quảng cáo load thành công*/
    open fun onAdLoaded(rewardedAd: RewardedAd) {}

    /*Quảng cáo load thành công*/
    open fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {}

    open fun onNotLoad() {}
}