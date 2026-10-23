package com.b9default.a1_common_utils.model_remote_config.screens

import com.b9default.a1_common_utils.base.model.AdType
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenWelcomebackModel (
    //
    @SerializedName("screen_name")
    val screenName: String?,
    @SerializedName("not_show_screen")
    val notShowScreen: String?,
    @SerializedName("ad_wcb_fullscreen_type")
    val adWcbFullscreenType: AdType?
)
