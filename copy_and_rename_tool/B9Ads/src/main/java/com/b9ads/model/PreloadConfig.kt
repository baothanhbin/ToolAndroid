package com.b9ads.model

data class PreloadConfig(
    val id: String,
    val isUse: Boolean,
    val bufferSize: Int? = null
)
