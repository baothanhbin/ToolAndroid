package com.b9default.ui.intro

import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.simple.SimpleInter0
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9ads.fan.interstitialAds.base.InterFanCallback
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.LoadLocation
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.b9default.a1_common_utils.view.tap
import com.b9default.a8_app_utils.ScreenNavigate
import com.b9default.ui.intro.adapter.IntroAdapter
import com.b9default.ui.intro.model.IntroModel
import com.b9ui.R
import com.b9ui.databinding.ActivityIntroBinding
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.b9utils.util.SystemSharePreferenceUtils
import com.b9utils.util.appupdate.AppUpdateManager
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

// Load quảng cáo native, native full và số lượng ads theo remote config
class IntroActivity : BaseActivity<ActivityIntroBinding, BaseViewModel>() {
    private val tag = "B9Default_IntroActivityTag"
    private var logEventAdNativeIntro: String = "adNtInt"

    //screen open count
    private var screenOpenCountKey: String = "intro_open_count"
    private var screenOpenCount: Int = 0

    //screen intro model
    private var remoteConfigScreenIntroModel: RemoteConfigScreenIntroModel? = null
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null
    private var remoteConfigNativeIntroModel: RemoteConfigAdNativeModel? = null
    private var remoteConfigNativeIntroFullModel: RemoteConfigAdNativeModel? = null
    private var remoteConfigInterIntroModel: RemoteConfigAdInterModel? = null

    //native ad
    private var nativeAdvanceV1: NativeAdvanceManager2? = null
    private var nativeFullScreenV1: NativeAdvanceManager2? = null
    private var simpleNativeAdvanceV2NotReload: SimpleNativeAdvanceManager0? = null
    private var simpleNativeFullScreenV2NotReload: SimpleNativeAdvanceManager0? = null

    //for reload native
    private var simpleNativeAdvanceSlide1V2Reload: SimpleNativeAdvanceManager0? = null
    private var simpleNativeAdvanceSlide2V2Reload: SimpleNativeAdvanceManager0? = null
    private var simpleNativeAdvanceSlide3V2Reload: SimpleNativeAdvanceManager0? = null
    private var simpleNativeAdvanceSlide4V2Reload: SimpleNativeAdvanceManager0? = null

    //inter ad
    private var simpleInterIntro: SimpleInter0? = null

    //intro adapter
    private var introAdapter: IntroAdapter? = null

    //slide info status
    //list slide
    val listIntroSlide = mutableListOf<IntroModel>()

    //list ad show status
    val listAdShowStatus = mutableListOf<Boolean>()

    //list btnNext show status
    val listBtnNextShowStatus = mutableListOf<Boolean>()

    //list obj native ad simple v2
    private val listSimpleNativeV2 = mutableListOf<SimpleNativeAdvanceManager0?>()

    //native full position
    private var nativeFullPos1 = -1
    private var nativeFullPos2 = -1

    // Có show btn close native full hay không
    private var isShowClose = false

    override fun createBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        Log.v(tag, "function initView IntroActivity")

        //open count
        screenOpenCount = SystemSharePreferenceUtils.getInt(this, this.screenOpenCountKey)
        screenOpenCount += 1
        SystemSharePreferenceUtils.setInt(this, this.screenOpenCountKey, screenOpenCount)
        Log.v(tag, "...screen open count $screenOpenCount")

        //lấy remote config AppAll
        getRemoteConfigModelData()

        //ad log name
        logEventAdNativeIntro =
            makeKeyShortName(remoteConfigNativeIntroModel?.adName ?: "ad_native_intro")
        Log.v(tag, "...logEventAdName=$logEventAdNativeIntro")

        //init native ad before for slide
        initNativeIntroV2Reload()

        //ad slide to manage list by show status
        initSlideData()

        //adapter data
        setSlideViewData()

        //action change slide
        binding.vpIntro.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                super.onPageSelected(position)

                //load inter
                if (binding.vpIntro.currentItem == 1) {
                    simpleInterIntro?.loadInter(this@IntroActivity, false)
                }
                //ad processing
                processAdInSlide(listIntroSlide, listAdShowStatus, position)
                showCloseBtn()

                //custom button next or textview next
                changeTextviewNextToButton(listIntroSlide, listBtnNextShowStatus, position)

            }
        })

        //textview next
        binding.tvNext.setOnClickListener {
            doNextSlide()
        }

        //add custom remote config button next
        binding.btnNext.setOnClickListener {
            doNextSlide()
        }

        //native full close
        binding.ivCloseNativeFull.tap {
            doNextSlide()
//            val currentPosition = binding.vpIntro.currentItem
//            val isAdSlide = listIntroSlide[currentPosition].image == 0
//
//            if (isAdSlide) {
//                processShowIntroImage() // Ẩn quảng cáo toàn màn hình
//                binding.vpIntro.setCurrentItem(
//                    currentPosition + 1,
//                    true
//                )
//                lastSlideNextProcess()
//            } else {
//                //Log event
//                Log.d(tag, "Không phải slide quảng cáo. Không cần đóng.")
//            }
        }
    }

    private fun doNextSlide() {
        if (!isLastSlide()) {
            val currentPosition = binding.vpIntro.currentItem
            binding.vpIntro.setCurrentItem(currentPosition + 1, true)
        } else {
            lastSlideNextProcess()
        }
    }

    private fun isLastSlide(): Boolean {
        val currentPosition = binding.vpIntro.currentItem
        return (currentPosition + 1 == listIntroSlide.size)
    }

    override fun initAd() {
        initNativeAd()
        initNativeIntroFullscreen()
        initInterIntro()
    }


    private fun initNativeIntroV1() {
        //native
        Log.v(tag, "...init native ad V1 with not reload and only 1 ad id")

        nativeAdvanceV1 = AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeIntro(),
            binding.frAdBottom,
            null
        )
    }

    private fun initNativeIntroV2NotReload() {
        //native

        Log.v(tag, "...init native ad V2 with not reload and only 1 ad id")
        simpleNativeAdvanceV2NotReload = SimpleNativeAdvanceManager0(
            remoteConfigNativeIntroModel?.adId ?: "",
            remoteConfigNativeIntroModel?.adName ?: ""
        )
        simpleNativeAdvanceV2NotReload?.setProperties(
            remoteConfigNativeIntroModel?.isShow ?: true,
            remoteConfigNativeIntroModel?.adLayoutFile ?: "",
            remoteConfigNativeIntroModel?.bgColor,
            remoteConfigNativeIntroModel?.borderColor,
            null
        )

        simpleNativeAdvanceV2NotReload?.setPropertiesForFan(
            remoteConfigNativeIntroModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigNativeIntroModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigNativeIntroModel?.adLayoutFan ?: "",
            remoteConfigNativeIntroModel?.bgColorFan ?: "",
            remoteConfigNativeIntroModel?.borderColorFan ?: "",
            remoteConfigNativeIntroModel?.buttonColorFan ?: "",
        )

        simpleNativeAdvanceV2NotReload?.loadNative(this, binding.frAdBottom, true)
    }

    private fun initNativeIntroV2Reload() {
        Log.v(tag, "function initNativeIntroV2Reload")
        val isReloadWhenChangeSlide: Boolean =
            (remoteConfigScreenIntroModel?.isReloadAdWhenChangeSlide == true)
        val isVer2: Boolean = (remoteConfigNativeIntroModel?.adVer == "ver2")

        if (!isReloadWhenChangeSlide or !isVer2) {
            Log.v(tag, "...can not init, reload =$isReloadWhenChangeSlide, ver2=$isVer2")
            return
        }

        //default break adID for Reload
        Log.v(tag, "...init native ad V2 with reload status and break ad id")

        val adIDSlideList: List<String> = if (this.screenOpenCount == 1) {
            (remoteConfigNativeIntroModel?.adIdL1 ?: "").split(",")
        } else {
            (remoteConfigNativeIntroModel?.adIdL2 ?: "").split(",")
        }
        if (adIDSlideList.size < 4) return

        //init all simple native in list
        if (remoteConfigScreenIntroModel?.isSlide1Show != false && remoteConfigScreenIntroModel?.isSlide1AdShow != false) {
            simpleNativeAdvanceSlide1V2Reload = initNativeIntroV2One(adIDSlideList[0])
        }

        if (remoteConfigScreenIntroModel?.isSlide2Show != false && remoteConfigScreenIntroModel?.isSlide2AdShow != false) {
            simpleNativeAdvanceSlide2V2Reload = initNativeIntroV2One(adIDSlideList[1])
        }

        if (remoteConfigScreenIntroModel?.isSlide3Show != false && remoteConfigScreenIntroModel?.isSlide3AdShow != false) {
            simpleNativeAdvanceSlide3V2Reload = initNativeIntroV2One(adIDSlideList[2])
        }

        if (remoteConfigScreenIntroModel?.isSlide4Show != false && remoteConfigScreenIntroModel?.isSlide4AdShow != false) {
            simpleNativeAdvanceSlide4V2Reload = initNativeIntroV2One(adIDSlideList[3])
        }

    }

    private fun loadNativeIntroV2Reload() {
        //load and show ad in slide1 immediately
        //other ad is load but not show
        val listSimpleNativeV2NotNull = listSimpleNativeV2.filterNotNull()
        val firstElement = listSimpleNativeV2NotNull.firstOrNull() ?: return
        for (simpleNativeV2 in listSimpleNativeV2NotNull) {
            if (simpleNativeV2 == firstElement) {
                simpleNativeV2.loadNative(this, binding.frAdBottom, true)
            } else {
                simpleNativeV2.loadNative(this, binding.frAdBottom, false)
            }
        }
    }

    private fun getFirstNativeElement(): SimpleNativeAdvanceManager0? {
        var firstElement: SimpleNativeAdvanceManager0? = null
        for (element in listSimpleNativeV2) {
            if (element != null) {
                firstElement = element
                break
            }
        }
        return firstElement
    }


    private fun isNativeV2Reload(): Boolean {
        val isReloadWhenChangeSlide: Boolean =
            (remoteConfigScreenIntroModel?.isReloadAdWhenChangeSlide == true)
        val isVer2: Boolean = (remoteConfigNativeIntroModel?.adVer == "ver2")
        return (isReloadWhenChangeSlide) and (isVer2)
    }

    private fun initNativeIntroV2One(
        adID: String,
    ): SimpleNativeAdvanceManager0 {

        Log.v(tag, "...init simple native ad obj")

        val simpleNativeAdvanceV2 = SimpleNativeAdvanceManager0(
            adID,
            remoteConfigNativeIntroModel?.adName ?: ""
        )

        simpleNativeAdvanceV2.setProperties(
            remoteConfigNativeIntroModel?.isShow ?: true,
            remoteConfigNativeIntroModel?.adLayoutFile ?: "",
            remoteConfigNativeIntroModel?.bgColor,
            remoteConfigNativeIntroModel?.borderColor,
            null
        )
        simpleNativeAdvanceV2.setPropertiesForFan(
            remoteConfigNativeIntroModel?.isChangeButtonClickOnlyWhenFan == true,
            remoteConfigNativeIntroModel?.isChangeDisableClickAllWhenFan == true,
            remoteConfigNativeIntroModel?.adLayoutFan ?: "",
            remoteConfigNativeIntroModel?.bgColorFan ?: "",
            remoteConfigNativeIntroModel?.borderColorFan ?: "",
            remoteConfigNativeIntroModel?.buttonColorFan ?: "",
        )
        return simpleNativeAdvanceV2
    }

    private fun initNativeAd() {
        Log.v(tag, "function initNativeAd...")
        val logEventFunctionName = "IntAct_InitNt"
        val isReloadWhenChangeSlide: Boolean =
            (remoteConfigScreenIntroModel?.isReloadAdWhenChangeSlide == true)
        val isVer2: Boolean = (remoteConfigNativeIntroModel?.adVer == "ver2")

        FirebaseLogEventUtils.logEventAdCommon(
            this, logEventAdNativeIntro,
            logEventFunctionName, "BeginF"
        )
        if (isVer2) {
            if (isReloadWhenChangeSlide) {
                loadNativeIntroV2Reload()
                FirebaseLogEventUtils.logEventAdCommon(
                    this, logEventAdNativeIntro,
                    logEventFunctionName, "ver2Re"
                )
                Log.v(tag, "... call loadNativeIntroV2Reload")
            }
            //init in function init initNativeIntroV2Reload
            //because need init before function initSlideDate
            //this function load ad process

            else {
                initNativeIntroV2NotReload()
                FirebaseLogEventUtils.logEventAdCommon(
                    this, logEventAdNativeIntro,
                    logEventFunctionName, "ver2NoRe"
                )
                Log.v(tag, "... call initNativeIntroV2NotReload")
            }

        } else {
            initNativeIntroV1()
            FirebaseLogEventUtils.logEventAdCommon(
                this, logEventAdNativeIntro,
                logEventFunctionName, "ver1"
            )
            Log.v(tag, "... call initNativeIntroV1")
        }
    }

    private fun initNativeIntroFullscreen() {
        val callbackFullScreen: NativeCallback = object : NativeCallback {
            override fun onNotLoad() {
                super.onNotLoad()
                isShowClose = true
                showCloseBtn()
                removeNativeFullSlide()
                setSlideViewData()
            }

            override fun onNotShow() {
                super.onNotShow()
                isShowClose = true
                showCloseBtn()
                removeNativeFullSlide()
                setSlideViewData()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                isShowClose = true
                showCloseBtn()
                removeNativeFullSlide()
                setSlideViewData()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                isShowClose = true
                showCloseBtn()
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                Log.v(tag, "ad native fullscreen - onAdSwipeGestureClicked")
            }

        }

        if (remoteConfigNativeIntroFullModel?.adVer == "ver2") {
            //native full
            simpleNativeFullScreenV2NotReload = SimpleNativeAdvanceManager0(
                remoteConfigNativeIntroFullModel?.adId ?: "",
                remoteConfigNativeIntroFullModel?.adName ?: ""
            )
            simpleNativeFullScreenV2NotReload?.setProperties(
                remoteConfigNativeIntroFullModel?.isShow ?: true,
                remoteConfigNativeIntroFullModel?.adLayoutFile ?: "",
                remoteConfigNativeIntroFullModel?.bgColor,
                remoteConfigNativeIntroFullModel?.borderColor,
                callbackFullScreen
            )


            simpleNativeFullScreenV2NotReload?.loadNative(
                this,
                binding.frAdFull,
                true
            )
        } else {
            //ad native fullscreen
            nativeFullScreenV1 = AdCommon.loadAndShowAdNativeAdvance(
                lifecycle,
                RemoteConfigManager.getKeyAdNativeIntroFull(),
                binding.frAdFull,
                callbackFullScreen
            )
        }
    }

    private fun showCloseBtn() {
        val currentPosition = binding.vpIntro.currentItem
        val isAdSlide = listIntroSlide[currentPosition].image == 0
        binding.ivCloseNativeFull.isVisible = isAdSlide && isShowClose
    }

    private fun initInterIntro() {
        if (remoteConfigInterIntroModel?.fan?.isUse == true) {
            if (remoteConfigInterIntroModel?.fan?.loadAt != LoadLocation.SPLASH) {
                AdCommon.loadInterFan(this, RemoteConfigManager.getKeyAdInterIntro())
            }
            return
        }
        val remoteConfigInterIntroModel = remoteConfigInterIntroModel
        if (remoteConfigInterIntroModel?.preload?.isUse == true) {
            AdCommon.preloadInter(remoteConfigInterIntroModel)
            return
        }
        val interCallback: InterCallback = object : InterCallback {

            override fun onCancelToShow() {
                super.onCancelToShow()
                nextActivityProcess()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                nextActivityProcess()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                nextActivityProcess(true)
            }
        }

        //inter intro
        simpleInterIntro = SimpleInter0(
            remoteConfigInterIntroModel?.adId ?: "",
            remoteConfigInterIntroModel?.adName ?: ""
        )
        simpleInterIntro?.setProperties(
            remoteConfigInterIntroModel?.isShow == true,
            remoteConfigInterIntroModel?.isShowNextActivityBeforeAd == true,
            interCallback
        )

    }

    private fun initSlideData() {
        if (remoteConfigScreenIntroModel?.isSlide1Show != false) {
            listIntroSlide.add(
                IntroModel(
                    R.drawable.img_intro1,
                    getString(R.string.title_intro_1),
                    getString(R.string.description_intro_1),
                )
            )
            listAdShowStatus.add(remoteConfigScreenIntroModel?.isSlide1AdShow ?: true)
            listBtnNextShowStatus.add(remoteConfigScreenIntroModel?.isSlide1NextBtn ?: false)
            //simple native
            listSimpleNativeV2.add(simpleNativeAdvanceSlide1V2Reload)
        }

        if (remoteConfigScreenIntroModel?.isSlide2Show != false) {
            listIntroSlide.add(
                IntroModel(
                    R.drawable.img_intro2,
                    getString(R.string.title_intro_2),
                    getString(R.string.description_intro_2),
                )
            )
            listAdShowStatus.add(remoteConfigScreenIntroModel?.isSlide2AdShow ?: true)
            listBtnNextShowStatus.add(remoteConfigScreenIntroModel?.isSlide2NextBtn ?: false)
            //simple native
            listSimpleNativeV2.add(simpleNativeAdvanceSlide2V2Reload)
        }

        if (remoteConfigScreenIntroModel?.isSlide3Show != false) {
            listIntroSlide.add(
                IntroModel(
                    R.drawable.img_intro3,
                    getString(R.string.title_intro_3),
                    getString(R.string.description_intro_3),
                )
            )
            listAdShowStatus.add(remoteConfigScreenIntroModel?.isSlide3AdShow ?: true)
            listBtnNextShowStatus.add(remoteConfigScreenIntroModel?.isSlide3NextBtn ?: false)
            //simple native
            listSimpleNativeV2.add(simpleNativeAdvanceSlide3V2Reload)

        }

        if (remoteConfigScreenIntroModel?.isSlide4Show != false) {
            listIntroSlide.add(
                IntroModel(
                    R.drawable.img_intro4,
                    getString(R.string.title_intro_4),
                    getString(R.string.description_intro_4),
                )
            )
            listAdShowStatus.add(remoteConfigScreenIntroModel?.isSlide4AdShow ?: true)
            listBtnNextShowStatus.add(remoteConfigScreenIntroModel?.isSlide4NextBtn ?: false)
            //simple native
            listSimpleNativeV2.add(simpleNativeAdvanceSlide4V2Reload)
        }

        //ad native full to list
        val adNativeFullPos1 = (remoteConfigScreenIntroModel?.adNativeFullPos1 ?: 0) - 1
        if ((adNativeFullPos1 >= 0) and (adNativeFullPos1 <= listIntroSlide.size)) {
            listIntroSlide.add(adNativeFullPos1, IntroModel(0, "", ""))
            listAdShowStatus.add(adNativeFullPos1, false)
            listBtnNextShowStatus.add(adNativeFullPos1, false)
            listSimpleNativeV2.add(adNativeFullPos1, null)
            nativeFullPos1 = adNativeFullPos1
        }

        val adNativeFullPos2 = (remoteConfigScreenIntroModel?.adNativeFullPos2 ?: 0) - 1
        if ((adNativeFullPos2 >= 0) and (adNativeFullPos2 < listIntroSlide.size)) {
            listIntroSlide.add(adNativeFullPos2, IntroModel(0, "", ""))
            listAdShowStatus.add(adNativeFullPos2, false)
            listBtnNextShowStatus.add(adNativeFullPos2, false)
            listSimpleNativeV2.add(adNativeFullPos2, null)
            nativeFullPos2 = adNativeFullPos2
        }
    }

    override fun hideNavigationBar() {
        if (remoteConfigScreenIntroModel?.isHideNaviMenu == true)
            return super.hideNavigationBar()
        else
            return
    }

    //hidden native bottom and image, show native full
    private fun processShowAdNativeFull() {

        binding.frAdFull.visibility = View.VISIBLE
        binding.frAdBottom.visibility = View.GONE
        binding.layoutIndicator.visibility = View.GONE

    }

    //hidden native full, show image and native bottom
    private fun processShowIntroImage() {
        binding.frAdFull.visibility = View.GONE
        binding.ivCloseNativeFull.visibility = View.GONE
        binding.frAdBottom.visibility = View.VISIBLE
        binding.layoutIndicator.visibility = View.VISIBLE
    }

    private fun changeTextviewNextToButton(
        listIntroSlide: MutableList<IntroModel>,
        listBtnNextShowStatus: MutableList<Boolean>,
        position: Int,
    ) {
        val isShowNativeFull = (listIntroSlide[position].image == 0)
        if (isShowNativeFull)
            return
        if (listBtnNextShowStatus[position]) {
            binding.btnNext.visibility = View.VISIBLE
            binding.tvNext.visibility = View.GONE
            binding.indicator.visibility = View.VISIBLE
            binding.viewNext.visibility = View.VISIBLE
            binding.viewdot.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.GONE
            binding.tvNext.visibility = View.VISIBLE
            binding.indicator.visibility = View.VISIBLE
            binding.viewNext.visibility = View.GONE
            binding.viewdot.visibility = View.GONE
        }
    }

    private fun processAdInSlide(
        listIntroSlide: MutableList<IntroModel>,
        listAdShowStatus: MutableList<Boolean>,
        position: Int,
    ) {
        Log.v(tag, "function processAdInSlide")

        val isShowNativeFull = (listIntroSlide[position].image == 0)
        Log.v(tag, "...position = $position, is native full $isShowNativeFull")

        //show native full
        if (isShowNativeFull) {
            //this.countPassNativeFull += 1
            processShowAdNativeFull()
        }

        //show native bottom if show_ad = true
        else if (listAdShowStatus[position]) {
            processShowIntroImage()
            binding.frAdBottom.visibility = View.VISIBLE

            //reload native bottom
            if (isNativeV2Reload() && (listSimpleNativeV2[position] != getFirstNativeElement()))
                listSimpleNativeV2[position]?.showNative(this, binding.frAdBottom)

        } else {
            processShowIntroImage()
            binding.frAdBottom.visibility = View.GONE
        }
    }

    //adjust distance button vs ad by firebase config
    override fun adjustLayout() {
        val bottomMargin: Int = remoteConfigScreenIntroModel?.distanceButtonNextVsAd ?: 0

        val paramsTextView = binding.tvNext.layoutParams as MarginLayoutParams
        val paramsDotsIndicator = binding.indicator.layoutParams as MarginLayoutParams

        //textview button
        paramsTextView.setMargins(
            paramsTextView.leftMargin,
            paramsTextView.topMargin,
            paramsTextView.rightMargin,
            bottomMargin
        )

        //dot indicator
        paramsDotsIndicator.setMargins(
            paramsDotsIndicator.leftMargin,
            paramsDotsIndicator.topMargin,
            paramsDotsIndicator.rightMargin,
            bottomMargin
        )

        //font size
        val textSize: Float = remoteConfigScreenIntroModel?.fontSize ?: 20F
        binding.tvNext.textSize = textSize
    }

    private fun getRemoteConfigModelData() {
        remoteConfigAppAllModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAppConfig(),
            RemoteConfigAppAllModel::class.java
        )


        remoteConfigScreenIntroModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenIntro(),
            RemoteConfigScreenIntroModel::class.java
        )

        remoteConfigNativeIntroModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeIntro(),
            RemoteConfigAdNativeModel::class.java
        )
        remoteConfigNativeIntroFullModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdNativeIntroFull(),
            RemoteConfigAdNativeModel::class.java
        )

        remoteConfigInterIntroModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdInterIntro(),
            RemoteConfigAdInterModel::class.java
        )
    }

    private fun lastSlideNextProcess() {
        //last slide
        if (isLastSlide()) {
            Log.v(tag, "next process in last slide ${binding.vpIntro.currentItem}")
            val remoteConfigInterIntroModel = remoteConfigInterIntroModel
            if (remoteConfigInterIntroModel?.fan?.isUse == true) {
                val nextScreenFull = remoteConfigScreenIntroModel?.nextScreenDefault ?: "app@home"
                val intent = ScreenNavigate.getIntent(applicationContext, nextScreenFull)
                AdCommon.showInterFan(
                    this,
                    RemoteConfigManager.getKeyAdInterIntro(),
                    intent,
                    object : InterFanCallback {
                        override fun onNextAction() {
                            finish()
                        }
                    })
            } else if (remoteConfigInterIntroModel?.preload?.isUse == true) {
                AdCommon.showPreloadInter(
                    this,
                    "",
                    remoteConfigInterIntroModel,
                    null,
                    object : InterCallback {

                        override fun onCancelToShow() {
                            super.onCancelToShow()
                            nextActivityProcess()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            nextActivityProcess(true)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            nextActivityProcess()
                        }
                    })
            } else {
                simpleInterIntro?.showInter(this@IntroActivity)
            }
        }
    }

    private fun setSlideViewData() {
        introAdapter = IntroAdapter(this@IntroActivity, listIntroSlide)
        introAdapter?.list?.addAll(listIntroSlide)
        binding.vpIntro.adapter = introAdapter

        binding.indicator.attachTo(binding.vpIntro)
        binding.vpIntro.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
    }

    private fun nextActivityProcess(withNativeFull: Boolean = false) {
        Log.d(tag, AppUpdateManager.checkUpdate.toString())
        Log.d(tag, remoteConfigAppAllModel?.appUpdateDiaglogOrActivity.toString())
        //check next screen
        val nextScreenFull = remoteConfigScreenIntroModel?.nextScreenDefault ?: "app@home"
        ScreenNavigate.nextScreenAndFinishCurrent(
            this,
            nextScreenFull,
            withNativeFull = withNativeFull,
            remoteConfigKeyInter = RemoteConfigManager.getKeyAdInterIntro(),
        )
    }

    private fun removeNativeFullSlide() {
        if (nativeFullPos2 > -1) {
            val removePos = nativeFullPos2
            listIntroSlide.removeAt(removePos)
            listAdShowStatus.removeAt(removePos)
            listBtnNextShowStatus.removeAt(removePos)
            nativeFullPos2 = -1
        }

        if (nativeFullPos1 > -1) {
            val removePos = nativeFullPos1
            listIntroSlide.removeAt(removePos)
            listAdShowStatus.removeAt(removePos)
            listBtnNextShowStatus.removeAt(removePos)
            nativeFullPos1 = -1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleNativeAdvanceV2NotReload?.destroy()
        simpleNativeFullScreenV2NotReload?.destroy()
        simpleInterIntro?.destroy()
        simpleNativeAdvanceSlide1V2Reload?.destroy()
        simpleNativeAdvanceSlide2V2Reload?.destroy()
        simpleNativeAdvanceSlide3V2Reload?.destroy()
        simpleNativeAdvanceSlide4V2Reload?.destroy()

        nativeAdvanceV1 = null
        nativeFullScreenV1 = null
        simpleNativeAdvanceV2NotReload = null
        simpleNativeFullScreenV2NotReload = null
        simpleInterIntro = null

        simpleNativeAdvanceSlide1V2Reload = null
        simpleNativeAdvanceSlide2V2Reload = null
        simpleNativeAdvanceSlide3V2Reload = null
        simpleNativeAdvanceSlide4V2Reload = null
    }
}