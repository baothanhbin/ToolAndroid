/**
 * goi truoc khi vao man hinh main de update
 */

package com.b9default.ui.update

import android.view.View
import androidx.activity.OnBackPressedCallback
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.view.tap
import com.b9default.a8_app_utils.SystemUtil
import com.b9ui.R
import com.b9ui.databinding.ActivityAppUpdateBinding
import com.b9utils.util.appupdate.AppUpdateManager

class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding, BaseViewModel>() {

    override fun createBinding() = ActivityAppUpdateBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        val tvContent: String = binding.tvContent.text.toString() + AppUpdateManager.updateContent
        binding.tvContent.text = tvContent

        //btn update click
        binding.btnSubmit.tap {
            SystemUtil.openAppInPlayStore(this)
            AppOpenResumeManager.setEnableAdsResume(false)
            com.b9utils.util.FirebaseLogEventUtils.logEventCommonWithName(this, "update_ok")
        }

        //btn cancel
        if (AppUpdateManager.checkUpdate == 2) {
            binding.btnCancel.visibility = View.GONE

        }

        binding.btnCancel.tap {
            finish()
        }
        binding.ivWhatNew.tap {
            if (AppUpdateManager.updateContent.isNotEmpty()) {
                if (binding.tvContent.visibility == View.VISIBLE) {
                    binding.tvContent.visibility = View.GONE
                    binding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatsnew)
                } else {
                    binding.tvContent.visibility = View.VISIBLE
                    binding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatnew_dismiss)
                    if (!binding.tvContent.text.toString()
                            .contains(AppUpdateManager.updateContent)
                    ) {
                        val tvContentTap: String =
                            binding.tvContent.text.toString() + AppUpdateManager.updateContent
                        binding.tvContent.text = tvContentTap
                    }
                }
            }
        }

    }

    override fun initAd() {
        //native
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeAppUpdate(),
            binding.frAd,
            null
        )
    }

}