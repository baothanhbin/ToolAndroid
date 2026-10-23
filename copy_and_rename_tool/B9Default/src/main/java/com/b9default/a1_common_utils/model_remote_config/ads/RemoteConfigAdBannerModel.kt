/**
 * 20240929 can xu ly them is_auto_reload_timer va ad_reload_timer_interval
 */

package com.b9default.a1_common_utils.model_remote_config.ads

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAdBannerModel(

    //not properties in manager
    @SerializedName("ad_name")
    val adName: String?,

    //properties in bannerManager
    @SerializedName("ad_id")
    val adId: String?,

    //not properties in manager,
    //using adCommon class or in app
    @SerializedName("is_show")
    val isShow: Boolean?
)
