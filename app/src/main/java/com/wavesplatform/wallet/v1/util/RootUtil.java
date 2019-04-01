/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.util;

import android.os.Build;

import com.wavesplatform.wallet.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtil {

    public static boolean isDeviceRooted() {
        return !BuildConfig.DEBUG && (buildTags() || checkPaths() || checkSuperUser() || isEmulator());

    }

    private static boolean buildTags() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkPaths() {
        String[] paths = {
                "/data/local/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/sbin/su",
                "/system/app/Superuser.apk",
                "/system/bin/failsafe/su",
                "/system/bin/su",
                "/system/sd/xbin/su",
                "/system/xbin/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkSuperUser() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
