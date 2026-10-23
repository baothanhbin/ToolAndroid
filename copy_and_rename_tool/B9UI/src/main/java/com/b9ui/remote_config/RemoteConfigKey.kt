//list all remote config here
//include default and custom add remove config

package com.b9ui.remote_config

object RemoteConfigKey {
    //app
    const val app_config = "app_config"
    const val ad_config_all = "ad_config_all"
    const val ad_appopen_resume = "ad_appopen_resume"

    //chua su dung hien tai, su dung ver tiep theo de refactor code
    const val app_api_config = "app_api_config"


    //splash
    const val screen_splash = "screen_splash"
    const val ad_native_splash = "ad_native_splash"
    const val ad_banner_splash = "ad_banner_splash"

    //language
    const val screen_language_start = "screen_language_start"
    const val ad_native_language_bottom = "ad_native_language_bottom"

    //intro
    const val screen_intro = "screen_intro"
    const val ad_inter_intro = "ad_inter_intro"
    const val ad_native_intro = "ad_native_intro"
    const val ad_native_intro_full = "ad_native_intro_full"

    //permission
    const val ad_native_permission = "ad_native_permission"

    //chua su dung
    const val screen_permission = "screen_permission"

    //app update
    const val version_update = "version_update"
    const val ad_native_app_update = "ad_native_app_update"


    //exit
    const val ad_native_exit = "ad_native_exit"

    //feedback
    const val ad_native_feedback = "ad_native_feedback"
    const val screen_feedback = "screen_feedback"

    //welcome back
    const val screen_welcomeback = "screen_welcomeback"
    const val ad_native_welcomeback = "ad_native_welcomeback"
    const val ad_inter_welcomeback = "ad_inter_welcomeback"

    const val ad_banner_collap_all = "ad_banner_collap_all"
    const val ad_inter_all = "ad_inter_all"
    const val ad_reward = "ad_reward"
    const val ad_reward_inter = "ad_reward_inter"

    //20250221
    const val ad_inter_splash = "ad_inter_splash"
    const val ad_open_splash = "ad_open_splash"

    const val app_key_config = "app_key_config"
    const val ad_banner_adaptive_all = "ad_banner_adaptive_all"
}