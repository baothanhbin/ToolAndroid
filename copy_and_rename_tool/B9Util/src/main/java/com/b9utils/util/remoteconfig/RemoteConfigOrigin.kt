package com.b9utils.util.remoteconfig

import android.content.Context
import android.util.Log
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.b9utils.util.type_adapter.IntDeserializer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.GsonBuilder

object RemoteConfigOrigin {
    private const val TAG = "RemoteConfigOriginTag"

    // Lấy remote config từ firebase remote
    fun initRemoteConfig(remoteConfigDefaults: Int, listener: OnCompleteListener<Boolean>) {
        // [START get_remote_config_instance]
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        // [END get_remote_config_instance]

        // [START enable_dev_mode]
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(3600) // Thời gian làm mới tối thiểu (giây)
//            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // [START set_default_values]
        remoteConfig.setDefaultsAsync(remoteConfigDefaults)
        // [END set_default_values]

        // [START fetch_config_with_callback]
        remoteConfig.fetchAndActivate().addOnCompleteListener(listener)

    }

    fun initRemoteConfig3(
        context: Context,
        fetchRemoteConfigCallback: FetchRemoteConfigCallback,
        remoteConfigDefaultsName: String = "remote_config_defaults",
    ) {

        try {
            FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_beginF")

            // [START get_remote_config_instance]
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            // [END get_remote_config_instance]

            // [START enable_dev_mode]
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }

//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(3600) // Thời gian làm mới tối thiểu (giây)
//            .build()

            remoteConfig.setConfigSettingsAsync(configSettings)
            // [END enable_dev_mode]

            // [START set_default_values]
            setRemoteConfigDefaults(context, remoteConfig, remoteConfigDefaultsName)
            // [END set_default_values]

            // [START fetch_config_with_callback]
            FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetStart")

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnComp")
                    if (task.isSuccessful) {
                        FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnCompSuc")
                    }
                    if (task.isCanceled) {
                        FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnCompCan")
                    }
                    if (task.isComplete) {
                        FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnCompComp")
                    }
                    fetchRemoteConfigCallback.onCompleteListener(task)
                }
                .addOnFailureListener { exception ->
                    FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnFail")
                    when (exception) {
                        is FirebaseRemoteConfigFetchThrottledException -> {
                            FirebaseLogEventUtils.logEventCommonWithName(
                                context,
                                "rmcf3_fetOnFail_Thr"
                            )
                        }

                        else -> {
                            FirebaseLogEventUtils.logEventCommonWithName(
                                context,
                                "rmcf3_fetOnFail_Unk"
                            )
                        }
                    }
                    fetchRemoteConfigCallback.onFailureListener(exception)
                }
                .addOnSuccessListener {
                    FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnSuc")
                    fetchRemoteConfigCallback.onSuccessListener()
                }
                .addOnCanceledListener {
                    FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetOnCancel")
                    fetchRemoteConfigCallback.onCanceledListener()
                }
            FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetEndF")
        } catch (ex: Exception) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_fetExeception")
        }
    }

    private fun setRemoteConfigDefaults(
        context: Context,
        remoteConfig: FirebaseRemoteConfig,
        remoteConfigDefaultName: String,
    ) {
        try {
            val resId = context.resources.getIdentifier(
                remoteConfigDefaultName,
                "xml",
                context.packageName
            )
            if (resId != 0) {
                FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_def_success")
                remoteConfig.setDefaultsAsync(resId)
            } else {
                FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_def_fail")
            }
        } catch (e: Exception) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "rmcf3_def_except")
        }
    }

    private fun getRemoteConfigString(key: String): String {
        val remoteConfig = Firebase.remoteConfig
        return remoteConfig.getString(key)
    }


    // Convert json to object
    fun <T> getConfigObject(context: Context, nameConfig: String, objectConvert: Class<T>): T? {

        val json = getRemoteConfigString(nameConfig)
        //screen and key for logging
        val key = makeKeyShortName(nameConfig)
        val screen = makeActivityShortName(context.javaClass.simpleName.toString())
        return if (json != "") {
            try {
                val gson = GsonBuilder()
                    .registerTypeAdapter(Int::class.javaObjectType, IntDeserializer())
                    .create()
                gson.fromJson(json, objectConvert)

            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseLogEventUtils.logEventConfigModelNull(context, screen, key + "Ex")
                null
            }
        } else {
            FirebaseLogEventUtils.logEventConfigModelNull(context, screen, key + "Js")
            null
        }
    }
}

