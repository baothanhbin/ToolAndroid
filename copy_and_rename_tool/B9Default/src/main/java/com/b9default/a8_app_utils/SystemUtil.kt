package com.b9default.a8_app_utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.core.content.edit
import java.util.Locale

object SystemUtil {
    private var myLocale: Locale? = null
    private const val KEY_REGION_NAME = "KEY_REGION_NAME"
    private const val DEFAULT_LANGUAGE_CODE = "en"

    fun saveRegionName(context: Context, regionName: String?) {
        val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        preferences.edit {
            putString(KEY_REGION_NAME, regionName ?: "")
        }
    }

    fun getRegionName(context: Context): String? {
        val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString(KEY_REGION_NAME, "")
    }

    // Lưu ngôn ngữ đã cài đặt
    fun saveLocale(context: Context, lang: String?) {
        setPreLanguage(context, lang ?: DEFAULT_LANGUAGE_CODE)
    }

    // Load lại ngôn ngữ đã lưu và thay đổi chúng
    fun setLocale(context: Context) {
        val language = getPreLanguage(context)
        if (language == "") {
            val config = Configuration()
            val locale = Locale.getDefault()
            Locale.setDefault(locale)
            config.locale = locale
            context.resources
                .updateConfiguration(config, context.resources.displayMetrics)
        } else {
            changeLang(language, context)
        }
    }

    // method phục vụ cho việc thay đổi ngôn ngữ.
    private fun changeLang(lang: String?, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        myLocale = Locale(lang)
        saveLocale(context, lang)
        Locale.setDefault(myLocale)
        val config = Configuration()
        config.locale = myLocale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getPreLanguage(mContext: Context): String? {
        val preferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString("KEY_LANGUAGE", "")
    }

    private fun setPreLanguage(context: Context, language: String?) {
        if (language != null && language != "") {
            val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("KEY_LANGUAGE", language)
            editor.apply()
        }
    }

    fun setActive(context: Context, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("active", value)
        editor.apply()
    }

    fun getActive(context: Context, value: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("active", value)
    }

    fun openAppInPlayStore(context: Context) {
        val appPackageName = context.packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // context là Activity hoặc Dialog
            when (context) {
                is Activity -> {
                    context.startActivity(intent)
                }

                is Dialog -> {
                    context.context.startActivity(intent)
                }

                else -> {
                    context.startActivity(intent)
                }
            }
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            when (context) {
                is Activity -> {
                    context.startActivity(intent)
                }

                is Dialog -> {
                    context.context.startActivity(intent)
                }

                else -> {
                    context.startActivity(intent)
                }
            }
        }
    }


    fun getNextActivityFromModuleToApp(screenName: String?, defaultScreenName: String): Intent {
        if (screenName.equals("home")) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("example://app/HomeActivity")
            }
            return intent

        } else if (screenName.equals("permission")) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("example://app/PermissionActivity")
            }
            return intent
        } else {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("example://app/${defaultScreenName}")
            }
            return intent
        }
    }

//    fun getNextActivityFromModuleToPerMissionActivity(screenName:String?, defaultScreenName: String) : Intent {
//        if (screenName.equals("permission_activity")){
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("example://app/PermissionActivity")
//            }
//            return intent
//        }else{
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("example://app/${defaultScreenName}")
//            }
//            return intent
//        }
//    }
}
