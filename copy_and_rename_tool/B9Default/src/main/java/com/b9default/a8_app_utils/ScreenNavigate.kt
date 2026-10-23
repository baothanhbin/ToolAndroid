package com.b9default.a8_app_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.b9default.ui.ads.NativeFullActivity
import com.b9default.ui.intro.IntroActivity
import com.b9default.ui.intro1.IntroActivity1
import com.b9default.ui.language.LanguageStartActivity
import com.b9default.ui.update.AppUpdateActivity

// Tim kiếm và mở activity tiếp theo dựa trên tên activity được cung cấp
// đồng thời đóng activity cũ
object ScreenNavigate {

    fun nextScreenAndFinishCurrent(
        activity: Activity,
        nextScreenFull: String?,
        defaultClass: Class<*>? = null,
        withNativeFull: Boolean = false,
        remoteConfigKeyInter: String? = null,
    ) {
        val intent = getIntent(activity.applicationContext, nextScreenFull, defaultClass) ?: return
        if (withNativeFull && remoteConfigKeyInter.isNullOrBlank().not()) {
            val intentNativeFull = Intent(activity.applicationContext, NativeFullActivity::class.java).apply {
                putExtra(NativeFullActivity.KEY_NEXT_INTENT, intent)
                putExtra(NativeFullActivity.KEY_INTER, remoteConfigKeyInter)
            }
            activity.startActivity(intentNativeFull)
        } else {
            activity.startActivity(intent)
        }
        activity.finishAffinity()
    }

    fun getIntent(
        context: Context,
        nextScreenFull: String?,
        defaultClass: Class<*>? = null,
    ): Intent? {

        var nextScreenName = nextScreenFull?.replace("app@", "") ?: ""
        nextScreenName = nextScreenName.replace("lib@", "")
        return if (nextScreenFull?.contains("app@") == true) {
            getNextActivityFromModuleToApp(nextScreenName)
        } else if (nextScreenFull?.contains("lib@") == true) {
            val activity = getActivityClass(nextScreenName, defaultClass) ?: return null
            Intent(context, activity)
        } else if (nextScreenFull == null && defaultClass != null) {
            Intent(context, defaultClass)
        } else {
            getNextActivityFromModuleToApp(nextScreenName)
        }
    }

    private fun getActivityClass(name: String?, defaultClass: Class<*>?): Class<*>? {
        return when (name) {
            "language" -> LanguageStartActivity::class.java
            "intro" -> IntroActivity::class.java
            "intro1" -> IntroActivity1::class.java
            "app_update" -> AppUpdateActivity::class.java
            else -> defaultClass
        }
    }

    private fun getNextActivityFromModuleToApp(screenName: String?): Intent {
        val activityName = when (screenName) {
            "home", null -> "HomeActivity"
            "permission" -> "PermissionActivity"
            else -> screenName
        }
        return Intent(Intent.ACTION_VIEW).apply {
            data = "example://app/$activityName".toUri()
        }
    }
}
