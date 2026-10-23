package com.b9default.a1_common_utils.model_remote_config.app

import com.google.gson.annotations.SerializedName

// 09/10/2025 Cuong fix #139
data class AppKeyConfig (
    @SerializedName("feedback")
    val feedbackKey: FeedbackKey?,

    @SerializedName("splash")
    val splashKey: SplashKey?,

    @SerializedName("other")
    val otherKey: OtherKey?,

    @SerializedName("intro")
    val introKey: IntroKey?,

    @SerializedName("exit")
    val exitKey: ExitKey?,

    @SerializedName("welcomeback")
    val welcomebackKey: WelcomeBackKey?,

    @SerializedName("language")
    val languageKey: LanguageKey?,

    @SerializedName("app_update")
    val appUpdateKey: AppUpdateKey?
)

data class FeedbackKey(
    @SerializedName("key_screen_feedback")
    val keyScreenFeedback: String?
)

data class SplashKey(
    @SerializedName("key_ad_native_splash")
    val keyAdNativeSplash: String?,

    @SerializedName("key_screen_splash")
    val keyScreenSplash: String?,

    @SerializedName("key_ad_splash")
    val keyAdSplash: String?,

    @SerializedName("key_ad_banner_splash")
    val keyAdBannerSplash: String?,

    @SerializedName("key_ad_inter_splash")
    val keyAdInterSplash: String?,

    @SerializedName("key_ad_open_splash")
    val keyAdOpenSplash: String?
)

data class OtherKey(
    @SerializedName("key_ad_appopen_resume")
    val keyAdAppopenResume: String?,

    @SerializedName("key_ad_config_all")
    val keyAdConfigAll: String?,

    @SerializedName("key_app_config")
    val keyAppConfig: String?,

    @SerializedName("key_app_api_config")
    val keyAppApiConfig: String?
)

data class IntroKey(
    @SerializedName("key_screen_intro")
    val keyScreenIntro: String?,

    @SerializedName("key_ad_inter_intro")
    val keyAdInterIntro: String?,

    @SerializedName("key_ad_native_intro")
    val keyAdNativeIntro: String?,

    @SerializedName("key_ad_native_intro_full")
    val keyAdNativeIntroFull: String?
)

data class ExitKey(
    @SerializedName("key_ad_native_exit")
    val keyAdNativeExit: String?
)

data class WelcomeBackKey(
    @SerializedName("key_ad_native_welcomeback")
    val keyAdNativeWelcomeBack: String?,

    @SerializedName("key_screen_welcomeback")
    val keyScreenWelcomeBack: String?,

    @SerializedName("key_ad_inter_welcomeback")
    val keyAdInterWelcomeBack: String?
)

data class LanguageKey(
    @SerializedName("key_screen_language_start")
    val keyScreenLanguageStart: String?,

    @SerializedName("key_ad_native_language_top")
    val keyAdNativeLanguageTop: String?,

    @SerializedName("key_ad_native_language_bottom")
    val keyAdNativeLanguageBottom: String?
)

data class AppUpdateKey(
    @SerializedName("key_ad_native_app_update")
    val keyAdNativeAppUpdate: String?,

    @SerializedName("key_screen_app_update")
    val keyScreenAppUpdate: String?,

    @SerializedName("key_version_update")
    val keyVersionUpdate: String?
)
