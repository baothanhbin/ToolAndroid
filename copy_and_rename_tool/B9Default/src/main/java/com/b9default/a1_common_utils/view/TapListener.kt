package com.b9default.a1_common_utils.view

import android.os.SystemClock
import android.view.View

abstract class TapListener : View.OnClickListener {

        private val DEBOUNCE = 600L

    private var lastClickMillis: Long = 0
    private var now = 0L

    override fun onClick(v: View?) {
        now = SystemClock.elapsedRealtime()
        if (now - lastClickMillis > DEBOUNCE)
            onTap(v)
        lastClickMillis = now
    }

    abstract fun onTap(v: View?)
}