package com.b9default.ui.language

import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenLanguageStartModel
import com.b9default.a8_app_utils.SystemUtil
import com.b9default.ui.language.adapter.LanguageAdapter
import com.b9default.ui.language.viewModel.LanguageViewModel
import com.b9ui.databinding.ActivityLanguageSettingBinding
import com.b9utils.util.remoteconfig.RemoteConfigOrigin

class LanguageSettingActivity : BaseActivity<ActivityLanguageSettingBinding, LanguageViewModel>() {

    private val languageAdapter by lazy { LanguageAdapter() }
    private var remoteConfigLanguageStart: RemoteConfigScreenLanguageStartModel? = null

    override fun createBinding(): ActivityLanguageSettingBinding {
        return ActivityLanguageSettingBinding.inflate(layoutInflater)
    }

    override fun setViewModel() = LanguageViewModel()

    override fun initView() {
        super.initView()
        remoteConfigLanguageStart = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteConfigManager.getKeyScreenLanguageStart(),
            RemoteConfigScreenLanguageStartModel::class.java
        )

        binding.rvLanguage.adapter = languageAdapter
        setUpViewModel()
    }

    override fun bindView() {
        super.bindView()

        binding.ivCheck.setOnClickListener {
            nextActivityProcess()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        languageAdapter.setOnItemClick {
            // update UI selected
            viewModel.selectLang(it)
        }
    }

    private fun setUpViewModel() {
        viewModel.language.observe(this) {
            languageAdapter.submitList(it)
        }

        viewModel.getLanguageList(
            languageStartModel = remoteConfigLanguageStart,
            selectedCode = SystemUtil.getPreLanguage(this).toString(),
            selectedRegionName = SystemUtil.getRegionName(this)
        )
    }

    private fun nextActivityProcess() {
        SystemUtil.saveLocale(this@LanguageSettingActivity, viewModel.getSelectedCode())
        SystemUtil.saveRegionName(this@LanguageSettingActivity, viewModel.getSelectedRegionName())

        //check next screen
        val nextScreenAppIntent = SystemUtil.getNextActivityFromModuleToApp(
            "home",
            "HomeActivity"
        )
        nextScreenAppIntent.putExtra("source", "lib@${this::class.java.simpleName}")
        startActivity(nextScreenAppIntent)

        finishAffinity()
    }
}