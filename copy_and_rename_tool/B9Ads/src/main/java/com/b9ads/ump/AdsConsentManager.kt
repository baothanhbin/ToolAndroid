package com.b9ads.ump

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.b9utils.util.FirebaseLogEventUtils

class AdsConsentManager private constructor(context: Context) {

    private val tag = "AdsConsentManagerTag"
    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)

    //callback
    //for gatherConsent
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(result: Boolean)
    }

    /** Interface definition for a callback to be invoked when consent gathering is complete.
     * for gatherConsent2*/
    fun interface OnConsentGatheringCompleteListener2 {
        fun consentGatheringComplete2(error: FormError?)
    }


    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * ANH ad new for consent form result
     * canRequestAd always return true
     */

    private fun getConsentResult(context: Context): Boolean {
        val consentResult = context.getSharedPreferences(context.packageName + "_preferences", 0)
            .getString("IABTCF_PurposeConsents", "")
        return consentResult.isNullOrBlank() || consentResult[0].toString() == "1"
    }

    /**
     * ANH gatherConsent
     * using for both test device and real device
     */
    fun gatherConsent(
        activity: Activity,
        //for debug deviceID
        deviceHashId: String,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {

        FirebaseLogEventUtils.logEventAdConsent(
            activity,
            "ump_gather_consent"
        )

        var params: ConsentRequestParameters? = null

        if (deviceHashId.isNotEmpty()) {
            val debugSettings =
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId(deviceHashId)
                    .build()
            params =
                ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

        } else {
            params = ConsentRequestParameters.Builder().build()
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                val statusBeforeConsent = consentInformation.consentStatus
                Log.v(tag, "statusBeforeConsent: $statusBeforeConsent")

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {

                        //form error also call consentCallback
                        onConsentGatheringCompleteListener.consentGatheringComplete(false)

                        Log.v(tag, "formError: ${formError.errorCode} ${formError.message}")

                        //ump log
                        FirebaseLogEventUtils.logEventAdConsent(
                            activity,
                            "ump_form_err_" + formError.errorCode.toString()
                        )

                    } else {

                        val consentFormResult = getConsentResult(activity)
                        onConsentGatheringCompleteListener.consentGatheringComplete(
                            consentFormResult
                        )

                        //check case
                        val statusAfterConsent = consentInformation.consentStatus
                        Log.v(tag, "statusAfterConsent: $statusAfterConsent")

                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED)
                        ) {
                            Log.v(
                                tag,
                                "user not in EU, consent not require, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(
                                activity,
                                "ump_not_required"
                            )

                        } else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (consentFormResult)
                        ) {
                            Log.v(
                                tag,
                                "user obtained OK consent, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_obtained_OK")

                        } else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (!consentFormResult)
                        ) {
                            Log.v(
                                tag,
                                "user obtained NG consent, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_obtained_NG")

                        } else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (consentFormResult)
                        ) {
                            Log.v(
                                tag,
                                "user new accept consent, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_new_OK")

                        } else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (!consentFormResult)
                        ) {
                            Log.v(
                                tag,
                                "user new refuse consent, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_new_NG")

                        } else {
                            Log.v(
                                tag,
                                "user consent other case, consentFormResult = $consentFormResult"
                            )

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_other_case")

                        }
                    }
                }
            },
            { requestConsentError ->

                //request update error also call consentCallback
                onConsentGatheringCompleteListener.consentGatheringComplete(false)

                Log.v(
                    tag,
                    "requestConsentError: ${requestConsentError.errorCode} ${requestConsentError.message}"
                )

                //ump log
                FirebaseLogEventUtils.logEventAdConsent(
                    activity,
                    "ump_request_err_" + requestConsentError.errorCode.toString()
                )
            },
        )

    }

    fun gatherConsent2(
        activity: Activity,
        deviceHashId: String,
        onConsentGatheringCompleteListener2: OnConsentGatheringCompleteListener2,
    ) {
        try {
            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_MngC_gt2Begin")

            var params: Any? = null
            if (deviceHashId.isNotEmpty()) {
                // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
                val debugSettings =
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .addTestDeviceHashedId(deviceHashId)
                        .build()

                params =
                    ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings)
                        .build()
            } else {
                params = ConsentRequestParameters.Builder().build()
            }


            // Requesting an update to consent information should be called on every app launch.
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    FirebaseLogEventUtils.logEventAdConsent(activity, "ump_MngC_gt2UdSuc")
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->

                        //log event begin function
                        FirebaseLogEventUtils.logEventAdConsent(activity, "ump_MngC_gt2Form")
                        // Consent has been gathered.
                        if (formError != null) {
                            Log.v(
                                tag,
                                "loadAndShowConsentFormIfRequired fail, formError: ${formError.errorCode}: ${formError.message}, canRequest: $canRequestAds"
                            )
                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(
                                activity,
                                "ump_MngC_gt2Form_err_" + formError.errorCode.toString()
                            )

                        } else {
                            Log.v(
                                tag,
                                "loadAndShowConsentFormIfRequired success, canRequest: $canRequestAds"
                            )
                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(
                                activity,
                                "ump_MngC_gt2form_success"
                            )
                        }
                        onConsentGatheringCompleteListener2.consentGatheringComplete2(formError)
                    }
                },
                { requestConsentError ->

                    Log.v(
                        tag,
                        "requestConsentError ${requestConsentError.errorCode}: ${requestConsentError.message}, canRequest: $canRequestAds"
                    )
                    //ump log
                    FirebaseLogEventUtils.logEventAdConsent(
                        activity,
                        "ump_MngC_gt2UdFail_err_" + requestConsentError.errorCode.toString()
                    )
                    onConsentGatheringCompleteListener2.consentGatheringComplete2(
                        requestConsentError
                    )
                },
            )

        } catch (e: Exception) {
            FirebaseLogEventUtils.logEventAdConsent(activity, "ump_MngC_gt2Exception")
            onConsentGatheringCompleteListener2.consentGatheringComplete2(null)
        }

    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener,
    ) {
        // [START present_privacy_options_form]
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
        // [END present_privacy_options_form]
    }


    companion object {
        @Volatile
        private var instance: AdsConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: AdsConsentManager(context).also { instance = it }
                }
    }
}
