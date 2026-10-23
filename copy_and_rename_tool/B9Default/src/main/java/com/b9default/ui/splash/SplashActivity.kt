package com.b9default.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.bannerAds.collapsible.BannerGravity
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.preload.InterPreload
import com.b9ads.admob.interstitialAds.simple.SimpleInter0
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9ads.admob.resumeAds.callback.OpenCallback
import com.b9ads.ump.GoogleMobileAdsConsentManager
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.base.model.AdType
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.LoadLocation
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdAppopenResumeModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenLanguageStartModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenSplashModel
import com.b9default.a1_common_utils.model_remote_config.screens.ScreenVersion
import com.b9default.a8_app_utils.Constants
import com.b9default.a8_app_utils.ScreenNavigate
import com.b9default.ui.intro1.ads.AdsIntroManager
import com.b9default.ui.language.LanguageStartActivity
import com.b9default.ui.no_internet.NoInternetActivity
import com.b9ui.databinding.ActivitySplashBinding
import com.b9utils.util.CheckInternet
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.remoteconfig.FetchRemoteConfigCallback
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.tasks.Task

class SplashActivity : AppCompatActivity() {

    private val tag = "B9Default_SplashActivityTag"
    private val adSplashOpenEventName = "adOpSpl"
    private val adSplashInScreenEventName = "adInScr"
    private lateinit var binding: ActivitySplashBinding

    //screen splash model
    private var remoteConfigScreenSplashModel: RemoteConfigScreenSplashModel? = null

    //ad splash model
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null

    //private var remoteConfigAdSplashBanner: RemoteConfigAdBannerModel? = null
    private var remoteConfigAdSplashNative: RemoteConfigAdNativeModel? = null
    private var remoteConfigAdSplashInter: RemoteConfigAdInterModel? = null


    //consent
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private var simpleNativeAdvanceManagerSplashVer2: SimpleNativeAdvanceManager0? = null
    private var simpleInterSplashVer2: SimpleInter0? = null


    //callback parameter for init ad
    private var isFinishConsent = false
    private var isFinishGetRemoteConfig = false
    private var isInitAdByTimeout = false

    //ad in screen (native or banner)
    private var isShowInScreenAd: Boolean = false

    //handle timeout for UMP and remote config
    //after x(ms) if not done get remote config or ump will auto call init ad
    private var handlerTimeOutForUmpNRemoteConfig: Handler? = null
    private val timeoutForUmpNRemoteConfig = 15000L

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val listText = listOf(
        com.b9ui.R.string.splash_text_1,
        com.b9ui.R.string.splash_text_2,
        com.b9ui.R.string.splash_text_3,
        com.b9ui.R.string.splash_text_4,
        com.b9ui.R.string.splash_text_5,
        com.b9ui.R.string.splash_text_6,
        com.b9ui.R.string.splash_text_7,
    )

    // screen ver2: Kiểm tra app có đang dưới background
    private var isPause = false

    // screen ver2: Kiểm tra có inter đang chờ show
    private var waitForShowInter = false

    // screen ver2: Khóa để không gọi initAds nhiều lần
    private val lock = Any()

    // screen ver2: Kiểm tra đã khởi tạo ad chưa
    private var isInitialized = false
    private var isMobileAdsInitialized = false
    private var isShowNativeAndInter = false
    private var isInterLoaded = false
    private var isInterFailToLoad = false
    private var handlerShowInter: Handler? = null
    private var handlerTimeoutShowInter: Handler? = null
    private var handlerTimeoutNextScreen: Handler? = null
    private var isNativeDone = false
    private var needShowInter = false
    private var showInterCalled = false
    private var countInitAd = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.v(tag, "function onCreate SplashActivity")
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onCreate")
        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_BeginF")

        //VIET ANH fix show logo on splash
        installSplashScreen()
        super.onCreate(savedInstanceState)

        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_Binding")

        //binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //log event
        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_ChkNet")

        //check internet
        if (!CheckInternet.haveNetworkConnection(this@SplashActivity)) {
            FirebaseLogEventUtils.logEventCommonWithName(this, "noNet_FrOnCrSpl")
            FirebaseLogEventUtils.logEventCommonWithName(this, "splash_noInternet")
            startActivity(Intent(this, NoInternetActivity::class.java))
            finish()
            return
        }
        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_ChkNetOK")

        //log campaign info
        //CampaignInfo.getCampaignInfo(this, null)

        //get firebase remote
        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_Rmcf")
        //getRemoteConfig()
        //getRemoteConfigOrigin()
        getRemoteConfig2()


        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_Consent")
        //display consent
        //processConsent()
        processConsent2()

        //log event
        FirebaseLogEventUtils.logEventCommonWithName(this, "onCrSpl_EndF")

        //create handle time for ump and remote config
        initTimeOutForUmpNRemoteConfig()

    }

    override fun onResume() {

        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onRs")
        super.onResume()
        isPause = false
        if (waitForShowInter && remoteConfigScreenSplashModel?.screenVersion == ScreenVersion.VER_2 && needShowInter) {
            waitForShowInter = false
            showInterSplash()
        }
        //not show resume ads in splash because policy
        //AppOpenResumeManager.setEnableAdsResume(false)
    }

    override fun onStop() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        handlerShowInter?.removeCallbacksAndMessages(null)
        handlerTimeoutShowInter?.removeCallbacksAndMessages(null)
        handlerTimeoutNextScreen?.removeCallbacksAndMessages(null)
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onDes")
        super.onDestroy()

    }

    private fun loopText() {
        handler.postDelayed(object : Runnable {
            var currentPosition = 0
            override fun run() {
                currentPosition = (currentPosition + 1) % listText.size
                binding.tvBottom.text = this@SplashActivity.getString(listText[currentPosition])
                handler.postDelayed(this, 1500)
            }
        }, 1500)
    }

    override fun onPause() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onPause")
        super.onPause()
        isPause = true

    }

    private fun initTimeOutForUmpNRemoteConfig() {

        val funNameLogEvent = "SplHdlTiO"
        Log.v(tag, "function initTimeOutForUmpNRemoteConfig")
        handlerTimeOutForUmpNRemoteConfig = Handler(Looper.getMainLooper())
        FirebaseLogEventUtils.logEventCommonWithName(this, "${funNameLogEvent}_Wait")
        handlerTimeOutForUmpNRemoteConfig?.postDelayed({

            FirebaseLogEventUtils.logEventCommonWithName(this, "${funNameLogEvent}_Do")
            FirebaseLogEventUtils.logEventCommonWithName(
                this,
                "${funNameLogEvent}_Ump${isFinishConsent}_Rmcf${isFinishGetRemoteConfig}"
            )
            //check processed init ads
            if (isFinishConsent && isFinishGetRemoteConfig) {
                FirebaseLogEventUtils.logEventCommonWithName(
                    this,
                    "${funNameLogEvent}_InitDoneNormal"
                )

            } else {
                isFinishConsent = true
                isFinishGetRemoteConfig = true
                isInitAdByTimeout = true
                FirebaseLogEventUtils.logEventCommonWithName(this, "${funNameLogEvent}_ClInitAdTiO")
                callInitAds("timeout")
            }
        }, timeoutForUmpNRemoteConfig)
        FirebaseLogEventUtils.logEventCommonWithName(this, "${funNameLogEvent}_Done")
    }

    private fun processConsent2() {
        Log.v(tag, "function processConsent2")
        try {
            // Required ump
            FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsentBg")
            googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(applicationContext)

            googleMobileAdsConsentManager.gatherConsent(
                this,
                Constants.UMP_TEST_DEVICE_ID
            ) { error ->
                Log.v(tag, "callback consent return")
                FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsent_callback")
                //log can request
                val canRequestAds: Boolean =
                    GoogleMobileAdsConsentManager.getInstance(this).canRequestAds
                FirebaseLogEventUtils.logEventAdConsent(
                    this,
                    "ump_SplConsent_canReq${canRequestAds}"
                )

                //log error form
                if (error != null) {
                    FirebaseLogEventUtils.logEventAdConsent(
                        this,
                        "ump_SplConsent_err${error.errorCode}"
                    )
                }

                //check policy
                if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                    // Regenerate the options menu to include a privacy setting.
                    FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsent_PrivacyR")

                }

                //call init ad

                isFinishConsent = true
                if (isFinishGetRemoteConfig && !isInitAdByTimeout) {
                    handlerTimeOutForUmpNRemoteConfig?.removeCallbacksAndMessages(null)
                    callInitAds("consent")
                    FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsent_ClInitAd")
                }
            }
            FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsentEn")
            Log.v(tag, "...consent finish")

        } catch (_: Exception) {
            Log.v(tag, "...exception in consent")
            FirebaseLogEventUtils.logEventAdConsent(this, "ump_SplConsent_exception")
        }
    }

    private fun getRemoteConfig2() {
        Log.v(tag, "function getRemoteConfig2")
        FirebaseLogEventUtils.logEventCommonWithName(this, "SplRmcf_BeginF")
        val fetchRemoteConfigCallback: FetchRemoteConfigCallback =
            object : FetchRemoteConfigCallback {
                override fun onCompleteListener(task: Task<Boolean>) {
                    Log.v(tag, "callback...remote config callback onCompleteListener")
                    FirebaseLogEventUtils.logEventCommonWithName(
                        this@SplashActivity,
                        "SplRmcf_FetComp"
                    )
                    getRemoteConfigData()
                    isShowNativeAndInter =
                        remoteConfigScreenSplashModel?.inscreenAdType == "native_advance" &&
                                remoteConfigScreenSplashModel?.fullScreenAdType == AdType.INTER
                    isFinishGetRemoteConfig = true
                    if (isFinishConsent && !isInitAdByTimeout) {
                        FirebaseLogEventUtils.logEventCommonWithName(
                            this@SplashActivity,
                            "SplRmcf_FetComp_ClAdInit"
                        )
                        handlerTimeOutForUmpNRemoteConfig?.removeCallbacksAndMessages(null)
                        callInitAds("fetComp")
                    }
                }

                override fun onFailureListener(exception: java.lang.Exception) {
                    Log.v(tag, "callback...remote config callback onFailureListener")
                    FirebaseLogEventUtils.logEventCommonWithName(
                        this@SplashActivity,
                        "SplRmcf_FetFail"
                    )
                }

                override fun onSuccessListener() {
                    Log.v(tag, "callback...remote config callback onSuccessListener")
                    FirebaseLogEventUtils.logEventCommonWithName(
                        this@SplashActivity,
                        "SplRmcf_FetSuccess"
                    )

                }

                override fun onCanceledListener() {
                    Log.v(tag, "callback...remote config callback onCanceledListener")
                    FirebaseLogEventUtils.logEventCommonWithName(
                        this@SplashActivity,
                        "SplRmcf_Cancel"
                    )
                    isFinishGetRemoteConfig = true
                    if (isFinishConsent && !isInitAdByTimeout) {
                        FirebaseLogEventUtils.logEventCommonWithName(
                            this@SplashActivity,
                            "SplRmcf_Cancel_ClInitAd"
                        )
                        handlerTimeOutForUmpNRemoteConfig?.removeCallbacksAndMessages(null)
                    }
                }
            }

        FirebaseLogEventUtils.logEventCommonWithName(this@SplashActivity, "SplRmcf_ClLibFn")

        RemoteConfigOrigin.initRemoteConfig3(
            this,
            fetchRemoteConfigCallback
        )
        FirebaseLogEventUtils.logEventCommonWithName(this, "SplRmcf_EndF")
    }

    private fun getRemoteConfigData() {
        RemoteConfigManager.reloadData(this)

        remoteConfigAppAllModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAppConfig(),
            RemoteConfigAppAllModel::class.java
        )

        remoteConfigScreenSplashModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenSplash(),
            RemoteConfigScreenSplashModel::class.java
        )

        remoteConfigAdSplashNative = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeSplash(),
            RemoteConfigAdNativeModel::class.java
        )

        remoteConfigAdSplashInter = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdInterSplash(),
            RemoteConfigAdInterModel::class.java
        )
    }

    private fun callInitAds(from: String) {
        if (remoteConfigScreenSplashModel?.screenVersion == ScreenVersion.VER_2) {
            synchronized(lock) {
                if (isMobileAdsInitialized) {
                    return
                }
                isMobileAdsInitialized = true
                MobileAds.initialize(this) {
                    initAds(from)
                }
            }
        } else {
            MobileAds.initialize(this) {
                initAds(from)
            }
        }
    }

    private fun initAds(from: String) {
        if (isInitialized && remoteConfigScreenSplashModel?.screenVersion == ScreenVersion.VER_2) {
            return
        }
        Log.v(tag, "function initAds")

        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Begin")
        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Fr_${from}")
        Log.v(tag, "...initAds function begin, from $from")

        if (!isFinishGetRemoteConfig) {
            FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_NGR_NotDoneRmcf")
            Log.v(tag, "...initAds initAd_NG_NotDoneRmcf")
            return
        }

        if (!isFinishConsent) {
            FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_NGR_NotDoneConsent")
            Log.v(tag, "...initAds initAd_NG_NotDoneConsent")
            return
        }

        countInitAd++
        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_CheckOK")
        Log.v(tag, "...initAds initAd_Pass1")
        //disable resume ads on splash screen
        //AppOpenResumeManager.setEnableAdsResume(false)

        //splash screen config

        //FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Pass2")

        // update version
        //setUpdateVersion()
        //FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Pass3")

        //init inter ad
        //initInterAdVer1()

        //need done UMP and get firebase remote config
        //InterManager.init(System.currentTimeMillis())
        //FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Pass5")

        AdsHelper.setVersionData(
            allow = remoteConfigAppAllModel?.allowShowAd,
            notShow = remoteConfigAppAllModel?.notShowAd
        )
        AdsHelper.calculateShowAds(this)

        // Setting change text
        if (remoteConfigScreenSplashModel?.isUseTextAnimate == true) {
            loopText()
        }
        //initInterSplashByInterVer2()

        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_ClInScrAd")
        loadAndShowInScreenAd()

        //init open resume ad
        //initOpenResumeAd()
        //FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_Pass7")


        //load and show splash
        //delay if show banner splash in splash screen
        Log.v(tag, "...in-screen ad is show: $isShowInScreenAd")
        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_ClAdOp")
        if (isShowInScreenAd) {
            FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_SplVsBnNtWa")
            val delayTimeMiniSecond =
                (remoteConfigScreenSplashModel?.inscreenAdShowDelayTime ?: 0L) * 1000
            Handler(Looper.getMainLooper()).postDelayed({
                FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_SplVsBnNtDo")
                loadFullScreenAd()
            }, delayTimeMiniSecond)

        } else {
            FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_SplNoBnNt")
            loadFullScreenAd()
        }
        loadAdsIntro()
        FirebaseLogEventUtils.logEventCommonWithName(this, "initAd_End${countInitAd}")
        isInitialized = true

    }

    private fun loadAndShowInScreenAd() {
        val funNameLogEvent = "LdNShInScrAd"
        Log.v(tag, "function loadAndShowInScreenAd")
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashInScreenEventName,
            funNameLogEvent, "BeginF"
        )
        //if onscreen ad is banner
        if ((remoteConfigScreenSplashModel?.inscreenAdType?.lowercase() == "banner_adaptive") &&
            ((remoteConfigScreenSplashModel?.inscreenAdShowDelayTime ?: 0L) > 0)
        ) {
            Log.v(tag, "...show ad in screen: banner_adaptive")
            isShowInScreenAd = true
            loadAndShowBannerAdaptive()
            FirebaseLogEventUtils.logEventAdCommon(
                this, adSplashInScreenEventName,
                funNameLogEvent, "Banner"
            )
            return


        } else if ((remoteConfigScreenSplashModel?.inscreenAdType?.lowercase() == "native_advance") &&
            (remoteConfigAdSplashNative?.adVer == "ver2")
        ) {
            Log.v(tag, "...show ad in screen: native_advance ver2")
            isShowInScreenAd = true
            loadAndShowNativeVer2()
            FirebaseLogEventUtils.logEventAdCommon(
                this, adSplashInScreenEventName,
                funNameLogEvent, "NativeV2"
            )
            return

        } else if (remoteConfigScreenSplashModel?.inscreenAdType?.lowercase() == "native_advance") {
            Log.v(tag, "...show ad in screen: native_advance ver1")
            isShowInScreenAd = true
            loadAndShowNativeVer1()
            FirebaseLogEventUtils.logEventAdCommon(
                this, adSplashInScreenEventName,
                funNameLogEvent, "NativeV1"
            )
            return

        } else {
            Log.v(tag, "...show ad in screen: no ad")
            isShowInScreenAd = false
            FirebaseLogEventUtils.logEventAdCommon(
                this, adSplashInScreenEventName,
                funNameLogEvent, "NoAd"
            )
        }
    }

    // 27/10/2025 Cuong fix #147
    private fun loadFullScreenAd() {
        val funNameLogEvent = "LdNShAdOp"
        Log.v(tag, "function loadFullScreenAd")
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            funNameLogEvent, "BeginF${countInitAd}"
        )
        if (remoteConfigScreenSplashModel?.fullScreenAdType == AdType.OPEN) {
            loadAndShowOpen()
        } else {
            loadInterSplash()
        }
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            funNameLogEvent, "Endf"
        )
    }

    // 27/10/2025 Cuong fix #147
    private fun loadAndShowOpen() {
        val openAd = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyOpenSplash(),
            RemoteConfigAdAppopenResumeModel::class.java
        )
        val resumeModel = openAd?.toResumeModel()
        if (resumeModel == null) {
            Log.v(tag, "resumeModel is null")
            nextActivity()
            return
        }
        AppOpenResumeManager.setProperties(resumeModel)
        val callback = object : OpenCallback {
            private var handled: Boolean = false

            override fun onCancelLoad() {
                super.onCancelLoad()
                if (handled.not()) {
                    handled = true
                    nextActivity()
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                if (handled.not()) {
                    handled = true
                    showOpen()
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError?, adError: AdError?) {
                super.onAdFailedToLoad(loadAdError, adError)
                if (handled.not()) {
                    handled = true
                    nextActivity()
                }
            }
        }
        AppOpenResumeManager.loadAd(this, callback)
    }

    // 27/10/2025 Cuong fix #147
    private fun showOpen() {
        val callback = object : OpenCallback {
            override fun onNextAction() {
                super.onNextAction()
                nextActivity()
            }
        }
        AppOpenResumeManager.showAd(this, true, callback)
    }

    private fun loadInterSplash() {
        if (remoteConfigAdSplashInter?.preload?.isUse == true) {
            preloadInterSplash()
            return
        }
        Log.v(tag, "...ad splash")
        initInterSplashByInterVer2()
        simpleInterSplashVer2?.loadInter(this, false)

        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            "LdNShAdOp", "ClV2_${simpleInterSplashVer2 != null}_$countInitAd"
        )
    }

    private fun preloadInterSplash() {
        val callback: InterCallback = object : InterCallback {
            private var handleData = false

            override fun onAdLoaded(interstitialAd: InterstitialAd?) {
                super.onAdLoaded(interstitialAd)
                Log.v(tag, "interSplash preload: onAdLoaded")
                if (handleData) {
                    return
                }
                handleData = true
                isInterLoaded = true
                if (needShowInter || isShowNativeAndInter.not()) setupShowInterSplash()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError?, adError: AdError?) {
                super.onAdFailedToLoad(loadAdError, adError)
                Log.v(tag, "callback preloadInterSplash onAdFailedToLoad: $loadAdError $adError")
                if (handleData.not()) {
                    handleData = true
                    isInterFailToLoad = true
                    if (needShowInter) nextActivity()
                }
            }

            override fun onCancelToLoad() {
                super.onCancelToLoad()
                Log.v(tag, "callback preloadInterSplash onCancelToLoad")
                if (handleData.not()) {
                    handleData = true
                    isInterFailToLoad = true
                    if (needShowInter || isShowNativeAndInter.not()) nextActivity()
                }
            }
        }
        val model = remoteConfigAdSplashInter?.toInterPreloadModel()
        if (model == null) {
            Log.v(tag, "function preloadInterSplash: model is null")
            nextActivity()
            return
        }
        Log.v(tag, "function start preload")
        InterPreload.startLoad(model, callback)
    }

    private fun showInterSplashPreloaded() {
        val model = remoteConfigAdSplashInter
        if (model == null) {
            Log.v(tag, "function showInterSplashPreloaded: model is null")
            nextActivity()
            return
        }
        AdCommon.showPreloadInter(
            this,
            RemoteConfigManager.getKeyAdInterSplash(),
            model,
            null,
            object : InterCallback {
                override fun onCancelToShow() {
                    super.onCancelToShow()
                    nextActivity()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    nextActivity()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    nextActivity()
                }
            }
        )
    }

    private fun initInterSplashByInterVer2() {
        Log.v(tag, "function initInterSplashByInterVer2")
        val funNameLogEvent = "InVer2Init"
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            funNameLogEvent, "BeginF"
        )
        val interCallback: InterCallback = object : InterCallback {

            override fun onCancelToLoad() {
                super.onCancelToLoad()
                isInterFailToLoad = true
                if (needShowInter || isShowNativeAndInter.not()) nextActivity()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError?, adError: AdError?) {
                super.onAdFailedToLoad(loadAdError, adError)
                isInterFailToLoad = true
                if (needShowInter || isShowNativeAndInter.not()) nextActivity()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd?) {
                super.onAdLoaded(interstitialAd)
                isInterLoaded = true
                if (needShowInter || isShowNativeAndInter.not()) setupShowInterSplash()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                nextActivity()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                nextActivity(true)
            }
        }

        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            funNameLogEvent, "InitSpIn"
        )

        //inter intro
        simpleInterSplashVer2 = SimpleInter0(
            remoteConfigAdSplashInter?.adId ?: "",
            remoteConfigAdSplashInter?.adName ?: ""
        )
        simpleInterSplashVer2?.setProperties(
            remoteConfigAdSplashInter?.isShow ?: true,
            remoteConfigAdSplashInter?.isShowNextActivityBeforeAd == true,
            interCallback
        )

        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashOpenEventName,
            funNameLogEvent, "EndF"
        )
    }

    private fun loadAndShowBannerAdaptive() {
        Log.v(tag, "function loadAndShowBannerAdaptive")
        AdCommon.loadAndShowAdBannerAdaptive(
            lifecycle,
            RemoteConfigManager.getKeyAdBannerSplash(),
            binding.frAdBottom,
            BannerGravity.BOTTOM,
            null
        )
    }

    private fun loadAdsIntro() {
        val languageModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenLanguageStart(),
            RemoteConfigScreenLanguageStartModel::class.java
        )
        if (languageModel?.nextScreenDefault?.contains("intro1") == true) {
            loadAdsIntroVer2()
        } else {
            loadInterIntro()
        }
    }

    private fun loadInterIntro() {
        val keyInterIntro = RemoteConfigManager.getKeyAdInterIntro()
        val interModel = RemoteConfigOrigin.getConfigObject(
            this,
            keyInterIntro,
            RemoteConfigAdInterModel::class.java
        )
        val useFan = interModel?.fan?.isUse == true
        val loadAt = interModel?.fan?.loadAt
        if (useFan && loadAt == LoadLocation.SPLASH) {
            AdCommon.loadInterFan(this, keyInterIntro)
        }
    }

    private fun loadAdsIntroVer2() {
        val interModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdInterIntro(),
            RemoteConfigAdInterModel::class.java
        )
        AdsIntroManager.loadInterFan(this, interModel, LoadLocation.SPLASH)

        val nativeFull = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeIntroFull(),
            RemoteConfigAdNativeModel::class.java
        )

        val screenIntro = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenIntro(),
            RemoteConfigScreenIntroModel::class.java
        )
        AdsIntroManager.loadNativeFull(this, nativeFull, screenIntro)
    }

    private fun loadAndShowNativeVer1() {
        Log.v(tag, "function loadAndShowNativeVer1")
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashInScreenEventName,
            "LdNShNtV1", "ClAdCm"
        )
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeSplash(),
            binding.frAdBottom,
            null
        )
    }

    private fun loadAndShowNativeVer2() {
        Log.v(tag, "function loadAndShowNativeVer2")
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashInScreenEventName,
            "LdNShNtV2", "Init"
        )
        val callback = object : NativeCallback {
            override fun onNotLoad() {
                super.onNotLoad()
                startTimerShowInter()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                startTimerShowInter()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                startTimerShowInter()
            }
        }
        simpleNativeAdvanceManagerSplashVer2 = SimpleNativeAdvanceManager0(
            remoteConfigAdSplashNative?.adId ?: "",
            remoteConfigAdSplashNative?.adName ?: ""
        )

        simpleNativeAdvanceManagerSplashVer2?.setProperties(
            remoteConfigAdSplashNative?.isShow ?: true,
            remoteConfigAdSplashNative?.adLayoutFile ?: "",
            remoteConfigAdSplashNative?.bgColor,
            remoteConfigAdSplashNative?.borderColor,
            callback,
        )

        simpleNativeAdvanceManagerSplashVer2?.setPropertiesForFan(
            remoteConfigAdSplashNative?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdSplashNative?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdSplashNative?.adLayoutFan ?: "",
            remoteConfigAdSplashNative?.bgColorFan ?: "",
            remoteConfigAdSplashNative?.borderColorFan ?: "",
            remoteConfigAdSplashNative?.buttonColorFan ?: "",
        )
        FirebaseLogEventUtils.logEventAdCommon(
            this, adSplashInScreenEventName,
            "LdNShNtV2", "Load"
        )
        simpleNativeAdvanceManagerSplashVer2?.loadNative(this, binding.frAdBottom, true)
        startTimeoutInter()
        startTimeoutNextScreen()
    }

    private fun startTimerShowInter() {
        isNativeDone = true
        if (isShowNativeAndInter.not()) return
        if (isInterFailToLoad) {
            nextActivity()
            return
        }
        val delayShow = remoteConfigScreenSplashModel?.interDelayShowWhenShowNative ?: 0
        handlerShowInter?.removeCallbacksAndMessages(null)
        handlerShowInter = Handler(Looper.getMainLooper())
        handlerShowInter?.postDelayed({
            if (isInterLoaded) {
                setupShowInterSplash()
            } else if (isInterFailToLoad) {
                nextActivity()
            } else {
                needShowInter = true
            }
        }, delayShow * 1000L)
    }

    private fun startTimeoutInter() {
        if (isShowNativeAndInter.not()) return
        val timeoutShow = remoteConfigScreenSplashModel?.interTimeoutWhenShowNative ?: 0
        handlerTimeoutShowInter?.removeCallbacksAndMessages(null)
        handlerTimeoutShowInter = Handler(Looper.getMainLooper())
        handlerTimeoutShowInter?.postDelayed({
            if (isNativeDone) {
                return@postDelayed
            } else if (isInterLoaded) {
                setupShowInterSplash()
            } else if (isInterFailToLoad) {
                nextActivity()
            } else {
                needShowInter = true
            }
        }, timeoutShow * 1000L)
    }

    private fun startTimeoutNextScreen() {
        if (isShowNativeAndInter.not()) return
        val timeout = remoteConfigScreenSplashModel?.timeoutNextScreenWhenShowNative ?: 0
        handlerTimeoutNextScreen?.removeCallbacksAndMessages(null)
        handlerTimeoutNextScreen = Handler(Looper.getMainLooper())
        handlerTimeoutNextScreen?.postDelayed({
            if (showInterCalled.not()) {
                nextActivity()
            }
        }, timeout * 1000L)
    }

    private fun setupShowInterSplash() {
        synchronized(this) {
            if (showInterCalled) {
                return
            }
            showInterCalled = true
            if (isPause && remoteConfigScreenSplashModel?.screenVersion == ScreenVersion.VER_2) {
                waitForShowInter = true
                needShowInter = true
            } else {
                showInterSplash()
            }
        }
    }

    private fun showInterSplash() {
        if (remoteConfigAdSplashInter?.preload?.isUse == true) {
            showInterSplashPreloaded()
        } else {
            simpleInterSplashVer2?.showInter(this@SplashActivity)
        }
    }

    fun nextActivity(showNativeFull: Boolean = false) {
        Log.v(tag, "function nextActivity showNativeFull: $showNativeFull")
        FirebaseLogEventUtils.logEventAdCommon(
            this, "Spl",
            "Next", ""
        )
        ScreenNavigate.nextScreenAndFinishCurrent(
            this,
            remoteConfigScreenSplashModel?.nextScreenDefault,
            LanguageStartActivity::class.java,
            showNativeFull,
            RemoteConfigManager.getKeyAdInterSplash()
        )
    }
}
