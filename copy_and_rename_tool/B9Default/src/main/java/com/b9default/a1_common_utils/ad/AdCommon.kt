package com.b9default.a1_common_utils.ad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.bannerAds.adaptive.AdaptiveBannerManager
import com.b9ads.admob.bannerAds.base.BannerCallback
import com.b9ads.admob.bannerAds.collapsible.BannerGravity
import com.b9ads.admob.bannerAds.collapsible.CollapseBannerManager
import com.b9ads.admob.interstitialAds.InterManager
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.preload.InterPreload
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9ads.admob.nativeAds.collapse.NativeCollapseManager
import com.b9ads.admob.rewardAds.RewardInterstitialAdManager
import com.b9ads.admob.rewardAds.RewardManager
import com.b9ads.admob.rewardAds.callback.RewardCallback
import com.b9ads.custom.nativeCollapse.NativeCollapseContainer
import com.b9ads.fan.interstitialAds.InterFanManager
import com.b9ads.fan.interstitialAds.base.InterFanCallback
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdBannerModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeCollap
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdRewardModel
import com.b9default.ui.ads.NativeFullActivity
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.b9utils.util.ext.toColor
import com.b9utils.util.remoteconfig.RemoteConfigOrigin

object AdCommon {
    private const val TAG = "AdCommon"
    private const val LOG_EVENT_CLASS_NAME = "AdCm"

    init {
        InterFanManager.setNativeFullActivity(NativeFullActivity::class.java.name)
        InterManager.setNativeFullActivity(NativeFullActivity::class.java.name)
        InterPreload.setNativeFullActivity(NativeFullActivity::class.java.name)
    }

    /**
     * for banner ad
     */
    fun loadAndShowAdBannerAdaptive(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        gravity: BannerGravity,
        bannerCallback: BannerCallback? = null,
    ): AdaptiveBannerManager? {
        val bannerConfigModel = RemoteConfigOrigin.getConfigObject(
            frAd.context,
            rmcfKey,
            RemoteConfigAdBannerModel::class.java
        )

        //check not show
        if (bannerConfigModel == null) {
            return null
        }

        return loadAndShowBanner(
            lifecycle,
            rmcfKey,
            frAd,
            gravity,
            bannerConfigModel,
            bannerCallback
        )
    }

    fun loadAndShowAdBanner(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        gravity: BannerGravity,
        bannerModel: RemoteConfigAdBannerModel?,
        bannerCallback: BannerCallback? = null,
    ): AdaptiveBannerManager? {

        if (bannerModel == null) {
            return null
        }

        return loadAndShowBanner(
            lifecycle,
            rmcfKey,
            frAd,
            gravity,
            bannerModel,
            bannerCallback
        )
    }

    private fun loadAndShowBanner(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        gravity: BannerGravity,
        bannerModel: RemoteConfigAdBannerModel,
        bannerCallback: BannerCallback? = null,
    ): AdaptiveBannerManager? {
        //check version not show ad
        if (!checkShowAdByVersion())
            return null

        //check not show
        if (bannerModel.adId.isNullOrBlank() || bannerModel.isShow == false) {
            return null
        }

        // Khởi tạo Manager
        val bannerAdaptiveManager =
            AdaptiveBannerManager(bannerModel.adId, lifecycle, bannerCallback)


        //set adInfo for log firebase
        bannerAdaptiveManager.setBannerAdName(makeKeyShortName(rmcfKey))

        //show ads
        bannerAdaptiveManager.loadAndShowAdaptiveBanner(frAd)

        //hidden line
        //TOP thi hide line TOP, BOTTOM thi hide line botom
        if (gravity == BannerGravity.TOP)
            bannerAdaptiveManager.setVisibleLineTop(false)
        else if (gravity == BannerGravity.BOTTOM)
            bannerAdaptiveManager.setVisibleLineBottom(false)

        return bannerAdaptiveManager
    }

    //collapsible banner
    fun loadAndShowAdBannerCollapse(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        gravity: BannerGravity,
        bannerCallback: BannerCallback? = null,
    ): CollapseBannerManager? {

        // Lấy remote config
//        val bannerConfigModel = com.util.remoteconfig.RemoteConfig.getConfigObject(
//            frAd.context,
//            rmcfKey,
//            RemoteConfigAdBannerModel::class.java
//        )
        val bannerConfigModel = RemoteConfigOrigin.getConfigObject(
            frAd.context,
            rmcfKey,
            RemoteConfigAdBannerModel::class.java
        )

        //check ump
        //ANH update 20250108
//        if (AdsConsentManager.getInstance(frAd.context).canRequestAds.not())
//            return null

        //check not show
        if (bannerConfigModel == null || bannerConfigModel.isShow == false) {
            return null
        }

        //check version not show ad
        if (!checkShowAdByVersion())
            return null

        // Khởi tạo BannerManager
        //bannerCollapseManager: CollapseBannerManager? = null
        val bannerCollapseManager =
            CollapseBannerManager(bannerConfigModel.adId ?: "", lifecycle, bannerCallback)


        //set adInfo for log firebase
        //val screenName: String = frAd.context.javaClass.simpleName
        bannerCollapseManager.setBannerAdName(makeKeyShortName(rmcfKey))

        //show ads
        bannerCollapseManager.loadAndShowCollapBanner(frAd, gravity = gravity)

        //hidden line
        //TOP thi hide line TOP, BOTTOM thi hide line bottom
        if (gravity == BannerGravity.TOP)
            bannerCollapseManager.setVisibleLineTop(false)
        else if (gravity == BannerGravity.BOTTOM)
            bannerCollapseManager.setVisibleLineBottom(false)

        return bannerCollapseManager

    }

    /**
     * for native ad
     * layout in app
     */

    fun loadAndShowAdNativeAdvance(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        nativeCallback: NativeCallback? = null,
    ): NativeAdvanceManager2? {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        val context = frAd.context

        val nativeModel = RemoteConfigOrigin.getConfigObject(
            frAd.context,
            rmcfKey,
            RemoteConfigAdNativeModel::class.java
        )

        if (nativeModel == null) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNtMdNull"
            )
            return null
        }

        return initAndLoadNative(lifecycle, rmcfKey, frAd, nativeCallback, nativeModel)
    }

    fun loadAndShowNativeCollapse(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        container: NativeCollapseContainer,
        nativeModel: RemoteConfigAdNativeCollap?,
        nativeCallback: NativeCallback? = null,
    ): NativeAdvanceManager2? {
        if (nativeModel == null) {
            return null
        }
        return initAndLoadNativeCollapse(
            lifecycle,
            rmcfKey,
            container,
            nativeCallback,
            nativeModel
        )
    }

    private fun initAndLoadNativeCollapse(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        nativeCallback: NativeCallback? = null,
        nativeModel: RemoteConfigAdNativeCollap,
    ): NativeCollapseManager? {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        val context = frAd.context

        val nativeAdvanceManager2 = initNativeCollapManager(
            lifecycle,
            rmcfKey,
            nativeModel,
            context,
            nativeCallback
        )
        if (nativeModel.native == null) {
            return null
        }
        loadNative(nativeAdvanceManager2, frAd, nativeModel.native, nativeCallback)
        //log
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )

        return nativeAdvanceManager2
    }

    private fun initNativeCollapManager(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        nativeCollapseModel: RemoteConfigAdNativeCollap,
        context: Context,
        nativeCallback: NativeCallback? = null,
    ): NativeCollapseManager? {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        Log.v(TAG, "loadAndShowAdNativeAdvance")
        val nativeModel = nativeCollapseModel.native ?: return null
        if (canLoadNative(rmcfKey, nativeModel, context, nativeCallback).not()) {
            return null
        }

        // Khởi tạo nativeManager
        val nativeManager = NativeCollapseManager(
            nativeCollapseModel.toNativeModel(),
            lifecycle
        )
        configNativeManager(nativeManager, nativeModel)

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "ClLdNSh"
        )
        return nativeManager
    }

    private fun initAndLoadNative(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        frAd: FrameLayout,
        nativeCallback: NativeCallback? = null,
        nativeModel: RemoteConfigAdNativeModel,
    ): NativeAdvanceManager2? {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        val context = frAd.context

        val nativeAdvanceManager2 = initNativeAdvanceManager2(
            lifecycle,
            rmcfKey,
            nativeModel,
            context,
            nativeCallback
        )
        loadNative(nativeAdvanceManager2, frAd, nativeModel, nativeCallback)
        //log
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "EndF"
        )

        return nativeAdvanceManager2
    }

    private fun loadNative(
        manager: NativeAdvanceManager2?,
        frAd: FrameLayout,
        nativeModel: RemoteConfigAdNativeModel,
        nativeCallback: NativeCallback? = null,
    ) {

        //get layout in app
        val nativeLayoutFile = nativeModel.adLayoutFile ?: ""
        val nativeLayout: NativeLayoutInapp? = nativeLayoutFile.let {
            NativeAdvanceLayoutManager.getNativeLayoutInApp(
                it
            )
        }

        //adjust layout bg_color and border_color
        val drawable = getDrawableFile(nativeModel)
        //show ad
        nativeLayout?.let {
            manager?.loadAndShowNative(
                frAd,
                it.layoutNativeId,
                it.shimmerLayoutId,
                drawable,
                nativeCallback
            )
        }
    }

    private fun canLoadNative(
        rmcfKey: String,
        nativeModel: RemoteConfigAdNativeModel,
        context: Context,
        nativeCallback: NativeCallback? = null,
    ): Boolean {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        //check version not show ad
        if (!checkShowAdByVersion()) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNotShVer"
            )
            nativeCallback?.onNotLoad()
            return false
        }

        //check not show setting
        //must check here, not check in lib because not show shimmer loading
        if (nativeModel.isShow == false) {
            FirebaseLogEventUtils.logEventAdCommon(
                context, logEventAdName,
                logEventFunctionName, "NGNotSh"
            )
            nativeCallback?.onNotLoad()
            return false
        }
        return true
    }

    private fun initNativeAdvanceManager2(
        lifecycle: Lifecycle?,
        rmcfKey: String,
        nativeModel: RemoteConfigAdNativeModel,
        context: Context,
        nativeCallback: NativeCallback? = null,
    ): NativeAdvanceManager2? {
        val logEventAdName = makeKeyShortName(rmcfKey)
        val logEventFunctionName = "${LOG_EVENT_CLASS_NAME}_LdNsh"
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "BeginF"
        )

        Log.v(TAG, "loadAndShowAdNativeAdvance")

        if (canLoadNative(rmcfKey, nativeModel, context, nativeCallback).not()) {
            return null
        }

        // Khởi tạo nativeManager
        val nativeAdvanceManager2 = NativeAdvanceManager2(
            nativeModel.adId ?: "",
            rmcfKey,
            nativeModel.isReloadWhenResume == true,
            nativeModel.isReloadWhenVideoEnd == true,
            nativeModel.reloadTimeWhenNotVideo ?: 0,
            nativeModel.maxPreload ?: 0,
            lifecycle
        )
        configNativeManager(nativeAdvanceManager2, nativeModel)

        //log event
        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdName,
            logEventFunctionName, "ClLdNSh"
        )
        return nativeAdvanceManager2
    }

    private fun configNativeManager(
        manager: NativeAdvanceManager2?,
        nativeModel: RemoteConfigAdNativeModel,
    ) {
        //setting auto reload timer
        //ANH 20250326 disable reload timer for v1
//        nativeModel.let {
//            nativeAdvanceManager2?.setReloadTimerInfo(
//                it.is_auto_reload_timer,
//                it.ad_reload_timer_interval
//            )
//        }

        //setting auto reload timer
        //ANH 20250326 disable reload timer for v1
        nativeModel.let {
            manager?.setReloadTimerInfo(it.isAutoReloadTimer == true, it.adReloadTimerInterval ?: 0)
        }

        //setting for fan
        nativeModel.let {
            manager?.setChangeAdCtrWhenFan(
                it.isChangeDisableClickAllWhenFan == true,
                it.isChangeButtonClickOnlyWhenFan == true,
                it.adLayoutFan,
                it.bgColorFan,
                it.borderColorFan,
                it.buttonColorFan
            )
        }
    }

    fun loadAndShowNativeOrBanner(
        lifecycle: Lifecycle?,
        adType: String?,
        frAd: FrameLayout?,
        rmcfKeyNativeAdvance: String? = null,
        rmcfKeyBannerCollap: String? = null,
        rmcfKeyBannerAdaptive: String? = null,
        bannerGravity: BannerGravity? = null,
        nativeCallback: NativeCallback? = null,
        bannerCallback: BannerCallback? = null,
    ): Any? {

        if ((adType.isNullOrEmpty()) || (frAd == null))
            return null

        if (adType == "native_advance" && rmcfKeyNativeAdvance != null) {
            return loadAndShowAdNativeAdvance(
                lifecycle,
                rmcfKeyNativeAdvance,
                frAd,
                nativeCallback
            )

        } else if (adType == "banner_collapsible" && rmcfKeyBannerCollap != null && bannerGravity != null) {
            return loadAndShowAdBannerCollapse(
                lifecycle,
                rmcfKeyBannerCollap,
                frAd,
                bannerGravity,
                bannerCallback
            )

        } else if (adType == "banner_adaptive" && rmcfKeyBannerAdaptive != null && bannerGravity != null) {
            return loadAndShowAdBannerAdaptive(
                lifecycle,
                rmcfKeyBannerAdaptive,
                frAd,
                bannerGravity,
                bannerCallback
            )
        } else
            return null
    }

    /**
     * for interstitial ad
     */

    fun loadInterAd(context: Context, rmcfKey: String, interCallback: InterCallback? = null) {
        val interModel = RemoteConfigOrigin.getConfigObject(
            context,
            rmcfKey,
            RemoteConfigAdInterModel::class.java
        )
        if (interModel?.preload?.isUse == true) {
            preloadInter(interModel, interCallback)
        } else {
            loadInterManual(context, rmcfKey, interModel, interCallback)
        }
    }

    private fun loadInterManual(
        context: Context,
        rmcfKey: String,
        interModel: RemoteConfigAdInterModel?,
        interCallback: InterCallback? = null,
    ) {
        if (interModel == null) {
            interCallback?.onNextAction()
            return
        }

        if (interModel.adId.isNullOrBlank()) {
            interCallback?.onNextAction()
            return
        }

        if (interModel.isShow != true) {
            interCallback?.onNextAction()
            return
        }

        if (!checkShowAdByVersion()) {
            interCallback?.onNextAction()
            return
        }
        interModel.let {
            InterManager.loadInter(
                context,
                rmcfKey,
                it.adId ?: "",
                interModel.isShow,
                interModel.adMode ?: "",
                interModel.isPreloadAfterShowAd == true,
                interModel.isShowNextActivityBeforeAd == true,
                interCallback
            )
        }
    }

    internal fun preloadInter(
        model: RemoteConfigAdInterModel,
        interCallback: InterCallback? = null,
    ) {
        val preloadModel = model.toInterPreloadModel()
        InterPreload.startLoad(preloadModel, interCallback)
    }

    internal fun showPreloadInter(
        activity: Activity,
        rmcf: String,
        model: RemoteConfigAdInterModel,
        intentNextScreen: Intent? = null,
        interCallback: InterCallback? = null,
    ) {
        val preloadModel = model.toInterPreloadModel()
        InterPreload.setIntentNextScreen(intentNextScreen)
        InterPreload.showPreloadedAd(activity, rmcf, preloadModel, interCallback)
    }

    fun showInter(
        activity: Activity,
        rmcfKey: String,
        intentNextScreen: Intent?,
        interCallback: InterCallback? = null,
    ) {
        val interModel = RemoteConfigOrigin.getConfigObject(
            activity,
            rmcfKey,
            RemoteConfigAdInterModel::class.java
        )
        if (interModel?.preload?.isUse == true) {
            showPreloadInter(activity, rmcfKey, interModel, intentNextScreen, interCallback)
        } else {
            showInterAd(activity, rmcfKey, intentNextScreen, interCallback)
        }
    }

    /**
     * @param intentNextScreen: intent sang màn kế tiếp, không start activity trong
     * onNextAction nữa, trong onNextAction sẽ xử lý các logic bổ sung khác
     */
    private fun showInterAd(
        activity: Activity,
        rmcfKey: String,
        intentNextScreen: Intent?,
        interCallback: InterCallback? = null,
    ) {
        //check params in InterManager
        //no check outsize because if true can not execute callback
        InterManager.setIntentNextScreen(intentNextScreen)
        InterManager.showInter(activity, rmcfKey, interCallback)
    }

    fun loadInterFan(context: Context, rmcfKey: String, interCallback: InterFanCallback? = null) {
        //inter ad loading
        val interModel = RemoteConfigOrigin.getConfigObject(
            context,
            rmcfKey,
            RemoteConfigAdInterModel::class.java
        )

        val canNotLoadAd =
            interModel == null || interModel.fan?.adId.isNullOrBlank() || interModel.fan?.isUse != true || interModel.isShow == false
        if (canNotLoadAd || !checkShowAdByVersion()) {
            interCallback?.onNextAction()
            return
        }
        requireNotNull(interModel)
        InterFanManager.loadInter(
            context,
            rmcfKey,
            requireNotNull(interModel.fan?.adId),
            interModel.isShow ?: true,
            interModel.adMode ?: "",
            interCallback
        )
    }

    /**
     * @param intentNextScreen: intent sang màn kế tiếp, không start activity trong
     * onNextAction nữa, trong onNextAction sẽ xử lý các logic bổ sung khác
     */
    fun showInterFan(
        activity: Activity,
        rmcfKey: String,
        intentNextScreen: Intent?,
        interCallback: InterFanCallback? = null,
    ) {
        InterFanManager.setIntentNextScreen(intentNextScreen)
        InterFanManager.showInter(activity, rmcfKey, interCallback)
    }

//    fun loadNShowInterAd(
//        activity: Activity,
//        rmcfKey: String,
//        interCallback: InterCallback? = null
//    ) {
//        Log.v(TAG, "loadNShowInterAd function")
//        val showAdInterCallback: InterCallback = object : InterCallback {
//            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                showInterAd(activity, rmcfKey, interCallback)
//                interCallback?.onAdLoaded(interstitialAd)
//            }
//
//            override fun onNextAction() {
//                interCallback?.onNextAction()
//            }
//        }
//        Log.v(TAG, "loadInterAd")
//
//        loadInterAd(activity, rmcfKey, showAdInterCallback)
//    }

    /**
     * get drawable from remote config
     * using for adjust layout ad native
     */
    private fun getDrawableFile(remoteConfigAdNativeModel: RemoteConfigAdNativeModel): GradientDrawable? {

        try {
            var drawable: GradientDrawable? = null
            drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(remoteConfigAdNativeModel.bgColor.toColor())
                setStroke(1, remoteConfigAdNativeModel.borderColor.toColor())
            }
            return drawable
        } catch (e: Exception) {
            return null
        }
    }

    fun checkShowAdByVersion() = AdsHelper.checkShowAdByVersion()

    /**
     * for RewardAd
     */
    fun loadRewardAd(
        activity: Activity,
        remoteConfigKey: String,
        rewardCallback: RewardCallback? = null,
    ) {
        try {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "request_load_reward")

            val rewardModel = RemoteConfigOrigin.getConfigObject(
                activity,
                remoteConfigKey,
                RemoteConfigAdRewardModel::class.java
            )

            Log.d(
                RewardManager.TAG,
                "The rewarded_${rewardModel?.adName} _${rewardModel?.isShow}"
            )

            if (rewardModel == null || rewardModel.adId.isNullOrBlank() || rewardModel.isShow == false) {
                rewardCallback?.onNotLoad()
                FirebaseLogEventUtils.logEventConfigModelNull(activity, "adCommon", remoteConfigKey)
                return
            }

            if (!checkShowAdByVersion()) {
                rewardCallback?.onNotLoad()
                return
            }

            RewardManager.initializeRewardAd(
                activity,
                rewardModel.adId,
                rewardModel.adName ?: "",
                rewardCallback
            )
        } catch (e: Exception) {
            rewardCallback?.onNotLoad()
            Log.e(RewardManager.TAG, "Error loading reward ad ${e.message}")
        }
    }

    fun showRewardAd(rewardCallback: RewardCallback? = null) {
        RewardManager.showRewardAd(rewardCallback)
    }


    fun rewardAdAndShow(
        activity: Activity,
        remoteConfigKey: String,
        rewardCallback: RewardCallback? = null,
    ) {
        try {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "request_loadNShow_reward")

            val rewardModel = RemoteConfigOrigin.getConfigObject(
                activity,
                remoteConfigKey,
                RemoteConfigAdRewardModel::class.java
            )

            Log.d(
                RewardManager.TAG,
                "The rewarded_${rewardModel?.adName} _${rewardModel?.isShow}"
            )

            if (rewardModel == null || rewardModel.adId.isNullOrBlank() || rewardModel.isShow == false) {
                rewardCallback?.onNotLoad()
                FirebaseLogEventUtils.logEventConfigModelNull(activity, "adCommon", remoteConfigKey)
                return
            }

            if (!checkShowAdByVersion()) {
                rewardCallback?.onNotLoad()
                Log.e(RewardManager.TAG, "not show ad version")
                return
            }

            RewardManager.initializeLoadAndShow(
                activity,
                rewardModel.adId,
                rewardModel.adName ?: "",
                rewardCallback
            )
        } catch (e: Exception) {
            rewardCallback?.onNotLoad()
            Log.e(RewardManager.TAG, "Error loading rewarded interstitial ad ${e.message}")
        }
    }


    /**
     * for RewardAdInterstitialAd
     */
    fun loadRewardAdInterstitialAd(
        activity: Activity,
        remoteConfigKey: String,
        rewardCallback: RewardCallback? = null,
    ) {
        try {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "request_load_reward_inter")

            val rewardModel = RemoteConfigOrigin.getConfigObject(
                activity,
                remoteConfigKey,
                RemoteConfigAdRewardModel::class.java
            )

            Log.d(
                RewardInterstitialAdManager.TAG,
                "The rewarded_inter_${rewardModel?.adName} _${rewardModel?.isShow}"
            )

            if (rewardModel == null || rewardModel.adId.isNullOrBlank() || rewardModel.isShow == false) {
                rewardCallback?.onNotLoad()
                FirebaseLogEventUtils.logEventConfigModelNull(activity, "adCommon", remoteConfigKey)
                return
            }

            if (!checkShowAdByVersion()) {
                rewardCallback?.onNotLoad()
                return
            }

            RewardInterstitialAdManager.initialize(
                activity,
                rewardModel.adId,
                rewardModel.adName ?: "",
                rewardCallback
            )
        } catch (e: Exception) {
            rewardCallback?.onNotLoad()
            Log.e(
                RewardInterstitialAdManager.TAG,
                "Error loading rewarded interstitial ad ${e.message}"
            )
        }
    }

    fun showRewardAdInterstitialAd(rewardCallback: RewardCallback? = null) {
        RewardInterstitialAdManager.showRewardInterstitialAd(rewardCallback)
    }


    fun rewardAdInterstitialAdAndShow(
        activity: Activity,
        remoteConfigKey: String,
        rewardCallback: RewardCallback? = null,
    ) {
        try {
            FirebaseLogEventUtils.logEventCommonWithName(activity, "request_loadNShow_reward_inter")

            val rewardModel = RemoteConfigOrigin.getConfigObject(
                activity,
                remoteConfigKey,
                RemoteConfigAdRewardModel::class.java
            )

            Log.d(
                RewardInterstitialAdManager.TAG,
                "The rewarded_inter_${rewardModel?.adName} _${rewardModel?.isShow}"
            )

            if (rewardModel == null || rewardModel.adId.isNullOrBlank() || rewardModel.isShow == false) {
                rewardCallback?.onNotLoad()
                FirebaseLogEventUtils.logEventConfigModelNull(activity, "adCommon", remoteConfigKey)
                return
            }

            if (!checkShowAdByVersion()) {
                rewardCallback?.onNotLoad()
                return
            }

            RewardInterstitialAdManager.initializeLoadAndShow(
                activity,
                rewardModel.adId,
                rewardModel.adName ?: "",
                rewardCallback
            )
        } catch (e: Exception) {
            rewardCallback?.onNotLoad()
            Log.e(
                RewardInterstitialAdManager.TAG,
                "Error loading rewarded interstitial ad ${e.message}"
            )
        }
    }


}