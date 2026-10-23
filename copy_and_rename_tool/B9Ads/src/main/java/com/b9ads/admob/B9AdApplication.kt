//update: ANH
//version: 20250305

package com.b9ads.admob

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.b9ads.admob.resumeAds.callback.OpenCallback
import com.b9ads.tracking.adjust.AdjustTracking

abstract class B9AdApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    private val tag = "B9AdApplicationTag"

    var currentActivity: Activity? = null

    open fun getCallback(): OpenCallback {
        return object : OpenCallback {
            override fun onNextAction() {
                //Toast.makeText(currentActivity, "on next action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    open fun showWelcombackScreen() {
        return
    }


    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        //facebook sdk init here
        //adjust init here
        setUpAdjust()

        // init adjust tracking
        AdjustTracking.init(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.v(tag, "start ads $currentActivity")
        currentActivity?.let {
            //AppOpenResumeManager.showAdResume(it, getCallback())
            showWelcombackScreen()
        }

    }

//    override fun onResume(owner: LifecycleOwner) {
//        super.onResume(owner)
//    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }


    abstract fun getAppTokenAdjust(): String

    abstract fun getFacebookId(): String

    abstract fun getTokenEventAdjust() : String

    //Setup gắn adjust
    private fun setUpAdjust() {
        val appToken = getAppTokenAdjust()
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this@B9AdApplication, appToken, environment)
        config.setLogLevel(LogLevel.DEBUG)
        config.defaultTracker = getAppTokenAdjust()
        Adjust.enable()
        Adjust.initSdk(config)
    }
    fun exampleFunction() {
        val tokenEvent = getTokenEventAdjust()
        Log.d(tag, "Token Event Adjust: $tokenEvent")
    }

}
