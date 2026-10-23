package com.b9ads.secret

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Secret {

    init {
        System.loadLibrary("secure")
    }

    fun decrypt(value: String): String {
        val key = gk()
        val iv = iv()
        val ivParameterSpec = IvParameterSpec(hexStringToByteArray(iv))
        val secretKeySpec = SecretKeySpec(hexStringToByteArray(key), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decodeBase64 = Base64.decode(value, Base64.DEFAULT)
        val decrypted = cipher.doFinal(decodeBase64)
        return String(decrypted)
    }


    private fun hexStringToByteArray(hex: String): ByteArray {
        // hexString phải có độ dài chẵn nếu không nó đã bị sửa
        if (hex.length % 2 != 0) {
            return ByteArray(16)
        }
        return ByteArray(hex.length / 2) {
            hex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }

    private external fun gk(): String
    private external fun iv(): String

}
