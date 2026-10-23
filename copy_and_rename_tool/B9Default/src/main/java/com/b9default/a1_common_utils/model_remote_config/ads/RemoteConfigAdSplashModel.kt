/**
 * 20290929 chek OK
 */

package com.b9default.a1_common_utils.model_remote_config.ads

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
class RemoteConfigAdSplashModel(
    //SplashAdManager.ad_id_inter
    //set in constructor
    @SerializedName("ad_id_appopen")
    var adIdAppOpen: String? = "",

    //SplashAdManager.ad_id_inter
    //set in constructor
    @SerializedName("ad_id_inter")
    var adIdInter: String? = "",

    //implemented trong SplashAdManager
    //set in constructor
    @SerializedName("is_show_app_open")
    var isShowAppOpen: Boolean? = false,

    //implemented trong SplashAdManager
    //set in constructor
    @SerializedName("is_show_inter")
    var isShowInter: Boolean? = true,

    //implemented trong SplashAdManager.AdMaxLoadingTime
    //set in constructor
    @SerializedName("max_loading_time")
    var maxLoadingTimeSecond: Long = 25,

    //implemented trong SplashAdManager.rate
    //set in constructor
    //1-30: app_open; rest: inter
    @SerializedName("appopen_rate")
    var appOpenRate: Int = 1,

    ) {
    fun getMaxLoadingTime(): Long {
        return maxLoadingTimeSecond * 1000
    }
}