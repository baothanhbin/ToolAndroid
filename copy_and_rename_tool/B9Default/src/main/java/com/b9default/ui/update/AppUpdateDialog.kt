/**
 * dung de goi update trong man hinh khac, ( default la man hinh main)
 */
package com.b9default.ui.update

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import com.b9default.a1_common_utils.view.tap
import com.b9ads.admob.resumeAds.AppOpenResumeManager
import com.b9ui.R
import com.b9utils.util.appupdate.AppUpdateManager
import com.b9default.a8_app_utils.SystemUtil
import com.b9default.a8_app_utils.toDp
import com.b9ui.databinding.DialogAppUpdateBinding

class AppUpdateDialog {

    fun showUpdateDialog(context: Context) {

        val layoutInflater = (context as Activity).layoutInflater
        val dialogBinding = DialogAppUpdateBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()
        val cornerRadius = 24f.toDp()
        val radii = floatArrayOf(
            cornerRadius, cornerRadius, cornerRadius, cornerRadius,
            cornerRadius, cornerRadius, cornerRadius, cornerRadius
        )
        val shape = RoundRectShape(radii, null, null)
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = Color.WHITE

        dialog.window?.setBackgroundDrawable(shapeDrawable)
        dialog.setCanceledOnTouchOutside(false)

        dialogBinding.btnSubmit.setOnClickListener {
            dialog.dismiss()
            SystemUtil.openAppInPlayStore(context)

            //log firebase
            com.b9utils.util.FirebaseLogEventUtils.logEventCommonWithName(context, "update_ok")
            AppOpenResumeManager.setEnableAdsResume(false)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
            //log firebase
            com.b9utils.util.FirebaseLogEventUtils.logEventCommonWithName(context, "update_cancel")
        }

        if (AppUpdateManager.checkUpdate == 2) {
            dialogBinding.btnCancel.visibility = View.GONE
        }

        //set text update content
        val tvContent = dialogBinding.tvContent.text.toString() + AppUpdateManager.updateContent
        dialogBinding.tvContent.text = tvContent
        dialogBinding.ivWhatNew.tap {
            if (AppUpdateManager.updateContent.isNotEmpty()) {
                if (dialogBinding.tvContent.visibility == View.VISIBLE) {
                    dialogBinding.tvContent.visibility = View.GONE
                    dialogBinding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatsnew)
                } else {
                    dialogBinding.tvContent.visibility = View.VISIBLE
                    dialogBinding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatnew_dismiss)
                    if (!dialogBinding.tvContent.text.toString()
                            .contains(AppUpdateManager.updateContent)
                    ) {
                        val tvContentTap =
                            dialogBinding.tvContent.text.toString() + AppUpdateManager.updateContent
                        dialogBinding.tvContent.text = tvContentTap

                    }
                }
            }
        }


        //ad native
//        AdCommon.loadAndShowAdNativeAdvance(context.lifecycle,
//            RemoteConfigKey.ad_native_app_update,
//            dialogBinding.frAd,
//            null)


        dialog.show()
    }
}
