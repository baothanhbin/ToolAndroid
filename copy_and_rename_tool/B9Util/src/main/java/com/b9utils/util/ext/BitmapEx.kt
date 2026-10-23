package com.b9utils.util.ext

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun Bitmap.toFile(context: Context): File {
    val wrapper = ContextWrapper(context)
    val file = File(wrapper.cacheDir, "${System.currentTimeMillis()}.jpg")
    val stream: OutputStream = FileOutputStream(file)
    this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    stream.flush()
    stream.close()
    return file
}