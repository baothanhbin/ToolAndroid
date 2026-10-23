package com.b9default.a1_common_utils.base.model

import com.google.gson.annotations.SerializedName

enum class Position {
    @SerializedName("left")
    LEFT,
    @SerializedName("right")
    RIGHT,
    @SerializedName("center")
    CENTER,

    @SerializedName("TOP_LEFT")
    TOP_LEFT,

    @SerializedName("TOP_RIGHT")
    TOP_RIGHT,

    @SerializedName("BOTTOM_LEFT")
    BOTTOM_LEFT,

    @SerializedName("BOTTOM_RIGHT")
    BOTTOM_RIGHT,
}
