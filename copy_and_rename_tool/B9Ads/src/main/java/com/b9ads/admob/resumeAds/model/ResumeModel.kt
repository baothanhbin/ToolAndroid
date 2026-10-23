package com.b9ads.admob.resumeAds.model

import com.b9ads.model.PreloadConfig

// 27/10/2025 Cuong fix #147
data class ResumeModel(
    val id: String,
    val name: String,
    val isShow: Boolean,
    val preload: PreloadConfig
)
