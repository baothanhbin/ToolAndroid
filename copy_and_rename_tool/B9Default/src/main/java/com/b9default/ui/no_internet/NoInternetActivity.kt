/**
 *hien tai neu khong co intenet den man hinh nointernet\
 * sau do co internet thi se bat lai app tu man splash cho du truoc do o man hinh khac
 */


package com.b9default.ui.no_internet

import android.content.Intent
import android.provider.Settings
import com.b9default.a1_common_utils.base.BaseActivity
import com.b9default.a1_common_utils.base.BaseViewModel
import com.b9ui.databinding.ActivityNoInternetBinding
import com.b9default.ui.splash.SplashActivity
import com.b9utils.util.CheckInternet


class NoInternetActivity : BaseActivity<ActivityNoInternetBinding, BaseViewModel>() {
    override fun createBinding(): ActivityNoInternetBinding {
        return ActivityNoInternetBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()

        binding.tvRetry.setOnClickListener {
            if (CheckInternet.haveNetworkConnection(this)) {
                startActivity(Intent(this, SplashActivity::class.java))
            } else {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        if (CheckInternet.haveNetworkConnection(this)) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }
}