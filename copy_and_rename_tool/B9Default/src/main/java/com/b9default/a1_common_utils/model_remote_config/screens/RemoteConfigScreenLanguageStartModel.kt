package com.b9default.a1_common_utils.model_remote_config.screens

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigScreenLanguageStartModel (
    //fix properties
    @SerializedName("screen_name")
    val screenName: String?,
    @SerializedName("is_hide_navi_menu")
    val isHideNaviMenu: Boolean?,
    @SerializedName("next_screen_default")
    val nextScreenDefault: String?,

    //anh add 20241117
    @SerializedName("is_reload_after_select_language")
    val isReloadAfterSelectLanguage: Boolean?,

    //20250212 beta-fixnative comment out because using language order
    //language adjust position
    //val lang_c_pos:Int,
    //val lang_p_pos:Int
    @SerializedName("lang_sort")
    val langSort: String? ="en,es,hi,fr,pt,de,ja,co",
    @SerializedName("trans_popup")
    val transPopup: TranslatePopupConfig? = null,
    @SerializedName("handAnimate")
    val handAnimate: HandAnimate? = null,
    @SerializedName("enableLanguageDrop")
    val enableLanguageDrop: Boolean? = null,
)

data class TranslatePopupConfig(
    @SerializedName("is_use")
    val isUse: Boolean?,
    @SerializedName("delay_time")
    val delayTime: Int?
)

data class HandAnimate(
    @SerializedName("isUse")
    val isUse: Boolean?,
    @SerializedName("pos")
    val pos: Int?,
    @SerializedName("1stOnly")
    val firstTimeOnly: Boolean?
)
