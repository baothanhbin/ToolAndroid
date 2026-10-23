package com.b9default.a1_common_utils.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.View
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName
import com.b9default.ui.no_internet.NoInternetActivity

fun View.tap(action: (view: View?) -> Unit) {
    setOnClickListener(object : TapListener() {
        override fun onTap(v: View?) {
            if (!com.b9utils.util.CheckInternet.haveNetworkConnection(context)) {
                context.findActivity()?.let {
                    val intent = Intent(it, NoInternetActivity::class.java)
                    val screenName = makeActivityShortName(context.javaClass.simpleName)
                    com.b9utils.util.FirebaseLogEventUtils.logEventCommonWithName(
                        context,
                        "noNet_Fr_${screenName}"
                    )

                    it.startActivity(intent)
                    //confirm fix bug here
                    it.overridePendingTransition(0, 0)
                }
                return
            }
            action(v)
        }
    })
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}