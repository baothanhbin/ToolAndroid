package com.b9ads.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.b9ads.R
import com.b9ads.databinding.AdsDialogLoadingBinding

class LoadingAdsDialog(context: Context) : Dialog(context, R.style.LoadingTheme) {

    private val binding by lazy { AdsDialogLoadingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setContentView(binding.root)
        fullScreen()
    }

    private fun fullScreen() {
        try {
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, true)
                WindowInsetsControllerCompat(it, binding.root).let { controller ->
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
