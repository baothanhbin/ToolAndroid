/**
 * 20240924 check OK
 */

package com.b9default.a1_common_utils.model_remote_config.ads

import com.b9ads.admob.interstitialAds.preload.model.InterPreloadModel
import com.b9default.a1_common_utils.base.model.PreloadConfig
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAdInterModel(
    @SerializedName("ad_name")
    val adName: String?,

    @SerializedName("ad_id")
    val adId: String?,

    //da implemented trong InterManager - properties InterSingle.isShow and AdCommon
    @SerializedName("is_show")
    val isShow: Boolean?,

    //20250219
    @SerializedName("ad_mode")
    val adMode: String? = "load_and_show",

    //implemented properties InterSingle.isShow.isPreloadAfterShowAd
    //tu dong reload sau khi show
    //example: 1 time using screen (intro) ->setting false
    //n time using screen -> setting true
    @SerializedName("is_preload_after_show_ad")
    var isPreloadAfterShowAd: Boolean? = false,

    //implemented properties InterSingle.isShowNextActivityBeforeAd
    //neu la true thi goi man B, roi show ad
    //neu la false thi show ad, click close ad-> hien thi man B
    @SerializedName("is_show_next_activity_before_ad")
    var isShowNextActivityBeforeAd: Boolean?,

    @SerializedName("fan")
    val fan: IntroFanConfig? = null,

    // config native sau khi show inter
    @SerializedName("nativeAfter")
    val nativeAfter: String? = null,

    @SerializedName("preload")
    val preload: PreloadConfig? = null
) {
    fun toInterPreloadModel(isShowNextActivityBeforeAd: Boolean? = null): InterPreloadModel {
        return InterPreloadModel(
            id = preload?.adId ?: "",
            adName = adName ?: "",
            isShow = isShow ?: false,
            isShowNextActivityBeforeAd = isShowNextActivityBeforeAd ?: (this.isShowNextActivityBeforeAd == true),
            bufferSize = preload?.bufferSize
        )
    }
}

data class IntroFanConfig(
    @SerializedName("is_use")
    val isUse: Boolean? = true,
    @SerializedName("ad_id")
    val adId: String? = "",
    @SerializedName("load_at")
    val loadAt: LoadLocation? = null
)

enum class LoadLocation {
    @SerializedName("splash")
    SPLASH,
    @SerializedName("intro")
    INTRO
}
