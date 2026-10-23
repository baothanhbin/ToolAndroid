package com.b9default.ui.update

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.view.tap
import com.b9default.a8_app_utils.SystemUtil
import com.b9ui.R
import com.b9ui.databinding.DialogBottomSheetUpdateBinding
import com.b9utils.util.appupdate.AppUpdateManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppUpdateBottomSheetDialog : BottomSheetDialogFragment() {
    private val binding by lazy {
        DialogBottomSheetUpdateBinding.inflate(layoutInflater)
    }
    private var nativeAdvanceManager2: NativeAdvanceManager2? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        val bottomSheet =
            (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            BottomSheetBehavior.from(it).apply {
                isDraggable = false
            }
        }
        nativeAdvanceManager2 = AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigManager.getKeyAdNativeAppUpdate(),
            binding.frAd,
            null
        )

        binding.btnSubmit.tap {
            SystemUtil.openAppInPlayStore(requireContext())

            AppOpenResumeManager.setEnableAdsResume(false)
        }
        binding.btnCancel.tap {
            dialog?.dismiss()
        }
        if (AppUpdateManager.checkUpdate == 2) {
            binding.btnCancel.visibility = View.INVISIBLE
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
                        binding.tvContent.text =
                            binding.tvContent.text.toString() + AppUpdateManager.updateContent
                    }
                }
            }
        }


    }


}
