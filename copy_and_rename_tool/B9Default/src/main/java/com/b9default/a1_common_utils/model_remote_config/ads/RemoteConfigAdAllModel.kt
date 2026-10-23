package com.b9default.a1_common_utils.model_remote_config.ads

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
class RemoteConfigAdAllModel(
    //remote config InterManager.interInterval
    @SerializedName("inter_interval")
    var interInterval: Long? = 15,

    //remote config InterManager.interStartInterval
    @SerializedName("inter_start_interval")
    var interStartInterval: Long? = 5,
) {

    fun getInterInterval(): Long {
        return (interInterval ?: 0) * 1000
    }

    fun getInterStartInterval(): Long {
        return (interStartInterval ?: 0) * 1000
    }
}