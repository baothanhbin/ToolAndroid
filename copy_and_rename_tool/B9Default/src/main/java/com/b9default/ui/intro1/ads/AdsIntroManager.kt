package com.b9default.ui.intro1.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.b9ads.admob.interstitialAds.base.InterCallback
import com.b9ads.admob.interstitialAds.simple.SimpleInter0
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9ads.fan.interstitialAds.base.InterFanCallback
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.LoadLocation
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.b9default.a8_app_utils.ScreenNavigate
import com.b9utils.util.FirebaseLogEventUtils
import com.b9utils.util.ShortNameEventUtils.makeKeyShortName
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

// Quản lý load và show ads trong màn intro1
object AdsIntroManager {

    private const val VER_2 = "ver2"
    private const val TAG = "B9Default_IntroActivityTag"
    private const val logEventFunctionName = "IntAct_InitNt"

    private var logEventAdNativeIntro: String = ""
    private val listNativeBot = mutableListOf<SimpleNativeAdvanceManager0?>()
    private val listNativeFull = mutableMapOf<Int, SimpleNativeAdvanceManager0?>()
    private val listNativeFullShowed = mutableMapOf<Int, SimpleNativeAdvanceManager0?>()
    private val listLoadFail = mutableListOf<Int>()
    private var screenOpenCount: Int = 0
    private var nativeBot: RemoteConfigAdNativeModel? = null
    private var screenIntro: RemoteConfigScreenIntroModel? = null

    private var nativeManagerV2: SimpleNativeAdvanceManager0? = null

    private var simpleInterIntro: SimpleInter0? = null

    private var onNativeLoadFail: (Int) -> Unit = {}

    fun destroy() {
        listNativeBot.forEach { it?.destroy() }
        listNativeFull.values.forEach { it?.destroy() }
        listNativeFullShowed.values.forEach { it?.destroy() }
        nativeManagerV2?.destroy()
        simpleInterIntro?.destroy()
    }

    fun checkNativeFullShowed(position: Int) = listNativeFullShowed[position] != null

    fun setOnNativeFullLoadFail(onNativeFail: (position: Int) -> Unit) {
        onNativeLoadFail = onNativeFail
    }

    fun getListLoadFail() = listLoadFail.toList()

    fun loadNativeFull(
        context: Context,
        nativeFull: RemoteConfigAdNativeModel?,
        screenIntro: RemoteConfigScreenIntroModel?,
    ) {
        val listIntroItem = listOfNotNull(
            screenIntro?.isSlide1Show,
            screenIntro?.isSlide2Show,
            screenIntro?.isSlide3Show,
            screenIntro?.isSlide4Show
        ).filter { it }.toMutableList()
        val pos1 = screenIntro?.adNativeFullPos1 ?: 0
        val pos2 = screenIntro?.adNativeFullPos2 ?: 0

        val listId = nativeFull?.listId?.toMutableList() ?: mutableListOf()
        val adName = nativeFull?.adName ?: ""
        if (pos1 - 1 >= 0 && pos1 <= listIntroItem.size) {
            val id = if (listId.isEmpty()) nativeFull?.adId else listId.removeAt(0)
            listIntroItem.add(pos1 - 1, true)
            loadOneNativeFull(id ?: "", adName + "_Pos1", context, nativeFull, pos1 - 1)
        }
        if (pos2 - 1 >= 0 && pos2 <= listIntroItem.size) {
            val id = if (listId.isEmpty()) nativeFull?.adId else listId.removeAt(0)
            loadOneNativeFull(id ?: "", adName + "_Pos2", context, nativeFull, pos2 - 1)
        }

    }

    fun showNativeFull(
        adFrameLayout: FrameLayout,
        position: Int,
        callback: NativeCallback? = null,
    ) {
        var nativeManager: SimpleNativeAdvanceManager0? = null
        val currentNative = listNativeFull.toMap()

        run findManager@{
            currentNative.forEach { (key, value) ->
                if (value != null) {
                    nativeManager = value
                    listNativeFull[key] = null
                    return@findManager
                }
            }
        }
        if (nativeManager == null) {
            callback?.onNotShow()
            return
        }
        nativeManager.waitAndShowNative(
            adFrameLayout.context,
            adFrameLayout,
            callback = callback
        )
        listNativeFullShowed[position] = nativeManager
    }

    fun loadInterFan(
        context: Context,
        interIntro: RemoteConfigAdInterModel?,
        location: LoadLocation,
    ) {
        if (location == interIntro?.fan?.loadAt && interIntro.fan.isUse == true) {
            AdCommon.loadInterFan(context, RemoteConfigManager.getKeyAdInterIntro())
        }
    }

    fun loadInterAdmob(activity: Activity, interIntro: RemoteConfigAdInterModel?) {
        if (interIntro?.preload?.isUse == true) {
            AdCommon.preloadInter(interIntro)
        } else {
            loadInterSimple(activity, interIntro)
        }
    }

    fun showInter(activity: Activity, interIntro: RemoteConfigAdInterModel?) {
        if (interIntro?.fan?.isUse == true) {
            showInterFan(activity)
        } else {
            showInterAdmob(activity, interIntro)
        }
    }

    fun initAdsIntro(
        lifecycle: Lifecycle,
        frameLayout: FrameLayout,
        screenOpenCount: Int,
        screenIntro: RemoteConfigScreenIntroModel? = null,
        nativeBot: RemoteConfigAdNativeModel? = null,
    ) {
        AdsIntroManager.screenOpenCount = screenOpenCount
        AdsIntroManager.screenIntro = screenIntro
        AdsIntroManager.nativeBot = nativeBot

        initNativeBot(lifecycle, frameLayout)
    }

    fun showNativeBot(position: Int, frameLayout: FrameLayout, isShowAd: Boolean) {
        val isVer2 = nativeBot?.adVer == VER_2
        val isReload = screenIntro?.isReloadAdWhenChangeSlide == true
        var realPosition = position
        run {
            listNativeFullShowed.keys.forEach {
                if (position > it) {
                    realPosition -= 1
                } else if (position < it) {
                    return@run
                }
            }
        }
        if (isVer2 && isReload && realPosition >= 0 && realPosition < listNativeBot.size) {
            if (listNativeBot[realPosition] == null) {
                frameLayout.removeAllViews()
            } else {
                listNativeBot[realPosition]?.waitAndShowNative(frameLayout.context, frameLayout)
            }
        } else if (isVer2) {
            frameLayout.isVisible = isShowAd
        }
    }

    private fun showInterAdmob(activity: Activity, interIntro: RemoteConfigAdInterModel?) {
        if (interIntro?.preload?.isUse == true) {
            showInterPreload(activity, interIntro)
        } else {
            showInterSimple(activity)
        }
    }

    private fun loadOneNativeFull(
        adId: String,
        adName: String,
        context: Context,
        nativeFull: RemoteConfigAdNativeModel?,
        position: Int,
    ) {
        val nativeFullManager = getSimpleNative(adId, adName, nativeFull, object : NativeCallback {
            override fun onNotLoad() {
                listNativeFull[position] = null
                listLoadFail.add(position)
                onNativeLoadFail(position)
                Log.v(TAG, "native intro full onNotLoad")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                listNativeFull[position] = null
                listLoadFail.add(position)
                onNativeLoadFail(position)
                Log.v(TAG, "native intro full load Fail: $position $p0")
            }
        })
        listNativeFull[position] = nativeFullManager
        nativeFullManager?.loadNative(context, null, false)
    }

    private fun loadInterSimple(
        activity: Activity,
        interIntro: RemoteConfigAdInterModel?,
    ) {
        val nextScreenFull = screenIntro?.nextScreenDefault ?: "app@home"

        val interCallback: InterCallback = object : InterCallback {

            override fun onCancelToShow() {
                super.onCancelToShow()
                ScreenNavigate.nextScreenAndFinishCurrent(activity, nextScreenFull)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                ScreenNavigate.nextScreenAndFinishCurrent(activity, nextScreenFull)
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                ScreenNavigate.nextScreenAndFinishCurrent(
                    activity,
                    nextScreenFull,
                    withNativeFull = true,
                    remoteConfigKeyInter = RemoteConfigManager.getKeyAdInterIntro()
                )
            }
        }

        //inter intro
        simpleInterIntro = SimpleInter0(interIntro?.adId ?: "", interIntro?.adName ?: "")
        simpleInterIntro?.setProperties(
            interIntro?.isShow ?: true,
            interIntro?.isShowNextActivityBeforeAd == true,
            interCallback
        )

        simpleInterIntro?.loadInter(activity, false)
    }

    private fun showInterFan(
        activity: Activity,
    ) {
        val nextScreenFull = screenIntro?.nextScreenDefault ?: "app@home"
        val intent = ScreenNavigate.getIntent(activity.applicationContext, nextScreenFull)
        AdCommon.showInterFan(
            activity,
            RemoteConfigManager.getKeyAdInterIntro(),
            intent,
            object : InterFanCallback {
                override fun onNextAction() {
                    activity.finish()
                }
            })
    }

    private fun showInterSimple(
        activity: Activity,
    ) {
        simpleInterIntro?.showInter(activity)
    }

    private fun showInterPreload(activity: Activity, interIntro: RemoteConfigAdInterModel) {
        val nextScreenFull = screenIntro?.nextScreenDefault ?: "app@home"
        AdCommon.showPreloadInter(
            activity,
            RemoteConfigManager.getKeyAdInterIntro(),
            interIntro,
            null,
            object : InterCallback {
                override fun onCancelToShow() {
                    super.onCancelToShow()
                    ScreenNavigate.nextScreenAndFinishCurrent(activity, nextScreenFull)
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    ScreenNavigate.nextScreenAndFinishCurrent(activity, nextScreenFull)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    ScreenNavigate.nextScreenAndFinishCurrent(activity, nextScreenFull)
                }
            }
        )
    }

    private fun initNativeBot(
        lifecycle: Lifecycle,
        frameLayout: FrameLayout,
    ) {
        logEventAdNativeIntro = makeKeyShortName(nativeBot?.adName ?: "ad_native_intro")

        val isVer2 = nativeBot?.adVer == VER_2
        val isReload = screenIntro?.isReloadAdWhenChangeSlide == true
        when {
            isVer2 && isReload -> initNativeBotReload(frameLayout)
            isVer2 -> initNativeNotReload(frameLayout)
            else -> initNativeBotVer1(lifecycle, frameLayout)
        }
    }

    private fun initNativeBotVer1(lifecycle: Lifecycle, frameLayout: FrameLayout) {
        Log.v(TAG, "...init native ad V1 with not reload and only 1 ad id")
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeIntro(),
            frameLayout,
            null
        )
        FirebaseLogEventUtils.logEventAdCommon(
            frameLayout.context, logEventAdNativeIntro, logEventFunctionName, "ver1"
        )
        Log.v(TAG, "... call initNativeIntroV1")
    }

    private fun initNativeNotReload(frameLayout: FrameLayout) {
        val context = frameLayout.context

        Log.v(TAG, "...init native ad V2 with not reload and only 1 ad id")
        val adId = nativeBot?.adId ?: ""
        val adName = nativeBot?.adName ?: ""
        nativeManagerV2 = getSimpleNative(adId, adName, nativeBot)
        nativeManagerV2?.loadNative(context, frameLayout, true)

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdNativeIntro, logEventFunctionName, "ver2NoRe"
        )
        Log.v(TAG, "... call initNativeIntroV2NotReload")
    }

    private fun initNativeBotReload(
        frameLayout: FrameLayout,
    ) {
        val context = frameLayout.context
        listNativeBot.clear()
        listNativeBot.addAll(List(4) { null })
        val adName = nativeBot?.adName ?: ""
        val idsString = if (screenOpenCount == 1) nativeBot?.adIdL1 else nativeBot?.adIdL2
        val listId = idsString?.split(",") ?: listOf()
        if (listId.size < 4) return

        if (screenIntro?.isSlide1Show == true && screenIntro?.isSlide1AdShow == true) {
            listNativeBot[0] = getSimpleNative(listId[0], adName + "L1X", nativeBot)
        }
        if (screenIntro?.isSlide2Show == true && screenIntro?.isSlide2AdShow == true) {
            listNativeBot[1] = getSimpleNative(listId[1], adName + "L1Y", nativeBot)
        }
        if (screenIntro?.isSlide3Show == true && screenIntro?.isSlide3AdShow == true) {
            listNativeBot[2] = getSimpleNative(listId[2], adName + "L2X", nativeBot)
        }
        if (screenIntro?.isSlide4Show == true && screenIntro?.isSlide4AdShow == true) {
            listNativeBot[3] = getSimpleNative(listId[3], adName + "L2Y", nativeBot)
        }

        listNativeBot.forEachIndexed { index, nativeManager ->
            val frame = if (index == 0) frameLayout else null
            nativeManager?.loadNative(context, frame, index == 0)
        }

        FirebaseLogEventUtils.logEventAdCommon(
            context, logEventAdNativeIntro, logEventFunctionName, "ver2Re"
        )
        Log.v(TAG, "... call loadNativeIntroV2Reload")
    }

    private fun getSimpleNative(
        adId: String,
        adName: String,
        config: RemoteConfigAdNativeModel?,
        callback: NativeCallback? = null,
    ): SimpleNativeAdvanceManager0? {
        if (config == null) return null
        Log.v(TAG, "...init simple native ad obj")
        val manager = SimpleNativeAdvanceManager0(adId, adName)
        manager.setProperties(
            config.isShow ?: true,
            config.adLayoutFile ?: "",
            config.bgColor,
            config.borderColor,
            callback
        )

        manager.setPropertiesForFan(
            config.isChangeButtonClickOnlyWhenFan == true,
            config.isChangeDisableClickAllWhenFan == true,
            config.adLayoutFan ?: "",
            config.bgColorFan ?: "",
            config.borderColorFan ?: "",
            config.buttonColorFan ?: "",
        )
        return manager
    }
}
