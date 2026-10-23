package com.b9default.a1_common_utils.base.model

import com.google.gson.annotations.SerializedName

data class PreloadConfig(
    @SerializedName("isUse")
    val isUse: Boolean? = false,
    @SerializedName("adId")
    val adId: String? = null,
    @SerializedName("bufferSize")
    val bufferSize: Int? = null
)