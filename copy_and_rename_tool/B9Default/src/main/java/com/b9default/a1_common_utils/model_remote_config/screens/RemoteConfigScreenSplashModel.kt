package com.b9default.a1_common_utils.model_remote_config.screens

import com.b9default.a1_common_utils.base.model.AdType
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenSplashModel (
    //
    @SerializedName("screen_name")
    val screenName: String?,
    @SerializedName("next_screen_default")
    val nextScreenDefault: String?,

    //ad type
    //banner: banner adaptive
    //native: native advance
    @SerializedName("inscreen_ad_type")
    val inscreenAdType:String?,
    @SerializedName("inscreen_ad_show_delay_time")
    val inscreenAdShowDelayTime: Long?,

    //not implement yet
    //true: load and show language when splash ad show
    //false: load and show language when dismiss ad splash
    @SerializedName("is_preload_language_screen")
    val isPreloadLanguageScreen: Boolean?,

    // Does the text change over time
    @SerializedName("is_use_text_animate")
    val isUseTextAnimate: Boolean? = null,
    @SerializedName("screenVersion")
    val screenVersion: ScreenVersion? = null,
    @SerializedName("fullScreenAdType")
    val fullScreenAdType: AdType?,
    @SerializedName("interDelayShowWhenShowNative")
    val interDelayShowWhenShowNative: Int?,
    @SerializedName("interTimeoutWhenShowNative")
    val interTimeoutWhenShowNative: Int?,
    @SerializedName("timeoutNextScreenWhenShowNative")
    val timeoutNextScreenWhenShowNative: Int?,
)

enum class ScreenVersion {
    /**
     * version 1:
     * Logic show inter splash cũ
     */
    @SerializedName("ver1")
    VER_1,
    /**
     * version 1:
     * Logic show inter splash bổ sung xử ký trường hợp app dưới background sẽ
     * không show inter ngay
     */
    @SerializedName("ver2")
    VER_2
}
