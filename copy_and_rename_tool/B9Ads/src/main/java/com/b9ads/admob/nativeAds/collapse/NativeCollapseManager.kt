package com.b9ads.admob.nativeAds.collapse

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import com.b9ads.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.b9ads.admob.nativeAds.collapse.model.NativeModel
import com.b9ads.custom.nativeCollapse.NativeCollapseContainer

class NativeCollapseManager(
    private val model: NativeModel,
    lifecycle: Lifecycle? = null
): NativeAdvanceManager2(
    model.adId,
    model.adName,
    model.isReloadWhenResume,
    model.isReloadWhenVideoEnd,
    model.reloadTimeWhenNotVideo,
    model.maxPreload,
    lifecycle
) {

    override fun addShimmer(shimmer: View?) {
        (frameLayout as? NativeCollapseContainer)?.resetAllState()
        super.addShimmer(shimmer)
        (frameLayout as? NativeCollapseContainer)?.let {
            it.enableDrag(model.showBtnClose.not())
            it.setClosePosition(model.closePosition)
            if (model.showBtnClose) {
                showBtnCloseAfter(model.delayShowBtn)
            }
        }
    }

    /**
     * Hiển thị nút đóng sau một khoảng thời gian
     * @param time Thời gian (tính bằng giây) sau đó nút đóng sẽ hiển thị
     */
    private fun showBtnCloseAfter(time: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            (frameLayout as? NativeCollapseContainer)?.showBtnClose()
        }, time * 1000L)
    }
}
