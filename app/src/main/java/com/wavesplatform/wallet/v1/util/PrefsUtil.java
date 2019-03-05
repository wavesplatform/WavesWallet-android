package com.wavesplatform.wallet.v1.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalanceStore;
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext;
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class PrefsUtil {

    public static final String GLOBAL_CURRENT_ENVIRONMENT = "global_current_environment";
    public static final String GLOBAL_CURRENT_ENVIRONMENT_DATA = "global_current_environment_data";
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
    public static final String KEY_ADDRESS_BOOK = "address_book";
    public static final String KEY_ASSET_BALANCES = "asset_balances";

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

    public List<AddressBookUser> getAllAddressBookUsers() {
        Type listType = new TypeToken<ArrayList<AddressBookUser>>() {
        }.getType();
        List<AddressBookUser> list = new Gson().fromJson(
                getGlobalValue(KEY_ADDRESS_BOOK, ""), listType);
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    public AddressBookUser getAddressBookUser(String address) {
        for (AddressBookUser addressBook : getAllAddressBookUsers()) {
            if (addressBook.getAddress().equals(address)) {
                return addressBook;
            }
        }
        return null;
    }

    public void deleteAddressBookUsers(String address) {
        List<AddressBookUser> list = new ArrayList<>();
        for (AddressBookUser addressBook : getAllAddressBookUsers()) {
            if (!addressBook.getAddress().equals(address)) {
                list.add(addressBook);
            }
        }
        setAddressBookUsers(list);
    }

    public void setAddressBookUsers(List<AddressBookUser> addressBookUsers) {
        setGlobalValue(KEY_ADDRESS_BOOK, new Gson().toJson(addressBookUsers));
    }

    public void saveAddressBookUsers(AddressBookUser addressBookUser) {
        List<AddressBookUser> result = getAllAddressBookUsers();
        boolean add = true;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getAddress().equals(addressBookUser.getAddress())) {
                result.set(i, addressBookUser);
                add = false;
            }
        }
        if (add) {
            result.add(addressBookUser);
        }
        setAddressBookUsers(result);
    }

    public void saveAssetBalance(AssetBalance assetBalance) {
        String guid = App.getAccessManager().getLoggedInGuid();
        Map<String, AssetBalanceStore> map = getAssetBalances();
        map.put(assetBalance.getAssetId(), new AssetBalanceStore(
                assetBalance.getAssetId(),
                assetBalance.isHidden(),
                assetBalance.getPosition(),
                assetBalance.isFavorite()));
        setGlobalValue(KEY_ASSET_BALANCES + "_" + guid, new Gson().toJson(map));
    }

    public void saveAssetBalances(Map<String, AssetBalanceStore> assetBalances) {
        String guid = App.getAccessManager().getLoggedInGuid();
        setGlobalValue(KEY_ASSET_BALANCES + "_" + guid, new Gson().toJson(assetBalances));
    }

    public Map<String, AssetBalanceStore> getAssetBalances() {
        String guid = App.getAccessManager().getLoggedInGuid();
        Map<String, AssetBalanceStore> map = new Gson().fromJson(
                getGlobalValue(KEY_ASSET_BALANCES + "_" + guid, ""),
                TypeToken.getParameterized(
                        HashMap.class,
                        String.class,
                        AssetBalanceStore.class).getType());
        if (map == null) {
            return new HashMap<>();
        } else {
            return map;
        }
    }

    public void saveAssetBalances(@NotNull List<AssetBalance> assetsList) {
        String guid = App.getAccessManager().getLoggedInGuid();
        Map<String, AssetBalanceStore> map = getAssetBalances();
        for (AssetBalance assetBalance : assetsList) {
            map.put(assetBalance.getAssetId(), new AssetBalanceStore(
                    assetBalance.getAssetId(),
                    assetBalance.isHidden(),
                    assetBalance.getPosition(),
                    assetBalance.isFavorite()));
        }
        setGlobalValue(KEY_ASSET_BALANCES + "_" + guid, new Gson().toJson(map));
    }
}