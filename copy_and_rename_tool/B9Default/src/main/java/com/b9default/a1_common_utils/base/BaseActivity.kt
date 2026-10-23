package com.b9default.a1_common_utils.base

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9default.a8_app_utils.SystemUtil
import com.b9utils.util.FirebaseLogEventUtils

abstract class BaseActivity<VB : ViewBinding, V : ViewModel> : AppCompatActivity() {
    protected lateinit var binding: VB
    protected lateinit var viewModel: V

    protected open val belowStatusBar = true
    protected open val statusBarTextWhite = false
    protected open val ignoreImeInset = true

    abstract fun createBinding(): VB
    abstract fun setViewModel(): V

    protected open fun initView() {}
    protected open fun bindView() {}

    open fun bindViewModel() {}

    //init ad
    protected open fun initAd() {}

    //adjust layout
    protected open fun adjustLayout() {}

    override fun hasWindowFocus(): Boolean {
        hideNavigationBar()
        return super.hasWindowFocus()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ) {
                statusBarTextWhite
            })
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onCreate")

        super.onCreate(savedInstanceState)

        SystemUtil.setLocale(this)
        binding = createBinding()
        setContentView(binding.root)
        viewModel = setViewModel()
        initView()
        bindView()
        bindViewModel()

        //AD implement
        initAd()

        //hide or show navigation bar
        hideNavigationBar()
        calculateInsets()

        //adjustScreenPortrait
        adjustScreenPortrait(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //adjust layout
        adjustLayout()
    }


    override fun onResume() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onResume")
        super.onResume()
        //re enable app open resume ad
        AppOpenResumeManager.setEnableAdsResume(AppOpenResumeManager.getIsShow())
    }

    override fun onStop() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onStop")
        super.onStop()

    }

    override fun onDestroy() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onDes")
        super.onDestroy()
    }

    override fun onPause() {
        //log event
        FirebaseLogEventUtils.logEventScreen(this, "onPause")
        super.onPause()
    }

    //confirm xu ly source code outdated
    protected open fun hideNavigationBar() {
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    //force screen portrait
    protected open fun adjustScreenPortrait(screenType: Int) {
        requestedOrientation = screenType
    }

    private fun calculateInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val statusBarsInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarVisible = insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            val navBarsInsets = if (navBarVisible) {
                insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            } else {
                androidx.core.graphics.Insets.NONE
            }
            val insetTop = if (belowStatusBar) statusBarsInsets.top else 0
            val insetBottom =
                if (imeVisible && ignoreImeInset.not()) imeInsets.bottom else navBarsInsets.bottom
            v.setPadding(0, insetTop, 0, insetBottom)
            insets
        }
    }
}