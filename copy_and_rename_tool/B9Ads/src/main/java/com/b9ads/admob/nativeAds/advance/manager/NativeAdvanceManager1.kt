/**
 * native manager in reload by timer class
 * pending ANH CUONG spec (priority)
 *  khi gap video huy timer va setting auto reload khi het video
 *  khi khong phai video thi setting timer theo trang thai enable/disable va thoi gian interval trong firebase
 *  khi activity pause, stop -> huy handle timer
 */

package com.b9ads.admob.nativeAds.advance.manager

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.b9utils.util.FirebaseLogEventUtils

open class NativeAdvanceManager1(
    // id quảng cáo
    adID: String,
    //ad name
    adName: String,
    isReloadWhenResume: Boolean = false,
    isReloadWhenVideoEnd: Boolean = false,
    reloadTimeWhenNotVideo: Long = 0,
    maxPreload: Int = 0,
    // lifecycle dùng để reload quảng cáo
    lifecycle: Lifecycle? = null

) : NativeAdvanceManager0(
    adID,
    adName,
    isReloadWhenResume,
    isReloadWhenVideoEnd,
    reloadTimeWhenNotVideo,
    maxPreload,
    lifecycle
) {

    // 12/11/2025 Cuong fix #146
    init {
       lifecycle?.addObserver(object : LifecycleEventObserver {
           override fun onStateChanged(
               source: LifecycleOwner,
               event: Lifecycle.Event,
           ) {
               screenState = event
               when(event) {
                   Lifecycle.Event.ON_START -> {
                       if (adAlreadyShow) {
                           setTimerHandleReloadAd()
                       }
                   }

                   Lifecycle.Event.ON_STOP -> {
                       handler.removeCallbacksAndMessages(null)
                   }

                   Lifecycle.Event.ON_DESTROY -> {
                       source.lifecycle.removeObserver(this)
                   }

                   else -> {}
               }
           }

       })
    }

    private var screenState = Lifecycle.Event.ON_DESTROY

    //remote config - is_auto_reload_timer
    private var isAutoReloadByTimer = true

    //remote config reload time interval - ad_reload_timer_interval
    //millisecond
    private var timerReloadInterval = 0L

    // Handler phục vụ reload quảng cáo by time
    private val handler = Handler(Looper.getMainLooper())

    fun setReloadTimerInfo(isAuto: Boolean, timeInterval: Long) {
        isAutoReloadByTimer = isAuto

        //time interval in millisecond
        //config in firebase is second
        timerReloadInterval = timeInterval * 1000
    }

    // Đặt thời gian để reload quảng cáo
    private fun setTimerHandleReloadAd() {
        handler.removeCallbacksAndMessages(null)
        if ((timerReloadInterval > 0) && isAutoReloadByTimer) {
            handler.postDelayed({
                reloadNative()
            }, timerReloadInterval)
        }
    }

    // 12/11/2025 Cuong fix #146
    //when show ad check
    //neu video khong show thi reload by time
    override fun onCheckNativeNotMediaVideo() {
        super.onCheckNativeNotMediaVideo()
        if (screenState == Lifecycle.Event.ON_RESUME) {
            setTimerHandleReloadAd()
        }
    }


    // 12/11/2025 Cuong fix #146
    private fun reloadNative() {
        if (screenState == Lifecycle.Event.ON_RESUME) {
            refreshAdNative()
            //log event
            val context = nativeAdView?.context
            FirebaseLogEventUtils.logEventAdCommon(context, adName, "Mng1", "RldByTime")
        }
    }

}
