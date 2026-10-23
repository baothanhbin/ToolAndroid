///* get from remote config and store in SharedPreferences*/
//package com.b9utils.util.remoteconfig
//
//import android.content.Context
//import android.content.SharedPreferences
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
//import com.google.gson.Gson
//import com.b9utils.util.FirebaseLogEventUtils
//import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
//import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
//
//object RemoteConfig {
//    // SharedPreferences
//    private const val SHARED_PREFERENCES_NAME = "RemoteConfig"
//
//    // Lấy remote config từ firebase remote
//    fun initRemoteConfig(remoteConfigDefaults: Int, listener: OnCompleteListener<Boolean>) {
//        FirebaseRemoteConfig.getInstance().reset()
//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(3600)
//            .build()
//
//        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(configSettings)
//        FirebaseRemoteConfig.getInstance().setDefaultsAsync(remoteConfigDefaults)
//        FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener(listener)
//    }
//
//    // Lấy remote trên firebase kiểu Boolean
//    private fun getRemoteConfigBoolean(adUnitId: String): Boolean {
//        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        return mFirebaseRemoteConfig.getBoolean(adUnitId)
//    }
//
//    // Lấy remote trên firebase kiểu String
//    private fun getRemoteConfigLong(adUnitId: String): Long {
//        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        return mFirebaseRemoteConfig.getLong(adUnitId)
//    }
//
//    // Lấy remote trên firebase kiểu Long
//    private fun getRemoteConfigString(adUnitId: String): String {
//        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        return mFirebaseRemoteConfig.getString(adUnitId)
//    }
//
//    // Lấy remote kiểu boolean từ Shared preferences
//    fun getConfigBoolean(context: Context, nameConfig: String): Boolean {
//        val pre = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
//        return pre.getBoolean(nameConfig, true)
//    }
//
//    // Lưu dữ liệu remote kiểu boolean vào Shared preferences
//    fun setConfigBoolean(context: Context, nameConfig: String, config: Boolean) {
//        val pre = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
//        val editor: SharedPreferences.Editor = pre.edit()
//        editor.putBoolean(nameConfig, config)
//        editor.apply()
//    }
//
//    // Lấy remote kiểu string từ Shared preferences
//    fun getConfigString(context: Context, nameConfig: String): String {
//        val pre = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//        return pre.getString(nameConfig, "") ?: ""
//    }
//
//    // Lưu dữ liệu remote kiểu string vào Shared preferences
//    private fun setConfigString(context: Context, nameConfig: String, config: String) {
//        val pre = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//        val editor: SharedPreferences.Editor = pre.edit()
//        editor.putString(nameConfig, config)
//        editor.apply()
//    }
//
//    // Lấy remote kiểu long từ Shared preferences
//    fun getConfigLongX1000(context: Context, nameConfig: String): Long {
//        val pre = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//        return pre.getLong(nameConfig, 0L) * 1000L
//    }
//
//    // Lấy remote kiểu long từ Shared preferences
//    fun getConfigLong(context: Context, nameConfig: String): Long {
//        val pre = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//        return pre.getLong(nameConfig, 0L)
//    }
//
//    // Lưu dữ liệu remote kiểu long vào Shared preferences
//    private fun setConfigLong(context: Context, nameConfig: String, config: Long) {
//        val pre = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//        val editor: SharedPreferences.Editor = pre.edit()
//        editor.putLong(nameConfig, config)
//        editor.apply()
//    }
//
//    // Convert json to object
//    fun <T> getConfigObject(context: Context, nameConfig: String, objectConvert: Class<T>): T? {
//        val json = getConfigString(context, nameConfig)
//
//        //screen and key for logging
//        val screen = makeActivityShortName(context.javaClass.simpleName.toString())
//        //key
//        val key = makeKeyShortName(nameConfig)
//
//        return if (json != "") {
//            try {
//                //ANH 20241231 comment out because lot of log event
//                //FirebaseLogEventUtils.logEventConfigModelNotNull(context, screen, key)
//                Gson().fromJson(json, objectConvert)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                FirebaseLogEventUtils.logEventConfigModelNull(context, screen, key + "Ex")
//                null
//            }
//        } else {
//            FirebaseLogEventUtils.logEventConfigModelNull(context, screen, key + "Json")
//            null
//        }
//    }
//
//    // Lưu tất cả các remote vào Shared preferences
//    fun saveAllRemoteConfigBoolean(
//        context: Context,
//        listKeyRemoteConfigBoolean: MutableList<String> = mutableListOf()
//    ) {
//        listKeyRemoteConfigBoolean.forEach { remoteConfig ->
//            setConfigBoolean(
//                context,
//                remoteConfig,
//                getRemoteConfigBoolean(remoteConfig)
//            )
//        }
//
//    }
//
//    fun saveAllRemoteConfigLong(
//        context: Context,
//        listKeyRemoteConfigLong: MutableList<String> = mutableListOf()
//    ) {
//
//        listKeyRemoteConfigLong.forEach { remoteConfig ->
//            setConfigLong(
//                context,
//                remoteConfig,
//                getRemoteConfigLong(remoteConfig)
//            )
//        }
//    }
//
//    fun saveAllRemoteConfigString(
//        context: Context,
//        listKeyRemoteConfigString: MutableList<String> = mutableListOf()
//    ) {
//        // Save config string
//
//        listKeyRemoteConfigString.forEach { remoteConfig ->
//            setConfigString(
//                context,
//                remoteConfig,
//                getRemoteConfigString(remoteConfig)
//            )
//        }
//    }
//
//}
//
