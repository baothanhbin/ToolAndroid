package com.b9ads.admob.interstitialAds.base

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.b9ads.enums.LoadState

class InterSingle() {

    //id
    var adUnitId: String = ""

    // tên quảng cáo
    //var adUnitName: String = "Interstitial"

    // Có hiển thị quảng cáo hay không
    //remote config is_show
    var isShow: Boolean = true

    //20250218
    var adMode: String = "load_and_show"

    // Sau khi hiển thị quảng cáo sẽ load lại hay khong
    //voi cac man hinh load 1 lan nhu intro thi de false
    //cac man hinh co back lai thi de true
    var isPreloadAfterShowAd = false

    //neu la true thi goi man B, roi show ad
    //neu la false thi show ad, click close ad-> hien thi man B
    //default la true, false khi 1 so truong hop dac biet co audio, muon B phat audio sau
    var isShowNextActivityBeforeAd = true

    // Quảng đang ở trang thái nào
    var loadState = LoadState.NOT_LOADING

    // Quảng cáo trả về
    var interstitialAd: InterstitialAd? = null

    //callback
    var interCallback: InterCallback? = null

}
