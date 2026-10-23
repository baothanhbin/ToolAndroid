package com.b9ads.admob.nativeAds.collapse.model

import com.b9ads.custom.nativeCollapse.model.ClosePosition

data class NativeModel(
    val adId: String,
    val adName: String,
    val isReloadWhenResume: Boolean = false,
    val isReloadWhenVideoEnd: Boolean = false,
    val reloadTimeWhenNotVideo: Long = 0,
    val maxPreload: Int = 0,
    val showBtnClose: Boolean = false,
    val delayShowBtn: Int = 0,
    val closePosition: ClosePosition = ClosePosition.TOP_RIGHT
)
