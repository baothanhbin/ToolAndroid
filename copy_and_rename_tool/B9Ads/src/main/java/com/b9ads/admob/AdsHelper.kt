package com.b9ads.admob

import android.content.Context
import android.content.pm.PackageManager
import com.b9ads.secret.Secret
import com.b9utils.util.appVersion.AppVersion
import java.security.MessageDigest

object AdsHelper {

    private var allowShowAd: List<String?>? = null
    private var notShowAd: List<String?>? = null
    private var originalSignatureSha: String = ""
    private var canShowAds: Boolean = false

    // Lấy SHA256 của keystore ban đầu dùng để build app
    fun setOriginalSignature(sha256: String) {
        this.originalSignatureSha = sha256
    }

    fun setVersionData(allow: List<String?>?, notShow: List<String?>?) {
        allowShowAd = allow
        notShowAd = notShow
    }

    fun checkShowAdByVersion() = canShowAds

    fun calculateShowAds(context: Context) {
        val currentSignature = getSHA256(context)
        // nếu khác nhau thì app đã được chỉnh sửa và ký lại bằng keystore khác
        canShowAds = if (currentSignature != Secret.decrypt(originalSignatureSha)) {
            false
        } else {
            compareVersion(context)
        }
    }

    // Lấy SHA256 của keystore hiện tại
    private fun getSHA256(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            val signature = packageInfo.signingInfo?.signingCertificateHistory?.firstOrNull()

            signature?.let { sig ->
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(sig.toByteArray())
                hash.joinToString(":") { "%02X".format(it) }
            }

        } catch (_: Exception) {
            null
        }
    }

    private fun compareVersion(context: Context): Boolean {
        val currentVersion = AppVersion.getCurrentVersion(context)
        return if (currentVersion.isBlank()) {
            false
        } else if (allowShowAd.isNullOrEmpty().not()) {
            allowShowAd?.any { it == currentVersion } == true
        } else if (notShowAd.isNullOrEmpty().not()) {
            notShowAd?.none { it == currentVersion } == true
        } else {
            false
        }
    }
}
