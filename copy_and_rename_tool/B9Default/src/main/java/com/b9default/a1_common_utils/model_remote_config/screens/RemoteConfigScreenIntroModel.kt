package com.b9default.a1_common_utils.model_remote_config.screens

import com.b9default.a1_common_utils.base.model.ViewConfig
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenIntroModel(
    //fix properties
    @SerializedName("screen_name")
    val screenName: String?,
    @SerializedName("is_hide_navi_menu")
    val isHideNaviMenu: Boolean?,
    @SerializedName("next_screen_default")
    val nextScreenDefault: String?,

    //Anh 20241117 add
    @SerializedName("is_reload_ad_when_change_slide")
    val isReloadAdWhenChangeSlide: Boolean?,

    //custom properties
    @SerializedName("is_slide1_show")
    val isSlide1Show: Boolean?,
    @SerializedName("is_slide1_ad_show")
    val isSlide1AdShow: Boolean?,
    @SerializedName("is_slide1_next_btn")
    val isSlide1NextBtn: Boolean? = false,

    @SerializedName("is_slide2_show")
    val isSlide2Show: Boolean?,
    @SerializedName("is_slide2_ad_show")
    val isSlide2AdShow: Boolean?,
    @SerializedName("is_slide2_next_btn")
    val isSlide2NextBtn: Boolean? = false,

    @SerializedName("is_slide3_show")
    val isSlide3Show: Boolean?,
    @SerializedName("is_slide3_ad_show")
    val isSlide3AdShow: Boolean?,
    @SerializedName("is_slide3_next_btn")
    val isSlide3NextBtn: Boolean? = false,

    @SerializedName("is_slide4_show")
    val isSlide4Show: Boolean?,
    @SerializedName("is_slide4_ad_show")
    val isSlide4AdShow: Boolean?,
    @SerializedName("is_slide4_next_btn")
    val isSlide4NextBtn: Boolean? = false,

    //adjust layout
    @SerializedName("distance_button_next_vs_ad")
    val distanceButtonNextVsAd: Int?,
    @SerializedName("font_size")
    val fontSize: Float?,

    //native fullscreen pisition
    @SerializedName("ad_native_full_pos1")
    val adNativeFullPos1: Int?,
    @SerializedName("ad_native_full_pos2")
    val adNativeFullPos2: Int?,
    // data của btn next bao gồm vị trí, ẩn hiện ...
    @SerializedName("btnNextConfig")
    val btnNextConfig: ViewConfig?,
    @SerializedName("btnCloseNativeFull")
    val btnCloseConfig: BtnCloseConfig?
)

data class BtnCloseConfig(
    @SerializedName("isShow")
    val isShow: Boolean?,
    @SerializedName("delayShow")
    val delay: Float?
)
