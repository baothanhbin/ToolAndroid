package com.b9default.a1_common_utils.model_remote_config.screens

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenHomeModel (
    //fix properties
    @SerializedName("screen_name")
    val screenName: String?,
    @SerializedName("is_hide_navi_menu")
    val isHideNaviMenu: Boolean?,

    //custom properties
    @SerializedName("ad_placement")
    val adPlacement: String?,
    @SerializedName("ad_type")
    val adType: String?,

    )