package com.b9utils.util.appupdate

import com.google.gson.annotations.SerializedName

//default no_update
data class RemoteConfigVersionUpdateModel (
    @SerializedName("lastest_version")
    val lastest_version: String,
    @SerializedName("lastest_update_content")
    val lastest_update_content: String,
    @SerializedName("must_update")
    val must_update: String,
    @SerializedName("should_update")
    val should_update: String,
    @SerializedName("no_update")
    val no_update: String,
)