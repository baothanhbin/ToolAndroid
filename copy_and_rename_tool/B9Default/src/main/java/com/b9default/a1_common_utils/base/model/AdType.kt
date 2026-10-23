package com.b9default.a1_common_utils.base.model

import com.google.gson.annotations.SerializedName

enum class AdType(val value: String) {
    @SerializedName("resume", alternate = ["open"])
    OPEN("resume"),
    @SerializedName("inter")
    INTER("inter"),
}
