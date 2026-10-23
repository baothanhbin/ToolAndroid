package com.b9ads.admob.interstitialAds.preload.model

data class InterPreloadModel(
    val id: String,
    val adName: String,
    val isShowNextActivityBeforeAd: Boolean,
    val isShow: Boolean = true,
    val bufferSize: Int? = null
)
