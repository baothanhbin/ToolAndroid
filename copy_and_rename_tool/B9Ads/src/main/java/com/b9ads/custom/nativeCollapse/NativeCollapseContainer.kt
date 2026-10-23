package com.b9ads.custom.nativeCollapse

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import com.b9ads.custom.nativeCollapse.model.ClosePosition
import com.b9ui.R
import com.b9utils.util.ext.toDp

class NativeCollapseContainer(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    private val contentBound = RectF()
    private val icClose = AppCompatResources.getDrawable(context, R.drawable.ic_ads_close)
    private val icSize = 24f.toDp()
    private val spaceAroundIcon = 12f.toDp()
    private var icPosition = ClosePosition.TOP_RIGHT
    private var oldTouchY: Float? = null
    private val thresholdPercent = 0.3f
    private val maxChildHeightPercent = 0.47f
    private var cancelTouch = false
    private var placeInContent = false
    private var showBtnClose = false
    private var needClose = false
    private var canDrag = false


    fun enableDrag(enable: Boolean) {
        canDrag = enable
        invalidate()
    }

    fun setClosePosition(position: ClosePosition) {
        this.icPosition = position
    }

    // 12/11/2025 Cuong fix #146
    fun cancelTouch(cancel: Boolean) {
       cancelTouch = cancel
    }

    fun showBtnClose() {
        canDrag = false
        showBtnClose = true
        invalidate()
    }

    // 12/11/2025 Cuong fix #146
    fun resetAllState() {
        cancelTouch = false
        placeInContent = false
        showBtnClose = false
        needClose = false
        canDrag = false
        isVisible = true
        icPosition = ClosePosition.TOP_RIGHT
    }

    /**
     * Tính toán kích thước của các child views trong FrameLayout không
     * vượt quá maxChildHeightPercent
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxChildHeight = measuredHeight * maxChildHeightPercent

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.translationY = 0f
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams

                // Tính toán available space cho child
                val availableWidth = parentWidth - paddingLeft - paddingRight -
                        lp.leftMargin - lp.rightMargin
                val availableHeight = (maxChildHeight - lp.topMargin - lp.bottomMargin).toInt()

                // Tạo measure spec phù hợp cho từng child
                val finalWidthSpec = when (lp.width) {
                    LayoutParams.MATCH_PARENT -> MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY)
                    LayoutParams.WRAP_CONTENT -> MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST)
                    else -> MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY)
                }

                val finalHeightSpec = when (lp.height) {
                    LayoutParams.MATCH_PARENT -> MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY)
                    LayoutParams.WRAP_CONTENT -> MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST)
                    else -> MeasureSpec.makeMeasureSpec(
                        minOf(lp.height, availableHeight),
                        MeasureSpec.EXACTLY
                    )
                }

                // Measure child - ConstraintLayout sẽ tự tính toán constraints
                child.measure(finalWidthSpec, finalHeightSpec)
            }
        }

        setMeasuredDimension(
            parentWidth,
            measuredHeight
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        // Vẽ icon đóng ở góc trên bên phải
        if (showBtnClose.not() || canDrag) return
        drawCloseIcon(canvas)
    }

    fun drawCloseIcon(canvas: Canvas) {
        val child = getChildAt(0) ?: return
        val topChild = child.top + child.translationY
        val botChild = child.bottom + child.translationY
        val top = when(icPosition) {
            ClosePosition.TOP_LEFT, ClosePosition.TOP_RIGHT -> topChild + 12f.toDp()
            ClosePosition.BOTTOM_LEFT, ClosePosition.BOTTOM_RIGHT -> botChild - 36f.toDp()
        }
        val left: Float = when(icPosition) {
            ClosePosition.TOP_LEFT, ClosePosition.BOTTOM_LEFT -> 12f.toDp()
            ClosePosition.TOP_RIGHT, ClosePosition.BOTTOM_RIGHT -> measuredWidth - icSize - spaceAroundIcon
        }
        icClose?.setBounds(
            left.toInt(),
            top.toInt(),
            (left + icSize).toInt(),
            (top + icSize).toInt()
        )
        icClose?.draw(canvas)
    }


    /**
     * Đặt vị trí các view con ở dưới cùng của FrameLayout
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val parentHeight = bottom - top
        var limitTop = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childTop = parentHeight - child.measuredHeight
            if (child.isVisible) {
                limitTop = maxOf(limitTop, childTop)
                child.layout(child.left, childTop, child.right, parentHeight)
            }
        }
        contentBound.set(left.toFloat(), limitTop.toFloat(), right.toFloat(), bottom.toFloat())
    }

    /**
     * Xử lý sự kiện chạm để xác định xem có nên can thiệp vào việc kéo thả hay không
     * Nếu chạm vào vùng nội dung thì sẽ cho phép kéo thả, ngược lại thì không
     * Chỉ chặn sự kiện vuốt dọc
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return when (ev?.action) {
            MotionEvent.ACTION_DOWN -> onInterceptDown(ev)

            MotionEvent.ACTION_MOVE -> {
                val oldTouchY = this.oldTouchY
                val needMove = oldTouchY != null && ev.y - oldTouchY >= 1f.toDp()
                canDrag && placeInContent && needMove
            }

            else -> false
        }
    }

    // Xử lý sự kiện chạm, vuốt và thả tay
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (cancelTouch) {
            return false
        }
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> needClose
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> {
                onActionUp(event)
                true
            }
            else -> false
        }
    }

    private fun onActionMove(event: MotionEvent): Boolean {
        if (canDrag.not()) return false
        val oldTouchY = oldTouchY ?: return false
        val deltaY = event.y - oldTouchY
        this.oldTouchY = event.y
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val newTop = child.top + deltaY + child.translationY
            if (child.isVisible && newTop >= contentBound.top) {
                child.translationY += deltaY
                invalidate()
            }
        }
        return true
    }

    private fun onActionUp(ev: MotionEvent) {
        val upAtClose = icClose?.bounds?.contains(ev.x.toInt(), ev.y.toInt()) == true
        if (needClose && upAtClose) {
            if (isNotEmpty()) {
                translateChildren(0f, getChildAt(0))
            }
            needClose = false
            return
        }
        needClose = false
        val threshold = contentBound.top + contentBound.height() * thresholdPercent
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isVisible) {
                translateChildren(threshold, child)
            }
        }
        placeInContent = false
        return
    }

    private fun onInterceptDown(ev: MotionEvent): Boolean {
        placeInContent = contentBound.contains(ev.x, ev.y)
        if (placeInContent) {
            oldTouchY = ev.y
        }
        if (canDrag) return false
        if (showBtnClose && icClose?.bounds?.contains(ev.x.toInt(), ev.y.toInt()) == true) {
            needClose = true
            return true
        }
        return false
    }

    // Nếu như đi quá ngưỡng thì sẽ dịch chuyển về dưới còn không dịch chuyển lên trên
    private fun translateChildren(threshold: Float, child: View) {
        val childTop = child.top + child.translationY
        val distance = if (childTop > threshold) {
            cancelTouch = true
            measuredHeight - child.top.toFloat()
        } else {
            0f
        }
        child.animate().translationY(distance)
            .setUpdateListener {
                invalidate()
            }
            .withEndAction {
                if (cancelTouch) {
                    isVisible = false
                    child.translationY = 0f
                    removeAllViews()
                }
            }.start()
    }
}