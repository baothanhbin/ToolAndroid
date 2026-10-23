package com.b9ads.tracking.adjust

import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustEvent
import com.b9ads.admob.B9AdApplication
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo

object AdjustTracking {

    private const val TAG = "AdjustTracking"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private const val AD_REVENUE_ADMOB = "admob_sdk"


    fun trackRevenueNative(loadedAdapterResponseInfo: AdapterResponseInfo?, adValue: AdValue) {
        try {
            val adName = loadedAdapterResponseInfo?.adSourceName ?: ""
            val valueMicros = adValue.valueMicros / 1_000_000.0

            Log.d(
                TAG,
                "adName: $adName - valueMicros: $valueMicros - valueMicrosDefault: ${adValue.valueMicros}"
            )

            // Send ad revenue info to Adjust
            val adRevenue = AdjustAdRevenue(AD_REVENUE_ADMOB)
            adRevenue.setRevenue(valueMicros, adValue.currencyCode)
            adRevenue.adRevenueNetwork = adName
            Log.d(TAG, "trackRevenueNative: ${adValue.currencyCode}")
            Adjust.trackAdRevenue(adRevenue)

            // Lấy application instance
            val app = appContext as? B9AdApplication
            if (app == null) {
                Log.e(TAG, "appContext chưa được khởi tạo!")
                return
            }
            // Lấy token sự kiện Adjust
            val tokenEvent = app.getTokenEventAdjust()
            if (tokenEvent.isEmpty()) {
                Log.e("AdjustTracking", "Token event Adjust rỗng!")
                return
            }
            val adjustEvent = AdjustEvent(tokenEvent)
            adjustEvent.setRevenue(valueMicros, adValue.currencyCode)
            Adjust.trackEvent(adjustEvent)
            Log.d(TAG, "Token Event Adjust: $tokenEvent")

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi track revenue: ${e.localizedMessage}")
            e.printStackTrace()
        }

    }

    fun trackRevenueBanner(loadedAdapterResponseInfo: AdapterResponseInfo?, adValue: AdValue) {
        try {
            val adName = loadedAdapterResponseInfo?.adSourceName ?: ""
            val valueMicros = adValue.valueMicros / 1_000_000.0
            Log.d(
                TAG,
                "adName: $adName - valueMicros: $valueMicros - valueMicrosDefault: ${adValue.valueMicros}"
            )

            // Send ad revenue info to Adjust
            val adRevenue = AdjustAdRevenue(AD_REVENUE_ADMOB)
            adRevenue.setRevenue(valueMicros, adValue.currencyCode)
            adRevenue.adRevenueNetwork = adName
            Log.d(TAG, "trackRevenueBanner: ${adValue.currencyCode}")
            Adjust.trackAdRevenue(adRevenue)

            // Lấy application instance
            val app = appContext as? B9AdApplication
            if (app == null) {
                Log.e(TAG, "appContext chưa được khởi tạo!")
                return
            }
            // Lấy token sự kiện Adjust
            val tokenEvent = app.getTokenEventAdjust()
            if (tokenEvent.isEmpty()) {
                Log.e("AdjustTracking", "Token event Adjust rỗng!")
                return
            }
            val adjustEvent = AdjustEvent(tokenEvent)
            adjustEvent.setRevenue(valueMicros, adValue.currencyCode)
            Adjust.trackEvent(adjustEvent)
            Log.d(TAG, "Token Event Adjust: $tokenEvent")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackRevenueReward(loadedAdapterResponseInfo: AdapterResponseInfo?, adValue: AdValue) {
        try {
            val adName = loadedAdapterResponseInfo?.adSourceName ?: ""
            val valueMicros = adValue.valueMicros / 1_000_000.0
            Log.d(
                TAG,
                "adName: $adName - valueMicros: $valueMicros - valueMicrosDefault: ${adValue.valueMicros}"
            )

            // Send ad revenue info to Adjust
            val adRevenue = AdjustAdRevenue(AD_REVENUE_ADMOB)
            adRevenue.setRevenue(valueMicros, adValue.currencyCode)
            adRevenue.adRevenueNetwork = adName
            Log.d(TAG, "trackRevenueReward: ${adValue.currencyCode}")
            Adjust.trackAdRevenue(adRevenue)

            // Lấy application instance
            val app = appContext as? B9AdApplication
            if (app == null) {
                Log.e(TAG, "appContext chưa được khởi tạo!")
                return
            }
            // Lấy token sự kiện Adjust
            val tokenEvent = app.getTokenEventAdjust()
            if (tokenEvent.isEmpty()) {
                Log.e("AdjustTracking", "Token event Adjust rỗng!")
                return
            }
            val adjustEvent = AdjustEvent(tokenEvent)
            adjustEvent.setRevenue(valueMicros, adValue.currencyCode)
            Adjust.trackEvent(adjustEvent)
            Log.d(TAG, "Token Event Adjust: $tokenEvent")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackRevenueOpen(loadedAdapterResponseInfo: AdapterResponseInfo?, adValue: AdValue) {
        try {
            val adName = loadedAdapterResponseInfo?.adSourceName ?: ""
            val valueMicros = adValue.valueMicros / 1_000_000.0
            Log.d(
                TAG,
                "adName: $adName - valueMicros: $valueMicros - valueMicrosDefault: ${adValue.valueMicros}"
            )

            // Send ad revenue info to Adjust
            val adRevenue = AdjustAdRevenue(AD_REVENUE_ADMOB)
            adRevenue.setRevenue(valueMicros, adValue.currencyCode)
            adRevenue.adRevenueNetwork = adName
            Log.d(TAG, "trackRevenueOpen: ${adValue.currencyCode}")
            Adjust.trackAdRevenue(adRevenue)

            // Lấy application instance
            val app = appContext as? B9AdApplication
            if (app == null) {
                Log.e(TAG, "appContext chưa được khởi tạo!")
                return
            }
            // Lấy token sự kiện Adjust
            val tokenEvent = app.getTokenEventAdjust()
            if (tokenEvent.isEmpty()) {
                Log.e("AdjustTracking", "Token event Adjust rỗng!")
                return
            }
            val adjustEvent = AdjustEvent(tokenEvent)
            adjustEvent.setRevenue(valueMicros, adValue.currencyCode)
            Adjust.trackEvent(adjustEvent)
            Log.d(TAG, "Token Event Adjust: $tokenEvent")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackRevenueInter(loadedAdapterResponseInfo: AdapterResponseInfo?, adValue: AdValue) {
        try {
            val adName = loadedAdapterResponseInfo?.adSourceName ?: ""
            val valueMicros = adValue.valueMicros / 1_000_000.0
            Log.d(
                TAG,
                "adName: $adName - valueMicros: $valueMicros - valueMicrosDefault: ${adValue.valueMicros}"
            )

            // Send ad revenue info to Adjust
            val adRevenue = AdjustAdRevenue(AD_REVENUE_ADMOB)
            adRevenue.setRevenue(valueMicros, adValue.currencyCode)
            adRevenue.adRevenueNetwork = adName
            Log.d(TAG, "trackRevenueInter: ${adValue.currencyCode}")
            Adjust.trackAdRevenue(adRevenue)

            // Lấy application instance
            val app = appContext as? B9AdApplication
            if (app == null) {
                Log.e(TAG, "appContext chưa được khởi tạo!")
                return
            }
            // Lấy token sự kiện Adjust
            val tokenEvent = app.getTokenEventAdjust()
            if (tokenEvent.isEmpty()) {
                Log.e("AdjustTracking", "Token event Adjust rỗng!")
                return
            }
            val adjustEvent = AdjustEvent(tokenEvent)
            adjustEvent.setRevenue(valueMicros, adValue.currencyCode)
            Adjust.trackEvent(adjustEvent)
            Log.d(TAG, "Token Event Adjust: $tokenEvent")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}