package com.b9utils.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    private const val RESIZE_IMAGE_NAME_NEW = "ImageResize"

    fun getRealPathFromURI(contentUri: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(index)
        }
        return null
    }

    fun scaleBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= 512)
            return bitmap

        val w = bitmap.width
        val h = bitmap.height
        val aspRat = w / h.toFloat()
        val width = 512
        val height = width / aspRat
        return Bitmap.createScaledBitmap(bitmap, width, height.toInt(), false)
    }

    fun getFileFromScaledBitmap(context: Context, reducedBitmap: Bitmap, fileName: String): File? {
        val quality = 90
        val byteArrayOutputStream = ByteArrayOutputStream()
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()

        val cacheDir = File(context.cacheDir, RESIZE_IMAGE_NAME_NEW)
        if (!cacheDir.isDirectory) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, fileName)
        file.createNewFile()

        return try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bitmapData)
            fileOutputStream.flush()
            fileOutputStream.close()
            file
        } catch (ex: IOException) {
            null
        }
    }
    fun getBitmapFromPath(imagePath: String): Bitmap? {
        return BitmapFactory.decodeFile(imagePath)
    }

    fun calculatorResultImageSize(bitmap: Bitmap, imageResult: ImageView, maxSize: Int = 0) {
        val ratio = bitmap.width / bitmap.height.toFloat()
        val minSizeView = if (imageResult.width == 0)
            maxSize
        else
            minOf(imageResult.width, imageResult.height)
        val width: Int
        val height: Int
        if (ratio >= 1) {
            width = minSizeView
            height = (width / ratio).toInt()
        } else {
            height = minSizeView
            width = (height * ratio).toInt()
        }
        val layoutParams = imageResult.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        imageResult.layoutParams = layoutParams
    }
    /**
     * Kiểm tra kích thước file ảnh (dung lượng)
     * @param filePath đường dẫn tới file ảnh
     * @param maxFileSize kích thước file tối đa (byte)
     * @return true nếu file hợp lệ, false nếu vượt quá giới hạn
     */
    fun isFileSizeValid(filePath: String, maxFileSize: Long): Boolean {
        val file = File(filePath)
        return file.length() <= maxFileSize
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    fun saveBitmapToGallery(context: Context, bitmap: Bitmap){
        val contentResolver = context.contentResolver
        val imageUri: Uri?
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "AppName_${System.currentTimeMillis()}.png"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/AppName"
            )
        }

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        imageUri?.let { uri ->
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.close()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}