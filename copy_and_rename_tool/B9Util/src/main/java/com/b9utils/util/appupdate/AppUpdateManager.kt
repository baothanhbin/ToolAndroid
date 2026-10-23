/**
 * using in main/home screen
 */
package com.b9utils.util.appupdate

import android.content.Context
import com.b9utils.util.appVersion.AppVersion

object AppUpdateManager {
    var checkUpdate: Int = 0
    var updateContent: String = ""
    var isShowAppUpdate: Boolean = true

    /**
     * return 0: no update
     * return 1: should update
     * return 2: must update
     */
    fun checkUpdate(context: Context, updateInfo: RemoteConfigVersionUpdateModel): Int {
        try {
            val currentVersion = AppVersion.getCurrentVersion(context)
            //setting update content
            updateContent = updateInfo.lastest_update_content

            //check update
            val lastestVersion: String = updateInfo.lastest_version
            if ((currentVersion.isEmpty()) || (lastestVersion.isEmpty()))
                return 0;

            val lCurrentVersion: Long = currentVersion.replace(".", "").toLong()
            val lLastestVersion: Long = updateInfo.lastest_version.replace(".", "").toLong()
            if ((lCurrentVersion <= 1) || (lLastestVersion <= 1))
                return 0;

            if (lCurrentVersion >= lLastestVersion)
                return 0;

            //no update
            val noUpdateList: List<String> = updateInfo.no_update.replace(".", "").split(",")
            if (noUpdateList.contains(currentVersion.replace(".", "")))
                return 0

            //should update
            val shouldUpdateList: List<String> = updateInfo.should_update.replace(".", "").split(",")
            if (shouldUpdateList.contains(currentVersion.replace(".", "")))
                return 1

            //must update
            val mustUpdateList: List<String> = updateInfo.must_update.replace(".", "").split(",")
            if (mustUpdateList.contains(currentVersion.replace(".", "")))
                return 2

            //if not config
            return 0
        } catch (_: Exception) {
            return 0
        }
    }
}