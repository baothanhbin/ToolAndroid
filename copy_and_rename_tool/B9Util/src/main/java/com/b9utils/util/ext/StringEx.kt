package com.b9utils.util.ext

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.google.gson.Gson

inline fun <reified T> String.convertToObject(): T = Gson().fromJson(this, T::class.java)

fun String?.toColor(): Int {
    return try {
        if (this.isNullOrBlank()) return Color.WHITE
        this.toColorInt()
    } catch (_: Exception) {
        Color.WHITE
    }
}