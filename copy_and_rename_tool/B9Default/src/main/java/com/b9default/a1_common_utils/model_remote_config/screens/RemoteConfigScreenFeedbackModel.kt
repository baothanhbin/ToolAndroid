package com.b9default.a1_common_utils.model_remote_config.screens

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenFeedbackModel (
    //
    @SerializedName("screen_name")
    val screenName: String?,

    //gmail: direct go gmail
    //bottom_sheet: open bottom sheet
    @SerializedName("type")
    val type: String?,

    //default email in feedback
    @SerializedName("fb_email")
    val fbEmail: String?,

    //tittle in email
    @SerializedName("fb_tittle")
    val fbTittle: String?,

    //custom properties
)