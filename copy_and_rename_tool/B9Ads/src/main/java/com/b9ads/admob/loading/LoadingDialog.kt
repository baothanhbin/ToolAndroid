package com.b9ads.admob.loading

import android.content.Context
import com.b9ads.dialog.LoadingAdsDialog

open class LoadingDialog {

    private var loadingDialog: LoadingAdsDialog? = null

    protected fun showLoadingDialog(context: Context) {
        try {
            if (loadingDialog == null) {
                loadingDialog = LoadingAdsDialog(context)
            } else if (loadingDialog?.isShowing == true) {
                return
            }
            loadingDialog?.show()
        } catch (_: Exception) {
        }
    }

    protected fun hideLoadingDialog() {
        try {
            if (loadingDialog?.isShowing == true) {
                loadingDialog?.dismiss()
                loadingDialog = null
            }
        } catch (_: Exception) {
        }
    }
}
