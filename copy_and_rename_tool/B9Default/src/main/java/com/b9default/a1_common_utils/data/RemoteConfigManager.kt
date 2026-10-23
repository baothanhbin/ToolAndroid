package com.b9default.a1_common_utils.data

import android.content.Context
import com.b9default.a1_common_utils.model_remote_config.app.AppKeyConfig
import com.b9ui.remote_config.RemoteConfigKey
import com.b9utils.util.remoteconfig.RemoteConfigOrigin

object RemoteConfigManager {
    private var appKeyConfig: AppKeyConfig? = null

    private fun getKey(key: String?, defaultKey: String): String {
        return if (key.isNullOrBlank()) defaultKey else key
    }

    fun reloadData(context: Context) {
        appKeyConfig = RemoteConfigOrigin.getConfigObject(
            context,
            RemoteConfigKey.app_key_config,
            AppKeyConfig::class.java
        )
    }

    // Feedback
    fun getKeyScreenFeedback(): String {
        val keyFromRemote = appKeyConfig?.feedbackKey?.keyScreenFeedback
        val keyDefault = RemoteConfigKey.screen_feedback
        return getKey(keyFromRemote, keyDefault)
    }

    // Splash
    fun getKeyAdNativeSplash(): String {
        val keyFromRemote = appKeyConfig?.splashKey?.keyAdNativeSplash
        val keyDefault = RemoteConfigKey.ad_native_splash
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyScreenSplash(): String {
        val keyFromRemote = appKeyConfig?.splashKey?.keyScreenSplash
        val keyDefault = RemoteConfigKey.screen_splash
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdBannerSplash(): String {
        val keyFromRemote = appKeyConfig?.splashKey?.keyAdBannerSplash
        val keyDefault = RemoteConfigKey.ad_banner_splash
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdInterSplash(): String {
        val keyFromRemote = appKeyConfig?.splashKey?.keyAdInterSplash
        val keyDefault = RemoteConfigKey.ad_inter_splash
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyOpenSplash(): String {
        val keyFromRemote = appKeyConfig?.splashKey?.keyAdOpenSplash
        val keyDefault = RemoteConfigKey.ad_open_splash
        return getKey(keyFromRemote, keyDefault)
    }

    // Other
    fun getKeyAdAppopenResume(): String {
        val keyFromRemote = appKeyConfig?.otherKey?.keyAdAppopenResume
        val keyDefault = RemoteConfigKey.ad_appopen_resume
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdConfigAll(): String {
        val keyFromRemote = appKeyConfig?.otherKey?.keyAdConfigAll
        val keyDefault = RemoteConfigKey.ad_config_all
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAppConfig(): String {
        val keyFromRemote = appKeyConfig?.otherKey?.keyAppConfig
        val keyDefault = RemoteConfigKey.app_config
        return getKey(keyFromRemote, keyDefault)
    }

    // Intro
    fun getKeyScreenIntro(): String {
        val keyFromRemote = appKeyConfig?.introKey?.keyScreenIntro
        val keyDefault = RemoteConfigKey.screen_intro
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdInterIntro(): String {
        val keyFromRemote = appKeyConfig?.introKey?.keyAdInterIntro
        val keyDefault = RemoteConfigKey.ad_inter_intro
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdNativeIntro(): String {
        val keyFromRemote = appKeyConfig?.introKey?.keyAdNativeIntro
        val keyDefault = RemoteConfigKey.ad_native_intro
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdNativeIntroFull(): String {
        val keyFromRemote = appKeyConfig?.introKey?.keyAdNativeIntroFull
        val keyDefault = RemoteConfigKey.ad_native_intro_full
        return getKey(keyFromRemote, keyDefault)
    }

    // Exit
    fun getKeyAdNativeExit(): String {
        val keyFromRemote = appKeyConfig?.exitKey?.keyAdNativeExit
        val keyDefault = RemoteConfigKey.ad_native_exit
        return getKey(keyFromRemote, keyDefault)
    }

    // Welcomeback
    fun getKeyAdNativeWelcomeback(): String {
        val keyFromRemote = appKeyConfig?.welcomebackKey?.keyAdNativeWelcomeBack
        val keyDefault = RemoteConfigKey.ad_native_welcomeback
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyScreenWelcomeback(): String {
        val keyFromRemote = appKeyConfig?.welcomebackKey?.keyScreenWelcomeBack
        val keyDefault = RemoteConfigKey.screen_welcomeback
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdInterWelcomeback(): String {
        val keyFromRemote = appKeyConfig?.welcomebackKey?.keyAdInterWelcomeBack
        val keyDefault = RemoteConfigKey.ad_inter_welcomeback
        return getKey(keyFromRemote, keyDefault)
    }

    // Language
    fun getKeyScreenLanguageStart(): String {
        val keyFromRemote = appKeyConfig?.languageKey?.keyScreenLanguageStart
        val keyDefault = RemoteConfigKey.screen_language_start
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyAdNativeLanguageBottom(): String {
        val keyFromRemote = appKeyConfig?.languageKey?.keyAdNativeLanguageBottom
        val keyDefault = RemoteConfigKey.ad_native_language_bottom
        return getKey(keyFromRemote, keyDefault)
    }

    // App Update
    fun getKeyAdNativeAppUpdate(): String {
        val keyFromRemote = appKeyConfig?.appUpdateKey?.keyAdNativeAppUpdate
        val keyDefault = RemoteConfigKey.ad_native_app_update
        return getKey(keyFromRemote, keyDefault)
    }

    fun getKeyVersionUpdate(): String {
        val keyFromRemote = appKeyConfig?.appUpdateKey?.keyVersionUpdate
        val keyDefault = RemoteConfigKey.version_update
        return getKey(keyFromRemote, keyDefault)
    }
}
