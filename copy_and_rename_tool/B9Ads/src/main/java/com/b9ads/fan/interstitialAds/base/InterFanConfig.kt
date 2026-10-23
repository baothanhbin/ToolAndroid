package com.b9ads.fan.interstitialAds.base

import com.b9ads.enums.LoadState
import com.facebook.ads.InterstitialAd

class InterFanConfig {

    //id Fan
    var adUnitId: String? = ""

    var adMode: String? = ""

    // Có hiển thị quảng cáo hay không
    //remote config is_show
    var isShow: Boolean? = false

    //callback fan
    var interFanCallback: InterFanCallback? = null

    var customShowCallback: InterFanCallback? = null

    // inter fan
    var interFanAd: InterstitialAd? = null

    // Quảng đang ở trang thái nào
    var loadState: LoadState = LoadState.NOT_LOADING
}
