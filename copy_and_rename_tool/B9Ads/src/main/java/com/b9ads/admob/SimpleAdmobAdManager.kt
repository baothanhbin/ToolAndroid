package com.b9ads.admob

object SimpleAdmobAdManager {
    private const val TAG = "b9ads_AdAdmobManagerTag"
    private var timeStart: Long = 0L

    // time interval config firebase
    private var timeInterStartInterval: Long = 0L
    private var timeInterBetweenInterval = 0L
    var timeLastShowInter: Long = 0L

    //not show ad version
    private var currentVersion: String = ""
    private var notShowAdVersion: String = ""
    var isShowAdByVersion: Boolean = true


    //another ad showing
    var isShowingInter: Boolean = false
    var isShowingResume: Boolean = false
    var isShowingRewarded: Boolean = false
    var isSimpleAdScript: Boolean = false


    //function

    fun setDataInterAd(
        timeInterStartInterval: Long = 0L,
        timeInterBetweenInterval: Long = 0L,
    ) {
        this.timeStart = System.currentTimeMillis()
        this.timeInterStartInterval = timeInterStartInterval
        this.timeInterBetweenInterval = timeInterBetweenInterval

    }

    fun setDataVersionNotShowAd(
        currentVersion: String = "",
        notShowAdVersion: String = ""
    ) {
        this.currentVersion = currentVersion
        this.notShowAdVersion = notShowAdVersion
        this.isShowAdByVersion = updateIsShowAdByVersion()
    }

    fun canShowInter(): Boolean {

//        if (isShowingInter) {
//            return false
//        }
//
//        if (isShowingRewarded) {
//            return false
//        }
//
//        if (isShowingResume) {
//            return false
//        }

//        if (!isShowAdByVersion) {
//            return false
//        }

        if (timeStart == 0L || timeInterStartInterval == 0L || timeInterBetweenInterval == 0L || timeLastShowInter == 0L)
            return true

        val currentTime: Long = System.currentTimeMillis()
        val isStartIntervalCondition: Boolean =
            (currentTime - timeStart > timeInterStartInterval)
        if (!isStartIntervalCondition) {
            return false
        }

        //by time interval
        val isBetweenIntervalCondition: Boolean =
            (currentTime - timeLastShowInter > timeInterBetweenInterval)

        if (!isBetweenIntervalCondition) {
            return false
        }

        return true

    }

    fun canShowResume(): Boolean {
        if (isShowingInter) {
            return false
        }

//        if (isShowingRewarded) {
//            return false
//        }
//
//        if (isShowingResume) {
//            return false
//        }
//
//        if (!isShowAdByVersion) {
//            return false
//        }

        return true
    }

    fun canRequestResume(): Boolean {
        if (isShowingInter) {
            return false
        }

        return true
    }

    fun updateIsSimpleScript() {
        this.isSimpleAdScript = false
    }

    private fun updateIsShowAdByVersion(): Boolean {
        try {
            if (currentVersion.isEmpty())
                return true
            if (notShowAdVersion.isEmpty())
                return true
            if (currentVersion.replace(".", "") == notShowAdVersion.replace(".", ""))
                return false
            else
                return true
        } catch (e: Exception) {
            return true
        }

    }


}