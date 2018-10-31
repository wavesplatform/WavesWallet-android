package com.wavesplatform.wallet.v1.util;

import android.os.Build;

public class AndroidUtils {

    public static boolean is21orHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
