/**
 * 20290929 chek OK
 */

package com.b9default.a1_common_utils.model_remote_config.ads

import android.os.Parcelable
import com.b9default.a1_common_utils.base.model.Position
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// 08/10/2025 Cuong fix #137
@Parcelize
class RemoteConfigAdNativeModel(
    @SerializedName("ad_name")
    var adName: String? = "",

    //NativeAdvanManager0.ad_id
    @SerializedName("ad_id")
    var adId: String? = "",

    /**
     * list id dùng cho trường hợp nhiều native full,
     * config ad_native_full_pos1 và ad_native_full_pos2 trong
     * screen_intro có thể dẫn nhiều native full tại màn này
     */
    @SerializedName("listId")
    val listId: List<String?>?,

    //not using in NativeAdvanceNanager,
    //setting in AdCommon or in app
    @SerializedName("is_show")
    var isShow: Boolean? = true,

    //max preload native times
    //param in NativeAdvanceManager0.preloadedMaxTimes
    //for height show rate
    @SerializedName("max_preload")
    var maxPreload: Int? = 0,

    //Timer reload setting (default reload resume, trigger : yes)
    //setting in AdCommon
    //before using set NativeAdvanceManager1.isAutoReloadByTimer properties
    @SerializedName("is_auto_reload_timer")
    var isAutoReloadTimer: Boolean? = true,

    //id for reload timer
    @SerializedName("ad_id_rd_timer")
    var adIdRdTimer: String? = "",

    //dung trong NativeAdvanceManager1.timerReloadInterval
    //setting in AdCommon
    //before using set NativeAdvanceManager1.ad_reload_timer_interval properties
    @SerializedName("ad_reload_timer_interval")
    var adReloadTimerInterval: Long? = 0,

    //remote config
    @SerializedName("is_reaload_when_resume")
    var isReloadWhenResume: Boolean? = false,

    //remote config
    @SerializedName("is_reaload_when_video_end")
    var isReloadWhenVideoEnd: Boolean? = false,

    //remote config
    @SerializedName("reload_time_when_not_video")
    var reloadTimeWhenNotVideo: Long? = 0,

    //change button click only when Fan
    //setting in AdCommon
    //before using set NativeAdvanceManager2.is_change_button_click_only_when_fan
    @SerializedName("is_change_button_click_only_when_fan")
    var isChangeButtonClickOnlyWhenFan: Boolean? = false,

    //Anh add 20241125
    //change all disable click when Fan
    //setting in AdCommon
    @SerializedName("is_change_disable_click_all_when_fan")
    var isChangeDisableClickAllWhenFan: Boolean? = false,

    //auto reload when video end -chua implement
    //ANH dang nghien cuu spec reload cung voi Cuong
    //var is_auto_reload_when_media_finish: Boolean = true,

    //native ad layout file
    //setting in App when call loadAndShowNative function
    @SerializedName("ad_layout_file")
    var adLayoutFile: String? = "",

    //background color
    @SerializedName("bg_color")
    var bgColor: String? = "",

    //border color layout
    @SerializedName("border_color")
    var borderColor: String? = "",

    //20250306
    @SerializedName("ad_layout_fan")
    var adLayoutFan: String? = "",
    @SerializedName("bg_color_fan")
    var bgColorFan: String? = "",
    @SerializedName("border_color_fan")
    var borderColorFan: String? = "",
    @SerializedName("button_color_fan")
    var buttonColorFan: String? = "",

    //20250304 add id
    @SerializedName("ad_id_l1")
    var adIdL1: String? = "",
    @SerializedName("ad_id_l2")
    var adIdL2: String? = "",

    //native1, native2
    @SerializedName("ad_ver")
    var adVer: String? = "",
    @SerializedName("clicked")
    val clicked: ClickedConfig? = null,
    @SerializedName("btnNext")
    val btnNext: BtnNextConfig? = null,
    @SerializedName("nativeDrop")
    val nativeDrop: NativeDrop? = null,
): Parcelable

@Parcelize
data class BtnNextConfig(
    @SerializedName("pos")
    val pos: Position?
): Parcelable

@Parcelize
data class ClickedConfig(
    @SerializedName("btnActColor")
    val btnActColor: String?,
    @SerializedName("delayTimeShowBtnNext")
    val delayTimeShowBtnNext: Float?
): Parcelable

@Parcelize
data class NativeDrop(
    @SerializedName("isUse")
    val isUse: Boolean?,
    @SerializedName("adId")
    val adId: List<String?>?
): Parcelable

