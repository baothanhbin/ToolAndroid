/**
 * for logevent
 * neu can them thi ke thua class nay
 */

package com.b9utils.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;


public class FirebaseLogEventUtils {
    private static final String TAG = "FirebaseLogEventUtils";
    private static final String PREFIX = "v1e_";


    //remote config load data null event
    public static void logEventConfigModelNull(Context context, String screenName, String obj_name) {
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "fbNu_" + screenName + "_" + obj_name, bundle);
    }

    //remote config load data ok event
    public static void logEventConfigModelNotNull(Context context, String screenName, String obj_name) {
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "fbOK_" + screenName + "_" + obj_name, bundle);
    }

    //all ad
    public static void logEventAdCommon(Context context, String event_name, String screen_name, String info) {
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + event_name + "_" + screen_name + "_" + info, bundle);
    }

    public static void logEventAd(Context context, String eventName) {
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + eventName, null);
    }

    public static void logEventAdPaid(Context context, String adName, Double value, String currency) {
        Bundle bundle = new Bundle();
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value);
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        String shortAdName = ShortNameEventUtils.INSTANCE.makeKeyShortName(adName);
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + shortAdName + "_paid", bundle);
    }

    public static void logEventAdPaidAll(Context context, Double value, String currency) {
        Bundle bundle = new Bundle();
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value);
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_paid", bundle);
    }

//    //all ad
//    //ad fail to load
//    public static void logEventAdFailedToLoad(Context context, String ad_name, String ad_id, String error) {
//        Bundle bundle = new Bundle();
////        bundle.putString("v1_ad_name", ad_name);
////        bundle.putString("v1_ad_id", ad_id);
////        bundle.putString("v1_ad_loaded_error", error);
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_ld_err_" + ad_name, bundle);
//    }
//
//    //resume + inter only
//    //onAdFailedToShowFullScreenContent
//    public static void logEventAdFailedToShow(Context context, String ad_name, String ad_id, String error) {
//        Bundle bundle = new Bundle();
////        bundle.putString("v1_ad_name", ad_name);
////        bundle.putString("v1_ad_id", ad_id);
////        bundle.putString("v1_ad_loaded_error", error);
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_sh_err_" + ad_name, bundle);
//    }
//
//
//    //all click event
//    //for all ad
//    public static void logEventAdClicked(Context context, String ad_name, String ad_id) {
//        Bundle bundle = new Bundle();
//        String ad_name_short = ad_name.replace("Activity", "");
////        bundle.putString("v1_ad_name", ad_name);
////        bundle.putString("v1_ad_id", ad_id);
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_clk_" + ad_name_short, bundle);
//    }
//
//    //all ad
//    public static void logEventAdImpression(Context context, String ad_name, String ad_id) {
//        Bundle bundle = new Bundle();
//        String ad_name_short = ad_name.replace("Activity", "");
////        bundle.putString("v1_ad_name", ad_name);
////        bundle.putString("v1_ad_id", ad_id);
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_imp_" + ad_name_short, bundle);
//    }
//
//    //all ad
//    public static void logEventAdPaidRevenue(Context context, String ad_name, String ad_id, AdValue adValue) {
//        Bundle bundle = new Bundle();
//        bundle.putString("v1_ad_name", ad_name);
//        bundle.putString("v1_ad_id", ad_id);
//        bundle.putDouble("v1_ad_value", adValue.getValueMicros() / 1000000.0);
//        bundle.putString("v1_ad_currency", "USD");
//        bundle.putInt("v1_ad_precision", adValue.getPrecisionType());
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_paid", bundle);
//    }
//
//    public static void logEventAdPaidRevenueDetail(Context context, String adType, Bundle bundle, String ad_name, String ad_id, AdValue adValue) {
//        if (bundle == null)
//            return;
//        bundle.putString("v1_ad_name", ad_name);
//        bundle.putString("v1_ad_id", ad_id);
//        bundle.putDouble("v1_ad_value", adValue.getValueMicros() / 1000000.0);
//        bundle.putInt("v1_ad_precision", adValue.getPrecisionType());
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_paid_" + adType, bundle);
//    }

    //only native
    //ad_source goto 1 event name
    public static void logEventAdMediationWin(Context context,
                                              String ad_name,
                                              String ad_id,
                                              Bundle bundle) {
        if (bundle == null) {
            return;
        }
        //cut adname because too long
        ad_name = ad_name.replace("native", "");
        ad_name = ad_name.replace("Activity", "");

        bundle.putString("v1_ad_name", ad_name);
        bundle.putString("v1_ad_id", ad_id);
        String strPostEvent = bundle.getString("v1_ad_source_id");
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ad_med_win_" + strPostEvent, bundle);
    }


//    //only native
//    public static void logEventAdNativeChangeAdCtr(Context context, String ad_name, String info) {
//        Bundle bundle = new Bundle();
////        bundle.putString("v1_ad_name", ad_name);
////        bundle.putString("v1_ad_id", ad_id);
//        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + ad_name + "_" + info, bundle);
//    }

    //event track consent status
    //status: consent_ok_before, consent_ok, consent_cancel
    //tracking in splash screen
    public static void logEventAdConsent(Context context, String consentStatus) {
        Bundle bundle = new Bundle();
        bundle.putString("v1_logged_at_activity", context.getClass().getSimpleName());
        bundle.putString("v1_consent_status", consentStatus);
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + consentStatus, bundle);
    }


    //event track campaign
    //tracking in splash screen
    public static void logEventUACampaignInfo(Context context, String referrerUrl, Map<String, String> params) {
        Bundle bundle = new Bundle();
        bundle.putString("v1_logged_at_activity", context.getClass().getSimpleName());
        bundle.putString("v1_referrer_url", referrerUrl);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = "v1_" + entry.getKey();
            String value = entry.getValue();
            bundle.putString(key, value);
        }
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "ua_campaign_info", bundle);
    }


    //event for user
    public static void logEventScreenView(Context context, String log_at) {
        String screenName = context.getClass().getSimpleName();
        screenName = screenName.replace("Activity", "");
        //String screenName = "screenName";
        Bundle bundle = new Bundle();
        bundle.putString("v1_screen_name", screenName);
        bundle.putString("v1_logged_at", log_at);
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "scr_" + screenName, bundle);
    }

    public static void logEventScreen(Context context, String event) {
        String screenName = context.getClass().getSimpleName();
        String screenShortName = ShortNameEventUtils.INSTANCE.makeActivityShortName(screenName);
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + "scr_" + screenShortName + "_" + event, bundle);
    }

    //log event common
    public static void logEventCommonWithName(Context context, String event_name, Bundle bundle) {
        if (bundle == null) {
            return;
        }
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + event_name, bundle);
    }

    public static void logEventCommonWithName(Context context, String event_name) {
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent(PREFIX + event_name, bundle);
    }


    //   public static void logTotalRevenue001Ad(Context context) {
//        float revenue = AppUtil.currentTotalRevenue001Ad;
//        if (revenue / 1000000 >= 0.01) {
//            AppUtil.currentTotalRevenue001Ad = 0;
//            SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, 0);
//            Bundle bundle = new Bundle();
//            bundle.putFloat(FirebaseAnalytics.Param.VALUE, revenue / 1000000);
//            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
//            FirebaseAnalyticsUtil.logTotalRevenue001Ad(context, bundle);
//            FacebookEventUtils.logTotalRevenue001Ad(context, bundle);
//        }
//    }
}