package com.b9default.a1_common_utils.model_remote_config.ads

import com.b9ads.admob.nativeAds.collapse.model.NativeModel
import com.b9ads.custom.nativeCollapse.model.ClosePosition
import com.b9default.a1_common_utils.base.model.Position
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAdNativeCollap(
    @SerializedName("showBtnClose")
    val showBtnClose: Boolean?,
    @SerializedName("delayShowBtn")
    val delayShowBtn: Int?,
    @SerializedName("closePosition")
    val closePosition: Position?,
    @SerializedName("native")
    val native: RemoteConfigAdNativeModel?,
    @SerializedName("banner")
    val banner: RemoteConfigAdBannerModel? = null,
) {
    fun toNativeModel(): NativeModel {
        return NativeModel(
            native?.adId ?: "",
            native?.adName ?: "",
            native?.isReloadWhenResume == true,
            native?.isReloadWhenVideoEnd == true,
            native?.reloadTimeWhenNotVideo ?: 0,
            native?.maxPreload ?: 0,
            showBtnClose == true,
            delayShowBtn ?: 0,
            closePosition = when(closePosition) {
                Position.TOP_LEFT -> ClosePosition.TOP_LEFT
                Position.BOTTOM_LEFT -> ClosePosition.BOTTOM_LEFT
                Position.BOTTOM_RIGHT -> ClosePosition.BOTTOM_RIGHT
                else -> ClosePosition.TOP_RIGHT
            }
        )
    }
}
