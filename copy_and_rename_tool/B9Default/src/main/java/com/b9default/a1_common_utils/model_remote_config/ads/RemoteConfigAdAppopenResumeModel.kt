/**
 * 20241001 check OK
 */

package com.b9default.a1_common_utils.model_remote_config.ads

import com.b9ads.admob.resumeAds.model.ResumeModel
import com.b9default.a1_common_utils.base.model.PreloadConfig
import com.google.gson.annotations.SerializedName

// 08/10/2025 Cuong fix #137
data class RemoteConfigAdAppopenResumeModel(

    //not using
    @SerializedName("ad_name")
    val adName: String?,

    //properties ad_id in manager
    //using setId in setting function in app
    @SerializedName("ad_id")
    val adId: String?,

    //properties is_show in manager
    //using setIsShow in setting function in app
    @SerializedName("is_show")
    val isShow: Boolean?,

    // 27/10/2025 Cuong fix #147
    @SerializedName("preload")
    val preload: PreloadConfig?
) {
    fun toResumeModel(): ResumeModel {
        return ResumeModel(
            id = adId ?: "",
            name = adName ?: "",
            isShow = isShow ?: false,
            preload = com.b9ads.model.PreloadConfig(
                id = preload?.adId ?: "",
                isUse = preload?.isUse == true,
                bufferSize = preload?.bufferSize
            )
        )
    }
}
