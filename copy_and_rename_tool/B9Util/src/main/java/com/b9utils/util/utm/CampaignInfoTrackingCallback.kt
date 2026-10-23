package com.b9utils.util.utm

interface CampaignInfoTrackingCallback {

    //ad listener callback
    fun onInstallReferrerSetupFinished(info:String) {}
    fun onInstallReferrerServiceDisconnected() {}

}
