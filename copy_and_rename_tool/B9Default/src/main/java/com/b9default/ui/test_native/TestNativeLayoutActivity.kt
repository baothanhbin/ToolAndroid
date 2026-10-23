package com.b9default.ui.test_native

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9default.a1_common_utils.ad.NativeLayoutInapp
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9ui.databinding.ActivityTestNativeLayoutBinding


class TestNativeLayoutActivity : BaseActivity<ActivityTestNativeLayoutBinding, BaseViewModel>() {

    private val nativeIDTest = "ca-app-pub-3940256099942544/2247696110"
    override fun createBinding(): ActivityTestNativeLayoutBinding {
        return ActivityTestNativeLayoutBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        //addButtonAndFrameAd(NativeLayoutInapp.APP_ADS_NATIVE_DEFAULT)

        for (layout in NativeLayoutInapp.values()) {
            addButtonAndFrameAd(layout)
        }
    }

    override fun hideNavigationBar() {
        return
    }

    private fun addButtonAndFrameAd(layout: NativeLayoutInapp) {


        val frameLayoutX = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val buttonA = Button(this).apply {
            text = layout.key
            setOnClickListener {
                loadAd(layout, frameLayoutX)
            }
        }


        binding.mainLinear.addView(buttonA)
        binding.mainLinear.addView(frameLayoutX)
    }


    private fun loadAd(native_layout_file: NativeLayoutInapp, frAd: FrameLayout) {

        var nativeAdvanceManager2: NativeAdvanceManager2? = null
        nativeAdvanceManager2 = NativeAdvanceManager2(
            nativeIDTest,
            "test_layout",
            false,
            false,
            15,
            0,
            lifecycle,
        )
        //get layout in app
        nativeAdvanceManager2.loadAndShowNative(
            frAd,
            native_layout_file.layoutNativeId,
            native_layout_file.shimmerLayoutId,
            null
        )
    }

}