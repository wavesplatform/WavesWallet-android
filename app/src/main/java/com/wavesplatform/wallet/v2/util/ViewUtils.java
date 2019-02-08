package com.wavesplatform.wallet.v2.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by adambennett on 29/07/2016.
 */

public class ViewUtils {

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp to convert to pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
