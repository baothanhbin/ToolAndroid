package com.b9default.a1_common_utils.model_remote_config.app

import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAppAllModel(
    @SerializedName("is_show_wellcomeback_screen")
    val isShowWellcomebackScreen: Boolean?,
    @SerializedName("welcomeback_tittle")
    val welcomebackTittle: String?,
    @SerializedName("app_update_diaglog_or_activity")
    val appUpdateDiaglogOrActivity: String?,
    //ANH add 20241218

    //config in remote config
    @SerializedName("api_url")
    val apiUrl: String?,
    @SerializedName("api_token")
    val apiToken: String?,
    @SerializedName("category_info")
    val categoryInfo: RemoteConfigCategory?,
    @SerializedName("sd_info")
    val sdInfo: RemoteConfigServerInfo?,
    @SerializedName("sd_info_prioritize")
    val sdInfoPrioritize: RemoteConfigServerInfo?,
    @SerializedName("cf_info")
    val cfInfo: RemoteConfigServerInfo?,
    @SerializedName("cf_info_prioritize")
    val cfInfoPrioritize: RemoteConfigServerInfo?,
    @SerializedName("cf_generate_time")
    val cfGenerateTime: String?,
    @SerializedName("sd_generate_time")
    val sdGenerateTime: String?,
    @SerializedName("is_show_priority")
    val isShowPriority: Boolean?,
    @SerializedName("limit_number_generate")
    val limitNumberGenerate: Int?,
    @SerializedName("notShowAd")
    val notShowAd: List<String?>?,
    @SerializedName("allowShowAd")
    val allowShowAd: List<String?>?,
    @SerializedName("version_prioritize")
    val versionPrioritize: String?
)

data class RemoteConfigServerInfo(
    @SerializedName("url")
    val url: String?,
    @SerializedName("key_name")
    val keyName: String?,
    @SerializedName("key_value")
    val keyValue: String?,
    @SerializedName("client_code")
    val clientCode: String?
)

data class RemoteConfigCategory(
    @SerializedName("noco_api_url_category")
    val nocoApiUrlCategory: String?,
    @SerializedName("noco_api_key_category")
    val nocoApiKeyCategory: String?,
    @SerializedName("noco_api_token_category")
    val nocoApiTokenCategory: String?
)
