package com.b9utils.util.ext

import android.content.res.Resources
import android.util.TypedValue

fun Float.toDp(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )
}

fun Int.toDp() = this.toFloat().toDp()