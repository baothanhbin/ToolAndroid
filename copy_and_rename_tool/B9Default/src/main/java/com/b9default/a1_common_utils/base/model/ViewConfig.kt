package com.b9default.a1_common_utils.base.model

import com.google.gson.annotations.SerializedName

data class ViewConfig(
    @SerializedName("pos")
    val position: Position?,
    @SerializedName("isShow")
    val isShow: Boolean? = true
)
