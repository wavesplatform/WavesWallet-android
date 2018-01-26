package com.wavesplatform.wallet.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.wavesplatform.wallet.ui.auth.EnvironmentManager;

import org.apache.commons.lang3.*;

public class PrefsUtil implements PersistentPrefs {

    private SharedPreferences preferenceManager;

    public PrefsUtil(Context context) {
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getValue(String name, String value) {
        return getValueInternal(getGuid() + name, value);
    }

    public String getGlobalValue(String name, String value) {
        return getValueInternal(name, value);
    }

    private String getValueInternal(String name, String value) {
        return preferenceManager.getString(name, (value == null || value.isEmpty()) ? "" : value);
    }

    public void setGlobalValue(String name, String value) {
        setValueInternal(name, value);
    }

    public void setValue(String name, String value) {
        setValueInternal(getGuid() + name, value);
    }

    private void setValueInternal(String name, String value) {
        Editor editor = preferenceManager.edit();
        editor.putString(name, (value == null || value.isEmpty()) ? "" : value);
        editor.apply();
    }

    public int getValue(String name, int value) {
        return getValueInternal(getGuid() + name, value);
    }

    private int getValueInternal(String name, int value) {
        return preferenceManager.getInt(name, 0);
    }

    public void setValue(String name, int value) {
        setValueInternal(getGuid() + name, value);
    }

    private void setValueInternal(String name, int value) {
        Editor editor = preferenceManager.edit();
        editor.putInt(name, (value < 0) ? 0 : value);
        editor.apply();
    }

    public long getValue(String name, long value) {
        return getValueInternal(getGuid() + name, value);
    }

    private long getValueInternal(String name, long value) {
        return preferenceManager.getLong(name, 0L);
    }

    public void setValue(String name, long value) {
        setValueInternal(getGuid() + name, value);
    }

    private void setValueInternal(String name, long value) {
        Editor editor = preferenceManager.edit();
        editor.putLong(name, (value < 0L) ? 0L : value);
        editor.apply();
    }

    public boolean getValue(String name, boolean value) {
        return getValueInternal(getGuid() + name, value);
    }

    private boolean getValueInternal(String name, boolean value) {
        return preferenceManager.getBoolean(name, value);
    }

    public void setValue(String name, boolean value) {
        setValueInternal(getGuid() + name, value);
    }

    private void setValueInternal(String name, boolean value) {
        Editor editor = preferenceManager.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public boolean has(String name) {
        return preferenceManager.contains(name);
    }

    public void removeValue(String name) {
        removeValueInternal(getGuid() + name);
    }

    public void removeGlobalValue(String name) {
        removeValueInternal(name);
    }

    private void removeValueInternal(String name) {
        Editor editor = preferenceManager.edit();
        editor.remove(name);
        editor.apply();
    }

    public void clear() {
        Editor editor = preferenceManager.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Clears everything but the GUID for logging back in
     */
    public void logOut() {
        removeGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID);
    }

    /**
     * Reset value once user logged in
     */
    public void logIn() {
        setValue(PrefsUtil.LOGGED_OUT, false);
    }

    public void addListValue(String name, String value) {
        String prev = getValue(name, "");
        if (prev.isEmpty()) {
            setValue(name, value);
        } else {
            setValue(name, prev + "|" + value.trim());
        }
    }

    public void addGlobalListValue(String name, String value) {
        String prev = getGlobalValue(name, "");
        if (prev.isEmpty()) {
            setGlobalValue(name, value);
        } else {
            setGlobalValue(name, prev + "|" + value.trim());
        }
    }

    public String[] getGlobalValueList(String name) {
        if (getGlobalValue(name, "").isEmpty()) {
            return new String[]{};
        } else {
            return getValue(name, "").split("\\|");
        }
    }

    public String[] getValueList(String name) {
        if (getValue(name, "").isEmpty()) {
            return new String[]{};
        } else {
            return getValue(name, "").split("\\|");
        }
    }

    public void setValue(String name, String[] value) {
        setValue(name, org.apache.commons.lang3.StringUtils.join(value, "|"));
    }

    public void setGlobalValue(String name, String[] value) {
        setGlobalValue(name, org.apache.commons.lang3.StringUtils.join(value, "|"));
    }

    public String getGuid() {
        return getGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, "");
    }

    public String getEnvironment() {
        return getGlobalValue(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, EnvironmentManager.KEY_ENV_PROD);
    }


    public void removeAllGuid(String guid) {
        removeValue(guid + PrefsUtil.KEY_AB_ADDRESSES);
        removeValue(guid + PrefsUtil.KEY_AB_NAMES);

        removeValue(guid + PrefsUtil.KEY_PIN_FAILS);
        removeValue(guid + PrefsUtil.KEY_WALLET_NAME);
        removeValue(guid + PrefsUtil.KEY_PUB_KEY);
        removeValue(guid + PrefsUtil.KEY_ENCRYPTED_WALLET);
        removeValue(guid + PrefsUtil.KEY_ENCRYPTED_PASSWORD);
    }

    public void removeListValue(String name, int index) {
        setValue(name, ArrayUtils.remove(getValueList(name), index));
    }

    public void removeGlobalListValue(String name, int index) {
        setGlobalValue(name, ArrayUtils.remove(getGlobalValueList(name), index));
    }
}
