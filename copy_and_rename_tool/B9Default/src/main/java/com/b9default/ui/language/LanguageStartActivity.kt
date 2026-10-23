package com.b9default.ui.language

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.b9ads.admob.interstitialAds.InterManager
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.model.Position
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdAllModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdAppopenResumeModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenLanguageStartModel
import com.b9default.a8_app_utils.ScreenNavigate
import com.b9default.a8_app_utils.SystemUtil
import com.b9default.ui.intro1.IntroActivity1
import com.b9default.ui.language.adapter.LanguageAdapter
import com.b9default.ui.language.model.LanguageItem
import com.b9default.ui.language.viewModel.LanguageViewModel
import com.b9ui.R
import com.b9ui.databinding.ActivityLanguageStartBinding
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.b9utils.util.SystemSharePreferenceUtils
import com.b9utils.util.appupdate.AppUpdateManager
import com.b9utils.util.appupdate.RemoteConfigVersionUpdateModel
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.google.android.gms.ads.nativead.NativeAdView

class LanguageStartActivity : BaseActivity<ActivityLanguageStartBinding, LanguageViewModel>() {
    private val tag: String = "V1Lib_B9Default_LanguageStartActivityTag"
    private var logEventAdName: String = "adNtLang"

    //language adapter
    private val languageAdapter by lazy { LanguageAdapter() }

    private val translatingDialog by lazy { TranslatingDialog(this) }

    //language screen open count
    private var screenOpenCountKey: String = "lang_start_open_count"
    private var screenOpenCount: Int = 0

    //ad variable
    private var remoteConfigScreenLanguageStartModel: RemoteConfigScreenLanguageStartModel? = null
    private var remoteConfigAdNativeLanguageModel: RemoteConfigAdNativeModel? = null
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null

    //other ad status
    private var adNativeLanguageVersion = "ver1"
    private var isReloadNativeWhenChangeLanguage: Boolean = false

    //native advance manager old
    private var nativeLanguageManagerV1: NativeAdvanceManager2? = null

    //native simpleV2
    private var nativeLanguageManagerV2: SimpleNativeAdvanceManager0? = null

    //native advance manager new
    private var simpleNativeAdvanceManagerBefore: SimpleNativeAdvanceManager0? = null
    private var simpleNativeAdvanceManagerAfter: SimpleNativeAdvanceManager0? = null

    // native manager drop
    private var nativeManagerDrop: SimpleNativeAdvanceManager0? = null

    //ad resume
    private var remoteConfigAdResumeAdModel: RemoteConfigAdAppopenResumeModel? = null

    //app version
    private var remoteConfigVersionUpdateModel: RemoteConfigVersionUpdateModel? = null

    //timer native
    //default reload native is v2, not reload to setting ver=ver1,ver2...
    private var nativeLanguageReloadTimerManagerV2: SimpleNativeAdvanceManager0? = null
    private val handlerReloadNativeTimer = Handler(Looper.getMainLooper())
    private var reloadAdRunnable: Runnable? = null

    // check native after showed or not
    private var nativeAfterShowed = false

    // check native drop showed or not
    private var nativeDropShowed = false

    override fun createBinding(): ActivityLanguageStartBinding {
        return ActivityLanguageStartBinding.inflate(layoutInflater)
    }

    override fun setViewModel() = LanguageViewModel()

    override fun initView() {
        super.initView()
        binding.rvLanguage.adapter = languageAdapter
        Log.v(tag, "function initView LanguageStartActivity")

        //open count
        screenOpenCount = SystemSharePreferenceUtils.getInt(this, this.screenOpenCountKey)
        screenOpenCount += 1
        SystemSharePreferenceUtils.setInt(this, this.screenOpenCountKey, screenOpenCount)
        Log.v(tag, "...screen open count $screenOpenCount")

        //screen model
        remoteConfigScreenLanguageStartModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenLanguageStart(),
            RemoteConfigScreenLanguageStartModel::class.java
        )

        //app all model
        remoteConfigAppAllModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAppConfig(),
            RemoteConfigAppAllModel::class.java
        )

        //native bottom model
        remoteConfigAdNativeLanguageModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeLanguageBottom(),
            RemoteConfigAdNativeModel::class.java
        )

        remoteConfigScreenLanguageStartModel?.transPopup?.delayTime?.let {
            translatingDialog.setTimeClose(it * 1000L)
        }
        //get ad native status
        adNativeLanguageVersion = remoteConfigAdNativeLanguageModel?.adVer?.lowercase() ?: "ver1"
        isReloadNativeWhenChangeLanguage =
            remoteConfigScreenLanguageStartModel?.isReloadAfterSelectLanguage ?: false

        //ad log name
        logEventAdName = makeKeyShortName(
            remoteConfigAdNativeLanguageModel?.adName ?: "ad_native_language_bottom"
        )

        Log.v(
            tag,
            "...ad native status: version=$adNativeLanguageVersion, reload =$isReloadNativeWhenChangeLanguage"
        )
        Log.v(tag, "...logEventAdName=$logEventAdName")
        //button next
        binding.ivCheckLeft.setOnClickListener {
            saveLanguageAndNextScreen()
        }

        binding.ivCheckRight.setOnClickListener {
            saveLanguageAndNextScreen()
        }
        setUpViewModel()
    }

    override fun initAd() {
        Log.v(tag, "function initAd...")
        val logEventFunctionName = "LangAct_InitAd"

        FirebaseLogEventUtils.logEventAdCommon(
            this,
            logEventAdName,
            logEventFunctionName,
            "BeginF"
        )

        //init ad native
        if (adNativeLanguageVersion == "ver2") {
            if (isReloadNativeWhenChangeLanguage) {
                Log.v(tag, "...call ad native v2 with reload")
                FirebaseLogEventUtils.logEventAdCommon(
                    this,
                    logEventAdName,
                    logEventFunctionName,
                    "ver2Re"
                )
                initAdNativeSimpleV2AndReload()
            } else {
                Log.v(tag, "...call ad native v2 no reload")
                FirebaseLogEventUtils.logEventAdCommon(
                    this,
                    logEventAdName,
                    logEventFunctionName,
                    "ver2NoRe"
                )
                initAdNativeSimpleV2NotReload()

            }
        } else {
            initAdNativeVer1Cuong()
            FirebaseLogEventUtils.logEventAdCommon(
                this,
                logEventAdName,
                logEventFunctionName,
                "ver1"
            )
            Log.v(tag, "...call ad native v1")
        }

        //init ad native reload timer
        initAdNativeReloadTimerV2()
        initTimerReloadNative()

        //init inter ad
        initInterAdVer1()
        InterManager.init(System.currentTimeMillis())

        //init ad resume
        initOpenResumeAd()

        //version
        setUpdateVersion()

        initNativeDrop()
    }

    override fun hideNavigationBar() {
        if (remoteConfigScreenLanguageStartModel?.isHideNaviMenu == true)
            return super.hideNavigationBar()
        else
            return
    }

    override fun bindView() {
        languageAdapter.setOnItemClick {
            // update UI selected
            viewModel.selectLang(it)

            if (it !is LanguageItem.Child) {
                showNativeAfter()
            } else {
                showNativeDrop()
            }
        }
    }

    private fun showNativeAfter() {
        val isVer2 = adNativeLanguageVersion == "ver2"
        if (isVer2 && isReloadNativeWhenChangeLanguage && nativeAfterShowed.not()) {
            simpleNativeAdvanceManagerAfter?.showNative(
                this@LanguageStartActivity,
                binding.frAdsLangBottom
            )
            nativeAfterShowed = true
        }
    }

    private fun showNativeDrop() {
        if (remoteConfigAdNativeLanguageModel?.nativeDrop?.isUse != true) return
        if (nativeManagerDrop == null || nativeDropShowed) return

        Log.v(tag, "call show native drop")
        nativeManagerDrop?.showNative(
            this@LanguageStartActivity,
            binding.frAdsLangBottom
        )
        nativeDropShowed = true
    }

    private fun initNativeDrop() {
        if (
            remoteConfigAdNativeLanguageModel?.nativeDrop?.isUse != true ||
            remoteConfigScreenLanguageStartModel?.enableLanguageDrop != true
        ) {
            return
        }

        Log.v(tag, "function initNativeDrop")
        var adIdDrop: String? = null
        var adNameDrop = ""
        val hasDropIdSecond = remoteConfigAdNativeLanguageModel?.nativeDrop?.adId?.size == 2
        if (screenOpenCount == 1) {
            adIdDrop = remoteConfigAdNativeLanguageModel?.nativeDrop?.adId?.firstOrNull()
            adNameDrop = (remoteConfigAdNativeLanguageModel?.adName ?: "") + "_DropL1"
        } else if (hasDropIdSecond) {
            adIdDrop = remoteConfigAdNativeLanguageModel?.nativeDrop?.adId?.lastOrNull()
            adNameDrop = (remoteConfigAdNativeLanguageModel?.adName ?: "") + "_DropL2"
        }
        nativeManagerDrop = SimpleNativeAdvanceManager0(adIdDrop ?: return, adNameDrop)
        setCommonProperty(
            nativeManagerDrop,
            remoteConfigAdNativeLanguageModel
        )
        nativeManagerDrop?.loadNative(this, null, false)
    }

    // set common property for native manager
    private fun setCommonProperty(
        manager: SimpleNativeAdvanceManager0?,
        config: RemoteConfigAdNativeModel?
    ) {
        if (config == null) return
        manager?.setProperties(
            config.isShow ?: true,
            config.adLayoutFile ?: "",
            config.bgColor,
            config.borderColor,
            null
        )

        manager?.setPropertiesForFan(
            config.isChangeButtonClickOnlyWhenFan == true,
            config.isChangeDisableClickAllWhenFan == true,
            config.adLayoutFan ?: "",
            config.bgColorFan ?: "",
            config.borderColorFan ?: "",
            config.buttonColorFan ?: "",
        )
    }

    private fun setUpViewModel() {
        viewModel.language.observe(this) {
            languageAdapter.submitList(it)
        }

        viewModel.selectedRegion.observe(this) {
            if (it.isNotBlank()) {
                handleTranDialog()
            }
        }

        viewModel.getLanguageList(
            languageStartModel = remoteConfigScreenLanguageStartModel,
            screenOpenCount = screenOpenCount
        )
    }

    private fun visibleBtnNext(after: Long = 0L, forceHide: Boolean = false) {
        if (forceHide) {
            binding.ivCheckLeft.isVisible = false
            binding.ivCheckRight.isVisible = false
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.ivCheckLeft.isVisible =
                remoteConfigAdNativeLanguageModel?.btnNext?.pos == Position.LEFT
            binding.ivCheckRight.isVisible =
                remoteConfigAdNativeLanguageModel?.btnNext?.pos == Position.RIGHT
            if (remoteConfigAdNativeLanguageModel?.btnNext?.pos == null) {
                binding.ivCheckRight.isVisible = true
            }
        }, after)
    }

    private fun handleTranDialog() {
        val delayShowBtn =
            (remoteConfigAdNativeLanguageModel?.clicked?.delayTimeShowBtnNext ?: 0f) * 1000
        val timeShow = remoteConfigScreenLanguageStartModel?.transPopup?.delayTime ?: 0
        val isUse = remoteConfigScreenLanguageStartModel?.transPopup?.isUse == true
        visibleBtnNext(forceHide = true)
        if (isUse && timeShow > 0) {
            translatingDialog.show()
        } else {
            visibleBtnNext(after = delayShowBtn.toLong())
        }
        translatingDialog.setOnDismissListener {
            visibleBtnNext(after = delayShowBtn.toLong())
        }
    }

    private fun saveLanguageAndNextScreen() {
        SystemUtil.saveLocale(this, viewModel.getSelectedCode())
        SystemUtil.saveRegionName(this, viewModel.getSelectedRegionName())
        //check next screen
        ScreenNavigate.nextScreenAndFinishCurrent(
            this,
            remoteConfigScreenLanguageStartModel?.nextScreenDefault,
            IntroActivity1::class.java
        )
    }

    private fun initAdNativeVer1Cuong() {
        //when Fan always hidden button check??
        val adNativeCallback: NativeCallback = object : NativeCallback {
            override fun onChangeCtrWhenFan(view: NativeAdView) {
                super.onChangeCtrWhenFan(view)
                Log.v(tag, "onChangeCtrWhenFan")
                visibleBtnNext(forceHide = true)
            }
        }

        //ad adPlacement in bottom?, not configs
        val frAd: FrameLayout = binding.frAdsLangBottom

        nativeLanguageManagerV1 = AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeLanguageBottom(),
            frAd,
            adNativeCallback
        )
    }

    private fun initAdNativeSimpleV2AndReload() {
        Log.v(tag, "function initAdNativeSimpleV2AndReload")
        Log.v(tag, "...init ad for screen view count: ${this.screenOpenCount}")

        if (this.screenOpenCount == 1) {
            val listAdId: List<String> =
                (remoteConfigAdNativeLanguageModel?.adIdL1 ?: "").split(",")
            if (listAdId.size < 2) return
            Log.v(tag, "...using ad_id_l1")
            simpleNativeAdvanceManagerBefore = SimpleNativeAdvanceManager0(
                listAdId[0],
                (remoteConfigAdNativeLanguageModel?.adName ?: "") + "L1X"
            )

            simpleNativeAdvanceManagerAfter = SimpleNativeAdvanceManager0(
                listAdId[1],
                (remoteConfigAdNativeLanguageModel?.adName ?: "") + "L1Y"
            )


        } else {
            val listAdId: List<String> =
                (remoteConfigAdNativeLanguageModel?.adIdL2 ?: "").split(",")
            if (listAdId.size < 2) return
            Log.v(tag, "...using ad_id_l2")
            simpleNativeAdvanceManagerBefore = SimpleNativeAdvanceManager0(
                listAdId[0],
                (remoteConfigAdNativeLanguageModel?.adName ?: "") + "L2X"
            )

            simpleNativeAdvanceManagerAfter = SimpleNativeAdvanceManager0(
                listAdId[1],
                (remoteConfigAdNativeLanguageModel?.adName ?: "") + "L2Y"
            )
        }


        val btnAdsColor = try {
            remoteConfigAdNativeLanguageModel?.clicked?.btnActColor?.toColorInt()
        } catch (_: Exception) {
            null
        }
        val disableColor = if (btnAdsColor == null) null else getColor(R.color.disable_color)
        simpleNativeAdvanceManagerBefore?.setProperties(
            remoteConfigAdNativeLanguageModel?.isShow ?: true,
            remoteConfigAdNativeLanguageModel?.adLayoutFile ?: "",
            remoteConfigAdNativeLanguageModel?.bgColor,
            remoteConfigAdNativeLanguageModel?.borderColor,
            null,
            disableColor
        )

        simpleNativeAdvanceManagerBefore?.setPropertiesForFan(
            remoteConfigAdNativeLanguageModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdNativeLanguageModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdNativeLanguageModel?.adLayoutFan ?: "",
            remoteConfigAdNativeLanguageModel?.bgColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.borderColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.buttonColorFan ?: "",
        )
        simpleNativeAdvanceManagerAfter?.setProperties(
            remoteConfigAdNativeLanguageModel?.isShow ?: true,
            remoteConfigAdNativeLanguageModel?.adLayoutFile ?: "",
            remoteConfigAdNativeLanguageModel?.bgColor,
            remoteConfigAdNativeLanguageModel?.borderColor,
            null,
            btnAdsColor
        )
        simpleNativeAdvanceManagerAfter?.setPropertiesForFan(
            remoteConfigAdNativeLanguageModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdNativeLanguageModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdNativeLanguageModel?.adLayoutFan ?: "",
            remoteConfigAdNativeLanguageModel?.bgColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.borderColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.buttonColorFan ?: "",
        )

        simpleNativeAdvanceManagerBefore?.loadNative(this, binding.frAdsLangBottom, true)
        simpleNativeAdvanceManagerAfter?.loadNative(this, binding.frAdsLangBottom, false)

    }

    private fun initAdNativeSimpleV2NotReload() {
        Log.v(tag, "function initAdNativeSimpleV2NotReload")
        nativeLanguageManagerV2 = SimpleNativeAdvanceManager0(
            remoteConfigAdNativeLanguageModel?.adId ?: "",
            remoteConfigAdNativeLanguageModel?.adName ?: ""
        )
        nativeLanguageManagerV2?.setProperties(
            remoteConfigAdNativeLanguageModel?.isShow ?: true,
            remoteConfigAdNativeLanguageModel?.adLayoutFile ?: "",
            remoteConfigAdNativeLanguageModel?.bgColor,
            remoteConfigAdNativeLanguageModel?.borderColor,
            null

        )
        nativeLanguageManagerV2?.setPropertiesForFan(
            remoteConfigAdNativeLanguageModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdNativeLanguageModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdNativeLanguageModel?.adLayoutFan ?: "",
            remoteConfigAdNativeLanguageModel?.bgColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.borderColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.buttonColorFan ?: "",
        )

        nativeLanguageManagerV2?.loadNative(this, binding.frAdsLangBottom, true)

    }

    override fun onDestroy() {
        super.onDestroy()
        simpleNativeAdvanceManagerBefore?.destroy()
        simpleNativeAdvanceManagerAfter?.destroy()
        nativeLanguageManagerV2?.destroy()

        nativeLanguageManagerV1 = null
        nativeLanguageManagerV2 = null
        simpleNativeAdvanceManagerAfter = null
        simpleNativeAdvanceManagerBefore = null

        nativeLanguageReloadTimerManagerV2?.destroy()
        nativeLanguageReloadTimerManagerV2 = null

        //remove handle
        reloadAdRunnable?.let { handlerReloadNativeTimer.removeCallbacks(it) }

    }

    //other init ad
    private fun initInterAdVer1() {
        Log.v(tag, "function initInterAd")

        val remoteConfigAdAllModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdConfigAll(),
            RemoteConfigAdAllModel::class.java
        )

        remoteConfigAdAllModel?.let {
            InterManager.setTimeInterval(
                remoteConfigAdAllModel.getInterInterval(),
                remoteConfigAdAllModel.getInterStartInterval()
            )
        }
    }

    private fun initOpenResumeAd() {
        Log.v(tag, "function initOpenResumeAd")

        remoteConfigAdResumeAdModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdAppopenResume(),
            RemoteConfigAdAppopenResumeModel::class.java
        )
        if ((remoteConfigAdResumeAdModel?.adId ?: "").isEmpty())
            return

//        remoteCon
//        AppOpenResumeManager.setProperties(
//            remoteConfigAdResumeAdModel?.adId,
//            remoteConfigAdResumeAdModel?.adName,
//            remoteConfigAdResumeAdModel?.isShow
//        )
    }

    private fun setUpdateVersion() {
        Log.v(tag, "function setUpdateVersion")

        remoteConfigVersionUpdateModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyVersionUpdate(),
            RemoteConfigVersionUpdateModel::class.java
        )

        //confirm xem lai logic cho nay
        //bug null
        remoteConfigVersionUpdateModel?.let {
            AppUpdateManager.checkUpdate = AppUpdateManager.checkUpdate(applicationContext, it)
        }
    }

    private fun initAdNativeReloadTimerV2() {
        Log.v(tag, "function initAdNativeTimerV2")
        nativeLanguageReloadTimerManagerV2 = SimpleNativeAdvanceManager0(
            remoteConfigAdNativeLanguageModel?.adIdRdTimer ?: "",
            (remoteConfigAdNativeLanguageModel?.adName ?: "") + "RlTi"
        )
        nativeLanguageReloadTimerManagerV2?.setProperties(
            remoteConfigAdNativeLanguageModel?.isShow ?: true,
            remoteConfigAdNativeLanguageModel?.adLayoutFile ?: "",
            remoteConfigAdNativeLanguageModel?.bgColor,
            remoteConfigAdNativeLanguageModel?.borderColor,
            null

        )
        nativeLanguageReloadTimerManagerV2?.setPropertiesForFan(
            remoteConfigAdNativeLanguageModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdNativeLanguageModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdNativeLanguageModel?.adLayoutFan ?: "",
            remoteConfigAdNativeLanguageModel?.bgColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.borderColorFan ?: "",
            remoteConfigAdNativeLanguageModel?.buttonColorFan ?: "",
        )
    }

    private fun initTimerReloadNative() {
        Log.v(tag, "function initTimerReloadNative")
        val isReload: Boolean = remoteConfigAdNativeLanguageModel?.isAutoReloadTimer ?: false
        val timeReloadInterval: Long =
            (remoteConfigAdNativeLanguageModel?.adReloadTimerInterval ?: 0) * 1000

        if (isReload and (timeReloadInterval > 0)) {
            Log.v(tag, "...setting reload timer with timer = $timeReloadInterval")
            reloadAdRunnable = object : Runnable {
                override fun run() {
                    Log.v(tag, "callback timer reload load and show native")
                    nativeLanguageReloadTimerManagerV2?.loadNative(
                        this@LanguageStartActivity,
                        binding.frAdsLangBottom,
                        true
                    )
                    handlerReloadNativeTimer.postDelayed(this, timeReloadInterval)
                }
            }
            handlerReloadNativeTimer.postDelayed(
                reloadAdRunnable as Runnable,
                timeReloadInterval
            ) // Bắt đầu lặp
        }
    }
}

