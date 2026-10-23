package com.b9default.ui.ads

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.view.isVisible
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9ads.admob.nativeAds.advance.simple.SimpleNativeAdvanceManager0
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdInterModel
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.b9default.a1_common_utils.view.tap
import com.b9ui.databinding.ItemNativeFullBinding
import com.b9utils.util.ext.getParcel
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.google.android.gms.ads.LoadAdError

/**
 * Màn hiển thị native full sau khi show inter
 * Đầu vào KEY_NEXT_INTENT: intent sang màn kế
 */
class NativeFullActivity : BaseActivity<ItemNativeFullBinding, BaseViewModel>() {

    private var nativeFullModel: RemoteConfigAdNativeModel? = null
    private var intentNext: Intent? = null

    override fun createBinding() = ItemNativeFullBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        intentNext = intent.getParcel<Intent>(KEY_NEXT_INTENT)
        val nativeRemoteConfigKey = intent.getStringExtra(KEY_INTER) ?: ""
        val interModel = RemoteConfigOrigin.getConfigObject(
            this,
            nativeRemoteConfigKey,
            RemoteConfigAdInterModel::class.java
        )
        if (interModel?.nativeAfter.isNullOrBlank()) {
            nextScreen()
            return
        }
        nativeFullModel = RemoteConfigOrigin.getConfigObject(
            this,
            interModel.nativeAfter,
            RemoteConfigAdNativeModel::class.java
        )
        if (nativeFullModel == null) {
            nextScreen()
            return
        }
        visibleBtnClose(false)
        loadNativeAds()
    }

    override fun bindView() {
        binding.btnClose.tap {
            nextScreen()
        }
    }

    private fun nextScreen() {
        intentNext?.let {
            startActivity(it)
        }
        finish()
    }

    private fun visibleBtnClose(isVisible: Boolean = true) {
        binding.btnClose.isVisible = isVisible
    }

    private fun loadNativeAds() {
        val callback = object : NativeCallback {
            override fun onNotLoad() {
                super.onNotLoad()
                nextScreen()
            }

            override fun onNotShow() {
                super.onNotShow()
                nextScreen()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                visibleBtnClose()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                nextScreen()
            }
        }
        val nativeManager = getSimpleNative(nativeFullModel, callback)
        nativeManager?.loadNative(this, binding.frAds, true)
    }

    private fun getSimpleNative(
        config: RemoteConfigAdNativeModel?,
        callback: NativeCallback? = null,
    ): SimpleNativeAdvanceManager0? {
        if (config == null) return null
        Log.v(TAG, "...init ads manager id")
        val manager = SimpleNativeAdvanceManager0(config.adId ?: "", config.adName ?: "")
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

    companion object {
        private const val TAG = "NativeFullActivity_TAG"
        const val KEY_NEXT_INTENT = "KEY_NEXT_INTENT"
        const val KEY_INTER = "KEY_INTER"

        fun getIntent(context: Context, rmcfInter: String?, nextIntent: Intent? = null) =
            Intent(context, NativeFullActivity::class.java).apply {
                putExtra(KEY_NEXT_INTENT, nextIntent)
                putExtra(KEY_INTER, rmcfInter)
            }
    }
}
