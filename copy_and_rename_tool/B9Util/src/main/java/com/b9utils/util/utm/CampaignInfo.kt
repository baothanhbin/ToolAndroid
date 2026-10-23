package com.b9utils.util.utm

import android.app.Activity
import android.net.Uri
import android.os.RemoteException
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.b9utils.util.FirebaseLogEventUtils


object CampaignInfo {

    fun getCampaignInfo(activity: Activity, campaignInfoTrackingCallback: CampaignInfoTrackingCallback?){
        var campaignInfo = "no info"

        // creating an empty string for our referrer.
        var referrerUrl = ""

        // creating an empty string for our params.
        var params : Map<String, String> = mapOf()

        // variable for install referrer client.
        val referrerClient: InstallReferrerClient = InstallReferrerClient.newBuilder(activity).build();

        // on below line we are starting its connection.
        referrerClient.startConnection (object :InstallReferrerStateListener{
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        var response: ReferrerDetails? = null
                        try {
                            response = referrerClient.installReferrer
                            referrerUrl = response.installReferrer
                            val referrerClickTime = response.referrerClickTimestampSeconds
                            val appInstallTime = response.installBeginTimestampSeconds
                            val instantExperienceLaunched = response.googlePlayInstantParam
                            //add processing param in url

                            params = referrerUrl.split("&").associate {
                                val (key, value) = it.split("=")
                                key to value
                            }

                            campaignInfo = "referrerUrl is : $referrerUrl\n"
                            campaignInfo += "Referrer Click Time is : $referrerClickTime\n"
                            campaignInfo +="App Install Time : $appInstallTime\n"
                            campaignInfo +="instantExperienceLaunched: $instantExperienceLaunched\n"
                            for ((key, value) in params) {
                                campaignInfo +="$key: $value\n"
                            }

                        } catch (e: RemoteException) {
                            e.printStackTrace()
                            campaignInfo = "Exception: ${e.message}"
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        campaignInfo = "Feature not supported.."
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        campaignInfo = "Fail to establish connection"
                    }
                }

                //call calback
                campaignInfoTrackingCallback?.onInstallReferrerSetupFinished(campaignInfo)

                //end connection
                referrerClient.endConnection()

                //log firebase event
                com.b9utils.util.FirebaseLogEventUtils.logEventUACampaignInfo(activity, referrerUrl, params)
            }

            override fun onInstallReferrerServiceDisconnected() {
                campaignInfoTrackingCallback?.onInstallReferrerServiceDisconnected()
            }
        })

    }

}