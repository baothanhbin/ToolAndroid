package com.b9default.ui.intro1.model

data class IntroModel1(
    val image: Int = -1,
    val title: Int = -1,
    val content: Int = -1,
    val isNativeFull: Boolean = false,
    val showBtnNextLarge: Boolean = false,
    val showBtnClose: Boolean = false,
    val delayShowBtn: Float = 0f,
    val isShow: Boolean = true,
    val isShowNativeBot: Boolean = false
)
