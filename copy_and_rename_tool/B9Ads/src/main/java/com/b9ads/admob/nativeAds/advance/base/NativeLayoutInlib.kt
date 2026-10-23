package com.b9ads.admob.nativeAds.advance.base

import androidx.annotation.LayoutRes
import com.b9ads.R

// Các native mặc định trong lib
//layout native va layout loading

enum class NativeLayoutInlib(val key: String, @LayoutRes val layoutNativeId: Int, @LayoutRes val shimmerLayoutId: Int) {
    LIB_ADS_NATIVE_DEFAULT_LAYOUT("ads_native_default", R.layout.ads_native_default_layout, R.layout.ads_native_default_shimmer),
//    ADS_NATIVE_INLIB_1_INFO_2_MEDIA_3_BUTTON("ads_native_inlib_1_info_2_media_3_button", R.layout.ads_native_1_info_2_media_3_button, R.layout.ads_native_1_info_2_media_3_button_shimmer),
//    ADS_NATIVE_INLIB_1_MEDIA_2_INFO_3_BUTTON("ads_native_inlib_1_media_2_info_3_button",R.layout.ads_native_1_media_2_info_3_button, R.layout.ads_native_1_media_2_info_3_button_shimmer),
//    ADS_NATIVE_INLIB_1_MEDIA_2XXXINFOVSBUTTON("ads_native_inlib_1_media_2xxxinfovsbutton",R.layout.ads_native_1_media_2xxxinfovsbutton, R.layout.ads_native_1_media_2xxxinfovsbutton_shimmer),
//    ADS_NATIVE_INLIB_1_XXXINFOVSMEDIA_2_BUTTON("ads_native_inlib_1_xxxinfovsmedia_2_button",R.layout.ads_native_1_xxxinfovsmedia_2_button, R.layout.ads_native_1_xxxinfovsmedia_2_button_shimmer),
//    ADS_NATIVE_INLIB_1_ZZZINFOVSMEDIA_2_BUTTON("ads_native_inlib_1_zzzinfovsmedia_2_button", R.layout.ads_native_1_zzzinfovsmedia_2_button, R.layout.ads_native_1_zzzinfovsmedia_2_button_shimmer)
}
