package com.b9utils.util.ext

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.core.os.BundleCompat

inline fun <reified T : Parcelable> Intent.getParcel(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras?.let { BundleCompat.getParcelable(it, key, T::class.java) }
    } else {
        this.getParcelableExtra(key)
    }
}
