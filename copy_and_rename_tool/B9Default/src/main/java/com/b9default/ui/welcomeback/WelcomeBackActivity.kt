package com.b9default.ui.welcomeback


import android.util.Log
import android.view.View
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.simple.SimpleInter0
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9ads.admob.resumeAds.callback.OpenCallback
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.ad.AdCommon.checkShowAdByVersion
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9default.a1_common_utils.base.model.AdType
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdAppopenResumeModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenWelcomebackModel
import com.b9default.ui.ads.NativeFullActivity
import com.b9ui.databinding.ActivityWelcomeBackBinding
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.google.android.gms.ads.LoadAdError

class WelcomeBackActivity : BaseActivity<ActivityWelcomeBackBinding, BaseViewModel>() {

    private val tag: String = "B9Default_WelcomeBackActivityTag"

    //model
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null
    private var remoteConfigAdNativeWcb: RemoteConfigAdNativeModel? = null

    // 02/10/2025 Cuong fix #135
    private var remoteConfigScreen: RemoteConfigScreenWelcomebackModel? = null
    private var interWelcomeBack: RemoteConfigAdInterModel? = null
    private var simpleNative: SimpleNativeAdvanceManager0? = null
    private var simpleInter: SimpleInter0? = null


    override fun createBinding() = ActivityWelcomeBackBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        //get model data
        remoteConfigAppAllModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAppConfig(),
            RemoteConfigAppAllModel::class.java
        )

        remoteConfigAdNativeWcb = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeWelcomeback(),
            RemoteConfigAdNativeModel::class.java
        )

        remoteConfigScreen = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenWelcomeback(),
            RemoteConfigScreenWelcomebackModel::class.java
        )

        val openConfig = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdAppopenResume(),
            RemoteConfigAdAppopenResumeModel::class.java
        )
        openConfig?.toResumeModel()?.let {
            AppOpenResumeManager.setProperties(it)
        }

        //binding data
        binding.title.text = remoteConfigAppAllModel?.welcomebackTittle ?: ""

        //binding action
        binding.tvContinue.setOnClickListener {
            if (remoteConfigScreen?.adWcbFullscreenType == AdType.OPEN) {
                showOpenResume()
            } else {
                showInter()
            }
        }

        //hidden continue text
        if (checkShowAdByVersion())
            binding.tvContinue.visibility = View.GONE
        else
            binding.tvContinue.visibility = View.VISIBLE

    }

    override fun initAd() {
        if (checkShowAdByVersion()) {
            if (remoteConfigScreen?.adWcbFullscreenType == AdType.OPEN) {
                AppOpenResumeManager.loadAd(this.baseContext)
            } else {
                loadInter()
            }
        }

        Log.v(tag, "init native ad function ad ver ${remoteConfigAdNativeWcb?.adVer}")
        if (remoteConfigAdNativeWcb?.adVer == "ver2")
            intNativeVer2()
        else
            initNativeVer1()
    }

    private fun initNativeVer1() {
        Log.v(tag, "init native ad ver1")
        val nativeCallback: NativeCallback = object : NativeCallback {

            override fun onNotLoad() {
                super.onNotLoad()
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onNotShow() {
                super.onNotShow()
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onAdImpression() {
                super.onAdImpression()
                binding.tvContinue.visibility = View.VISIBLE
            }
        }
        //native
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle, RemoteConfigManager.getKeyAdNativeWelcomeback(),
            binding.frAd,
            nativeCallback
        )
    }

    private fun intNativeVer2() {
        Log.v(tag, "init native ad ver2")
        val nativeCallback: NativeCallback = object : NativeCallback {

            override fun onNotLoad() {
                super.onNotLoad()
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onNotShow() {
                super.onNotShow()
                binding.tvContinue.visibility = View.VISIBLE
            }

            override fun onAdImpression() {
                super.onAdImpression()
                binding.tvContinue.visibility = View.VISIBLE
            }

        }


        simpleNative = SimpleNativeAdvanceManager0(
            remoteConfigAdNativeWcb?.adId ?: "",
            remoteConfigAdNativeWcb?.adName ?: ""
        )

        simpleNative?.setProperties(
            remoteConfigAdNativeWcb?.isShow ?: true,
            remoteConfigAdNativeWcb?.adLayoutFile ?: "",
            remoteConfigAdNativeWcb?.bgColor,
            remoteConfigAdNativeWcb?.borderColor,
            nativeCallback,
        )

        simpleNative?.setPropertiesForFan(
            remoteConfigAdNativeWcb?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigAdNativeWcb?.isChangeDisableClickAllWhenFan == true,
            remoteConfigAdNativeWcb?.adLayoutFan ?: "",
            remoteConfigAdNativeWcb?.bgColorFan ?: "",
            remoteConfigAdNativeWcb?.borderColorFan ?: "",
            remoteConfigAdNativeWcb?.buttonColorFan ?: "",
        )

        simpleNative?.loadNative(this, binding.frAd, true)
    }

    // 02/10/2025 Cuong fix #135
    private fun showOpenResume() {
        AppOpenResumeManager.showAd(this, true, object : OpenCallback {
            override fun onNextAction() {
                finish()
            }
        })
    }

    // 27/10/2025 Cuong fix #142
    private fun loadInter() {
        val interWelcomeBack = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdInterWelcomeback(),
            RemoteConfigAdInterModel::class.java
        )
        this.interWelcomeBack = interWelcomeBack
        if (interWelcomeBack?.preload?.isUse == true) {
            AdCommon.preloadInter(interWelcomeBack)
        } else {
            loadSimpleInter()
        }
    }

    // 27/10/2025 Cuong fix #142
    private fun showInter() {
        if (interWelcomeBack?.preload?.isUse == true) {
            showInterPreloaded()
        } else {
            showSimpleInter()
        }
    }

    // 02/10/2025 Cuong fix #135
    private fun loadSimpleInter() {
        val interCallback = object : InterCallback {
            var needNative = false
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                needNative = true
            }

            override fun onNextAction() {
                super.onNextAction()
                finishWithNative(needNative)
            }
        }

        simpleInter = SimpleInter0(
            interWelcomeBack?.adId ?: "",
            interWelcomeBack?.adName ?: ""
        )
        simpleInter?.setProperties(
            interWelcomeBack?.isShow == true,
            interWelcomeBack?.isShowNextActivityBeforeAd == true,
            interCallback,
        )
        simpleInter?.loadInter(this)
    }

    // 27/10/2025 Cuong fix #142
    private fun showInterPreloaded() {
        val interCallback = object : InterCallback {
            override fun onNextAction() {
                super.onNextAction()
                finishWithNative()
            }
        }
        val interModel = interWelcomeBack
        if (interModel == null) {
            finishWithNative()
            return
        }
        AdCommon.showPreloadInter(
            this,
            RemoteConfigManager.getKeyAdInterWelcomeback(),
            interModel,
            null,
            interCallback
        )
    }

    // 02/10/2025 Cuong fix #135
    private fun showSimpleInter() {
        if (simpleInter == null) {
            finishWithNative(false)
            return
        }
        simpleInter?.waitAndShowInter(this)
    }

    // 02/10/2025 Cuong fix #135
    private fun finishWithNative(haveNative: Boolean = false) {
        if (haveNative) {
            val intent =
                NativeFullActivity.getIntent(this, RemoteConfigManager.getKeyAdInterWelcomeback())
            startActivity(intent)
        }
        finish()
    }
}
