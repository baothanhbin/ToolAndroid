package com.b9utils.util.appVersion

import android.content.Context
import android.content.pm.PackageManager

object AppVersion {
    // Lấy version của app
    fun getCurrentVersion(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            return versionName.toString()
        } catch (_: PackageManager.NameNotFoundException) {
            return ""
        }
    }
}
