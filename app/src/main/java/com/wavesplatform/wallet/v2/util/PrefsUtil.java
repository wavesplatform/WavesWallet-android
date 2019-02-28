package com.wavesplatform.wallet.v2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class PrefsUtil {

    public static final String SHOWED_NEWS_IDS = "showed_news_ids";
    public static final String GLOBAL_LAST_LOGGED_IN_GUID = "global_logged_in_wallet_guid";
    public static final String GLOBAL_SCHEME_URL = "scheme_url";
    public static final String LIST_WALLET_GUIDS = "list_wallet_guid";

    public static final String KEY_WALLET_NAME = "wallet_name";
    public static final String KEY_PUB_KEY = "wallet_public_key";
    public static final String KEY_ENCRYPTED_WALLET = "encrypted_wallet";
    public static final String KEY_SKIP_BACKUP = "skip_backup";
    public static final String KEY_ENCRYPTED_PASSWORD = "encrypted_password";
    public static final String KEY_PIN_FAILS = "pin_fails";
    public static final String KEY_USE_FINGERPRINT = "use_fingerprint";
    public static final String KEY_ENCRYPTED_PIN = "encrypted_pin";

    public static final String KEY_LAST_SENT_ADDRESSES = "last_sent_addresses";
    public static final String KEY_ACCOUNT_FIRST_OPEN = "key_account_first_open";

    public static final String KEY_DEFAULT_ASSETS = "key_default_assets";
    public static final String KEY_ENABLE_SPAM_FILTER = "enable_spam_filter";
    public static final String KEY_SPAM_URL = "spam_url";
    public static final String KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS = "key_need_update_transaction_after_change_spam_settings";
    public static final String KEY_SCRIPTED_ACCOUNT = "scripted_account";

    public static final String KEY_LAST_UPDATE_DEX_INFO = "last_update_dex_info";

    public static final String KEY_GLOBAL_NODE_COOKIES = "node_cookies";

    private SharedPreferences preferenceManager;

    @Inject
    public PrefsUtil(@ApplicationContext Context context) {
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getValue(String name, String value) {
        return getValueInternal(getGuid() + name, value);
    }

    public String getValue(String guid, String name, String value) {
        return getValueInternal(guid + name, value);
    }

    public int getValue(String guid, String name, int value) {
        return getValueInternal(guid + name, value);
    }

    public String getGlobalValue(String name, String value) {
        return getValueInternal(name, value);
    }

    public Long getGlobalValue(String name, Long value) {
        return getValueInternal(name, value);
    }

    public Set<String> getGlobalValue(String name) {
        return getValueInternal(name);
    }

    private String getValueInternal(String name, String value) {
        return preferenceManager.getString(name, TextUtils.isEmpty(value) ? "" : value);
    }

    private Set<String> getValueInternal(String name) {
        return preferenceManager.getStringSet(name, new HashSet<String>());
    }

    public void setGlobalValue(String name, String value) {
        setValueInternal(name, value);
    }

    public void setGlobalValue(String name, Long value) {
        setValueInternal(name, value);
    }

    public void setGlobalValue(String name, HashSet<String> value) {
        setValueInternal(name, value);
    }

    public void setValue(String name, String value) {
        setValueInternal(getGuid() + name, value);
    }

    public void setValue(String name, Long value) {
        setValueInternal(getGuid() + name, value);
    }

    public void setValue(String guid, String name, String value) {
        setValueInternal(guid + name, value);
    }

    public void setValue(String guid, String name, int value) {
        setValueInternal(guid + name, value);
    }

    public void setValue(String guid, String name, Boolean value) {
        setValueInternal(guid + name, value);
    }

    private void setValueInternal(String name, HashSet<String> value) {
        Editor editor = preferenceManager.edit();
        editor.putStringSet(name, value);
        editor.apply();
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
        return preferenceManager.getInt(name, value);
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
        return preferenceManager.getLong(name, value);
    }

    public boolean getValue(String name, boolean value) {
        return getGuidValue(getGuid(), name, value);
    }

    public boolean getGuidValue(String guid, String name, boolean value) {
        return preferenceManager.getBoolean(guid + name, value);
    }

    public void setValue(String name, boolean value) {
        setValueInternal(getGuid() + name, value);
    }

    private void setValueInternal(String name, boolean value) {
        Editor editor = preferenceManager.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private void setValueInternal(String name, long value) {
        Editor editor = preferenceManager.edit();
        editor.putLong(name, value);
        editor.apply();
    }

    public boolean has(String name) {
        return preferenceManager.contains(name);
    }

    public void removeValue(String guid, String name) {
        removeValueInternal(guid + name);
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
        removeGlobalValue(PrefsUtil.GLOBAL_LAST_LOGGED_IN_GUID);
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
            return getGlobalValue(name, "").split("\\|");
        }
    }

    public void setGlobalValue(String name, String[] value) {
        setGlobalValue(name, org.apache.commons.lang3.StringUtils.join(value, "|"));
    }

    public String getGuid() {
        return getGlobalValue(PrefsUtil.GLOBAL_LAST_LOGGED_IN_GUID, "");
    }
}