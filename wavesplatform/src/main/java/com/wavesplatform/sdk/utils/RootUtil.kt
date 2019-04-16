package com.wavesplatform.sdk.utils

import android.os.Build

import com.wavesplatform.sdk.BuildConfig

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootUtil {

    val isDeviceRooted: Boolean
        get() = !BuildConfig.DEBUG && (buildTags() || checkPaths() || checkSuperUser() || isEmulator)

    private val isEmulator: Boolean
        get() = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)

    private fun buildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkPaths(): Boolean {
        val paths = arrayOf("/data/local/su", "/data/local/xbin/su", "/data/local/bin/su", "/sbin/su", "/system/app/Superuser.apk", "/system/bin/failsafe/su", "/system/bin/su", "/system/sd/xbin/su", "/system/xbin/su")

        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkSuperUser(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process!!.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }

    }
}
