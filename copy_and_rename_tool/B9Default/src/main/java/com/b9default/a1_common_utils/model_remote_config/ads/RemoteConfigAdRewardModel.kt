package com.b9default.a1_common_utils.model_remote_config.ads

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAdRewardModel(
    @SerializedName("ad_name")
    val adName: String?,
    @SerializedName("ad_id")
    val adId: String?,
    @SerializedName("is_show")
    val isShow: Boolean?
)