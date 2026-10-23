package com.b9default.ui.intro1

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.model.Position
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.ads.LoadLocation
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.b9default.ui.intro1.adapter.IntroAdapter1
import com.b9default.ui.intro1.ads.AdsIntroManager
import com.b9default.ui.intro1.model.IntroModel1
import com.b9default.ui.intro1.viewModel.IntroViewModel
import com.b9ui.databinding.ActivityIntro1Binding
import com.b9utils.util.SystemSharePreferenceUtils
import com.b9utils.util.ext.toDp
import com.b9utils.util.remoteconfig.RemoteConfigOrigin

/** Intro1 chứa logic load ads mới, đưa thêm vị trí nút next
 *  lêm remote config, vuốt hay ấn để tắt remote config
 */
class IntroActivity1 : BaseActivity<ActivityIntro1Binding, IntroViewModel>() {
    private val tag = "B9Default_IntroActivityTag"

    //screen open count
    private var screenOpenCountKey: String = "intro_open_count"
    private var screenOpenCount: Int = 0

    //screen intro model
    private var remoteConfigScreenIntroModel: RemoteConfigScreenIntroModel? = null
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null
    private var remoteConfigNativeIntroModel: RemoteConfigAdNativeModel? = null
    private var remoteConfigInterIntroModel: RemoteConfigAdInterModel? = null

    //intro adapter
    private val introAdapter by lazy { IntroAdapter1() }

    override fun createBinding() = ActivityIntro1Binding.inflate(layoutInflater)

    override fun setViewModel() = IntroViewModel()

    override fun initView() {
        super.initView()
        Log.v(tag, "function initView IntroActivity1")
        binding.vpIntro.adapter = introAdapter
        binding.indicator.attachTo(binding.vpIntro)

        //open count
        screenOpenCount = SystemSharePreferenceUtils.getInt(this, this.screenOpenCountKey)
        screenOpenCount += 1
        SystemSharePreferenceUtils.setInt(this, this.screenOpenCountKey, screenOpenCount)
        Log.v(tag, "...screen open count $screenOpenCount")

        //lấy remote config AppAll
        getRemoteConfigModelData()
        visibleTvNext(remoteConfigScreenIntroModel?.btnNextConfig?.isShow)
    }

    override fun bindView() {
        //action change slide
        binding.vpIntro.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val item = introAdapter.currentList[position]
                //custom button next or textview next
                configView(item)
                //ad processing
                AdsIntroManager.showNativeBot(position, binding.frAdBottom, item.isShowNativeBot)
                // disable swipe
                if (item.isNativeFull) {
                    binding.vpIntro.isUserInputEnabled = item.showBtnClose.not()
                } else {
                    binding.vpIntro.isUserInputEnabled = true
                }
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

        introAdapter.setOnShowNativeFail {
            viewModel.removeNativeFull(it)
        }

        introAdapter.setOnNextSlide {
            doNextSlide()
        }
    }

    override fun bindViewModel() {
        viewModel.itemIntro.observe(this) {
            introAdapter.submitList(it)
        }

        viewModel.getIntroItem(remoteConfigScreenIntroModel)
    }

    override fun initAd() {
        AdsIntroManager.initAdsIntro(
            lifecycle,
            binding.frAdBottom,
            screenOpenCount,
            remoteConfigScreenIntroModel,
            remoteConfigNativeIntroModel
        )
        loadInter(remoteConfigInterIntroModel)

        // remove item native full when load fail
        AdsIntroManager.setOnNativeFullLoadFail {
            viewModel.removeNativeFull(AdsIntroManager.getListLoadFail())
        }

        viewModel.removeNativeFull(AdsIntroManager.getListLoadFail())
    }

    private fun doNextSlide() {
        val currentPosition = binding.vpIntro.currentItem
        if (currentPosition + 1 < introAdapter.itemCount) {
            binding.vpIntro.setCurrentItem(currentPosition + 1, true)
        } else {
            lastSlideNextProcess()
        }
    }

    private fun loadInter(interModel: RemoteConfigAdInterModel?) {
        if (interModel?.fan?.isUse == true) {
            AdsIntroManager.loadInterFan(this, interModel, LoadLocation.INTRO)
        } else {
            AdsIntroManager.loadInterAdmob(this, interModel)
        }
    }

    override fun hideNavigationBar() {
        if (remoteConfigScreenIntroModel?.isHideNaviMenu == true)
            return super.hideNavigationBar()
        else
            return
    }

    private fun visibleTvNext(isShow: Boolean?) {
        if (isShow != false) {
            binding.tvNext.visibility = View.VISIBLE
        } else {
            // hide text view and center dot
            binding.tvNext.visibility = View.INVISIBLE
            centerDot()
        }
    }

    private fun centerDot() {
        val dotParams = binding.indicator.layoutParams as? ConstraintLayout.LayoutParams
        dotParams?.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        dotParams?.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        binding.indicator.layoutParams = dotParams
    }

    private fun configView(
        introModel: IntroModel1,
    ) {
        if (introModel.isNativeFull) {
            binding.btnNext.isVisible = false
            binding.tvNext.isVisible = false
            binding.indicator.isVisible = false
            binding.frAdBottom.isVisible = false
            return
        } else {
            binding.indicator.isVisible = true
            binding.frAdBottom.isVisible = true
        }
        binding.btnNext.isVisible = introModel.showBtnNextLarge
        if (introModel.showBtnNextLarge) {
            visibleTvNext(false)
        } else {
            alignBtnNext()
            visibleTvNext(remoteConfigScreenIntroModel?.btnNextConfig?.isShow)
        }
    }

    //adjust distance button vs ad by firebase config
    override fun adjustLayout() {
        alignBtnNext()

        //font size
        val textSize: Float = remoteConfigScreenIntroModel?.fontSize ?: 20F
        binding.tvNext.textSize = textSize

        // bot margin
        val distance: Int = remoteConfigScreenIntroModel?.distanceButtonNextVsAd ?: 0
        val paramsTvNext = binding.tvNext.layoutParams as? ViewGroup.MarginLayoutParams
        paramsTvNext?.setMargins(
            paramsTvNext.leftMargin,
            paramsTvNext.topMargin,
            paramsTvNext.rightMargin,
            distance.toDp().toInt()
        )

    }

    private fun alignBtnNext() {
        val tvNextParams = binding.tvNext.layoutParams as? ConstraintLayout.LayoutParams
        val dotParams = binding.indicator.layoutParams as? ConstraintLayout.LayoutParams
        tvNextParams?.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        tvNextParams?.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        dotParams?.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        dotParams?.startToStart = ConstraintLayout.LayoutParams.PARENT_ID

        when (remoteConfigScreenIntroModel?.btnNextConfig?.position) {
            Position.LEFT -> moveBtnNextToLeft(tvNextParams, dotParams)
            Position.CENTER -> moveBtnNextToCenter(dotParams)
            else -> moveBtnNextToRight(tvNextParams, dotParams)
        }

        binding.tvNext.layoutParams = tvNextParams
        binding.indicator.layoutParams = dotParams
    }

    private fun moveBtnNextToLeft(
        tvNextParams: ConstraintLayout.LayoutParams?,
        dotParams: ConstraintLayout.LayoutParams?,
    ) {
        tvNextParams?.endToEnd = ConstraintLayout.LayoutParams.UNSET
        dotParams?.startToStart = ConstraintLayout.LayoutParams.UNSET
    }

    private fun moveBtnNextToRight(
        tvNextParams: ConstraintLayout.LayoutParams?,
        dotParams: ConstraintLayout.LayoutParams?,
    ) {
        tvNextParams?.startToStart = ConstraintLayout.LayoutParams.UNSET
        dotParams?.endToEnd = ConstraintLayout.LayoutParams.UNSET
    }

    private fun moveBtnNextToCenter(dotParams: ConstraintLayout.LayoutParams?) {
        dotParams?.endToEnd = ConstraintLayout.LayoutParams.UNSET
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

        remoteConfigInterIntroModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyAdInterIntro(),
            RemoteConfigAdInterModel::class.java
        )
    }

    private fun lastSlideNextProcess() {
        //last slide
        Log.v(tag, "next process in last slide ${binding.vpIntro.currentItem}")
        AdsIntroManager.showInter(this, remoteConfigInterIntroModel)
    }

    override fun onDestroy() {
        AdsIntroManager.destroy()
        super.onDestroy()
    }
}