package com.b9ads.admob.nativeAds.advance.base
import androidx.annotation.LayoutRes
import com.b9ui.R



//Các native mặc định trong app
//layout native va layout shimmer loading

enum class NativeLayout(val key: String, @LayoutRes val layoutNativeId: Int, @LayoutRes val shimmerLayoutId: Int) {

    //default
    APP_ADS_NATIVE_DEFAULT("ads_native_default", R.layout.ads_native_default_layout, R.layout.ads_native_default_shimmer),

    //media full, button full up
    APP_ADS_NATIVE_1_BUTTON_2_MEDIA_3_BUTTON("ads_native_1_button_2_media_3_info", R.layout.ads_native_1_button_2_media_3_info_layout, R.layout.ads_native_1_button_2_media_3_info_shimmer),
    APP_ADS_NATIVE_1_BUTTON_2_INFO_3_BUTTON("ads_native_1_button_2_info_3_media", R.layout.ads_native_1_button_2_info_3_media_layout, R.layout.ads_native_1_button_2_info_3_media_shimmer),

    //media full, button full down
    APP_ADS_NATIVE_1_MEDIA_2INFO_3_BUTTON("ads_native_1_media_2_info_3_button", R.layout.ads_native_1_media_2_info_3_button_layout, R.layout.ads_native_1_media_2_info_3_button_shimmer),
    APP_ADS_NATIVE_1_INFO_2_MEDIA_3_BUTTON("ads_native_1_info_2_media_3_button", R.layout.ads_native_1_info_2_media_3_button_layout, R.layout.ads_native_1_info_2_media_3_button_shimmer),

    //media full button half
    APP_ADS_NATIVE_1_MEDIA_2XXXINFOVSBUTTON("ads_native_1_media_2xxxinfovsbutton", R.layout.ads_native_1_media_2xxxinfovsbutton_layout, R.layout.ads_native_1_media_2xxxinfovsbutton_shimmer),

    //media half
    APP_ADS_NATIVE_1_XXXMEDIAVSYYYINFOVSBUTTON("ads_native_1_xxxmediavsyyyinfovsbutton", R.layout.ads_native_1_xxxmediavsyyyinfovsbutton_layout, R.layout.ads_native_1_xxxmediavsyyyinfovsbutton_shimmer),
    APP_ADS_NATIVE_1_XXXINFOVSMEDIA_2_BUTTON("ads_native_1_xxxinfovsmedia_2_button", R.layout.ads_native_1_xxxinfovsmedia_2_button_layout, R.layout.ads_native_1_xxxinfovsmedia_2_button_shimmer),

    //no media
    APP_ADS_NATIVE_1_INFO_2_BUTTON("ads_native_1_info_2_button", R.layout.ads_native_1_info_2_button_layout, R.layout.ads_native_1_info_2_button_shimmer),
    APP_ADS_NATIVE_1_BUTTON_2_INFO("ads_native_1_button_2_info", R.layout.ads_native_1_button_2_info_layout, R.layout.ads_native_1_button_2_info_shimmer),
    APP_ADS_NATIVE_1_XXXINFO_BUTTON("ads_native_1_xxxinfovsbutton", R.layout.ads_native_1_xxxinfovsbutton_layout, R.layout.ads_native_1_xxxinfovsbutton_shimmer),

    //custom layout
    APP_ADS_NATIVE_CUSTOM1("ads_native_custom1", R.layout.ads_native_custom1_layout, R.layout.ads_native_custom1_shimmer),
    APP_ADS_NATIVE_CUSTOM2("ads_native_custom2", R.layout.ads_native_custom2_layout, R.layout.ads_native_custom2_shimmer),
    APP_ADS_NATIVE_CUSTOM3("ads_native_custom3", R.layout.ads_native_custom3_layout, R.layout.ads_native_custom3_shimmer),

    //fullscreen layout
    APP_ADS_NATIVE_FULLSCREEN1("ads_native_fullscreen1", R.layout.ads_native_fullscreen1_layout, R.layout.ads_native_fullscreen1_shimmer),
    APP_ADS_NATIVE_FULLSCREEN2("ads_native_fullscreen2", R.layout.ads_native_fullscreen2_layout, R.layout.ads_native_fullscreen2_shimmer),
    APP_ADS_NATIVE_FULLSCREEN3("ads_native_fullscreen3", R.layout.ads_native_fullscreen3_layout, R.layout.ads_native_fullscreen3_shimmer),


}
    //khong dung duoc
    //APP_ADS_NATIVE_1_ZZZINFOVSMEDIA_2_BUTTON("ads_native_1_zzzinfovsmedia_2_button", R.layout.ads_native_1_zzzinfovsmedia_2_button_layout, R.layout.ads_native_1_zzzinfovsmedia_2_button_shimmer),


