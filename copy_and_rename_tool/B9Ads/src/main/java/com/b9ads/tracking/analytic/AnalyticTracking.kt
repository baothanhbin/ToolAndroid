package com.b9ads.tracking.analytic

import android.content.Context
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo

object AnalyticTracking {

    private val fanSourceIds = listOf("10568273599589928883", "11198165126854996598")

    fun trackingRevenue(
        context: Context,
        adName: String,
        adValue: AdValue,
    ) {
        val value = adValue.valueMicros / 1_000_000.0
        val currencyCode = adValue.currencyCode
        FirebaseLogEventUtils.logEventAdPaid(context, adName, value, currencyCode)
        FirebaseLogEventUtils.logEventAdPaidAll(context, value, currencyCode)
    }

    fun trackFanImpression(context: Context, adName: String, sourceId: String?) {
        if (sourceId in fanSourceIds) {
            val shortName = ShortNameEventUtils.makeKeyShortName(adName)
            FirebaseLogEventUtils.logEventAd(context, "ad_f_im_$shortName")
            FirebaseLogEventUtils.logEventAd(context, "ad_f_im_all")
        }
    }
}
