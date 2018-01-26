package com.wavesplatform.wallet.util;

import android.content.Context;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;

import com.wavesplatform.wallet.crypto.Base58;

import org.apache.commons.io.Charsets;

public class StringUtils {

    private Context mContext;

    public StringUtils(Context context) {
        mContext = context;
    }

    public String getString(@StringRes int stringId) {
        return mContext.getString(stringId);
    }

    public String getQuantityString(@PluralsRes int pluralId, int size) {
        return mContext.getResources().getQuantityString(pluralId, size, size);
    }

    public static boolean isValidName(String name) {
        return name.matches("^[\\w\\- ]{3,}$");
    }

    public static String fromBase58Utf8(String encoded) {
        if (encoded == null)
            return null;

        try {
            return new String(Base58.decode(encoded), Charsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
