/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import java.util.*
import javax.inject.Inject

class PrefsUtil @Inject constructor(@ApplicationContext context: Context) {

    private val preferenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val guid: String
        get() = getGlobalValue(GLOBAL_LAST_LOGGED_IN_GUID, "")

    private val dexNotShownAlertAboutPairList: MutableList<String>
        get() {
            val listType = object : TypeToken<MutableList<String>>() {}.type
            return Gson().fromJson<MutableList<String>>(
                    getValue(KEY_DEX_PAIR_SMART_INFO_NOT_SHOW_LIST, ""), listType)
                    ?: mutableListOf()
        }

    fun getValue(name: String, value: String): String {
        return getValueInternal(guid + name, value)
    }

    fun getValue(guid: String, name: String, value: String): String {
        return getValueInternal(guid + name, value)
    }

    fun getValue(guid: String, name: String, value: Int): Int {
        return getValueInternal(guid + name, value)
    }

    fun getValue(name: String): MutableSet<String> {
        return getValueInternal(guid + name)
    }

    fun getGlobalValue(name: String, value: String): String {
        return getValueInternal(name, value)
    }

    fun getGlobalValue(name: String, value: Long?): Long? {
        return getValueInternal(name, value!!)
    }

    fun getGlobalValue(name: String): Set<String> {
        return getValueInternal(name)
    }

    private fun getValueInternal(name: String, value: String): String {
        return preferenceManager.getString(name, if (TextUtils.isEmpty(value)) "" else value) ?: ""
    }

    private fun getValueInternal(name: String): MutableSet<String> {
        return preferenceManager.getStringSet(name, mutableSetOf()) ?: mutableSetOf()
    }

    fun setGlobalValue(name: String, value: String) {
        setValueInternal(name, value)
    }

    fun setGlobalValue(name: String, value: Long?) {
        setValueInternal(name, value!!)
    }

    fun setGlobalValue(name: String, value: HashSet<String>) {
        setValueInternal(name, value)
    }

    fun setValue(name: String, value: String) {
        setValueInternal(guid + name, value)
    }

    fun setValue(name: String, value: Long?) {
        setValueInternal(guid + name, value!!)
    }

    fun setValue(guid: String, name: String, value: String) {
        setValueInternal(guid + name, value)
    }

    fun setValue(guid: String, name: String, value: Int) {
        setValueInternal(guid + name, value)
    }

    fun setValue(guid: String, name: String, value: Boolean?) {
        setValueInternal(guid + name, value!!)
    }

    fun setValue(name: String, value: MutableSet<String>) {
        setValueInternal(guid + name, value.toHashSet())
    }

    private fun setValueInternal(name: String, value: HashSet<String>) {
        val editor = preferenceManager.edit()
        editor.putStringSet(name, value)
        editor.apply()
    }

    private fun setValueInternal(name: String, value: String?) {
        val editor = preferenceManager.edit()
        editor.putString(name, if (value == null || value.isEmpty()) "" else value)
        editor.apply()
    }

    fun getValue(name: String, value: Int): Int {
        return getValueInternal(guid + name, value)
    }

    private fun getValueInternal(name: String, value: Int): Int {
        return preferenceManager.getInt(name, value)
    }

    private fun setValueInternal(name: String, value: Int) {
        val editor = preferenceManager.edit()
        editor.putInt(name, if (value < 0) 0 else value)
        editor.apply()
    }

    fun getValue(name: String, value: Long): Long {
        return getValueInternal(guid + name, value)
    }

    private fun getValueInternal(name: String, value: Long): Long {
        return preferenceManager.getLong(name, value)
    }

    fun getValue(name: String, value: Boolean): Boolean {
        return getGuidValue(guid, name, value)
    }

    fun getGuidValue(guid: String, name: String, value: Boolean): Boolean {
        return preferenceManager.getBoolean(guid + name, value)
    }

    fun setValue(name: String, value: Boolean) {
        setValueInternal(guid + name, value)
    }

    private fun setValueInternal(name: String, value: Boolean) {
        val editor = preferenceManager.edit()
        editor.putBoolean(name, value)
        editor.apply()
    }

    private fun setValueInternal(name: String, value: Long) {
        val editor = preferenceManager.edit()
        editor.putLong(name, value)
        editor.apply()
    }

    fun has(name: String): Boolean {
        return preferenceManager.contains(name)
    }

    fun removeValue(guid: String, name: String) {
        removeValueInternal(guid + name)
    }

    fun removeGlobalValue(name: String) {
        removeValueInternal(name)
    }

    private fun removeValueInternal(name: String) {
        val editor = preferenceManager.edit()
        editor.remove(name)
        editor.apply()
    }

    fun clear() {
        val editor = preferenceManager.edit()
        editor.clear()
        editor.apply()
    }

    /**
     * Clears everything but the GUID for logging back in
     */
    fun logOut() {
        removeGlobalValue(GLOBAL_LAST_LOGGED_IN_GUID)
    }

    fun addGlobalListValue(name: String, value: String) {
        val prev = getGlobalValue(name, "")
        if (prev.isEmpty()) {
            setGlobalValue(name, value)
        } else {
            setGlobalValue(name, prev + "|" + value.trim { it <= ' ' })
        }
    }

    fun getGlobalValueList(name: String): Array<String> {
        return if (getGlobalValue(name, "").isEmpty()) {
            arrayOf()
        } else {
            getGlobalValue(name, "").split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
    }

    fun setGlobalValue(name: String, value: Array<String>) {
        setGlobalValue(name, org.apache.commons.lang3.StringUtils.join(value, "|"))
    }

    fun setNotShownSmartAlertForPair(amount: String, price: String, notShowAgain: Boolean) {
        if (notShowAgain) {
            val shownAlertAboutPairList = dexNotShownAlertAboutPairList
            val uniqueId = amount + price
            shownAlertAboutPairList.add(uniqueId)
            saveNotShownSmartAlertForPairList(shownAlertAboutPairList)
        }
    }

    fun saveNotShownSmartAlertForPairList(list: MutableList<String>) {
        setValue(KEY_DEX_PAIR_SMART_INFO_NOT_SHOW_LIST, Gson().toJson(list))
    }

    fun isNotShownSmartAlertForPair(amount: String, price: String): Boolean {
        val uniqueId = amount + price
        return dexNotShownAlertAboutPairList.firstOrNull { it == uniqueId } != null
    }

    companion object {
        const val SHOWED_NEWS_IDS = "showed_news_ids"
        const val GLOBAL_LAST_LOGGED_IN_GUID = "global_logged_in_wallet_guid"
        const val GLOBAL_SCHEME_URL = "scheme_url"
        const val LIST_WALLET_GUIDS = "list_wallet_guid"
        const val KEY_WALLET_NAME = "wallet_name"
        const val KEY_PUB_KEY = "wallet_public_key"
        const val KEY_ENCRYPTED_WALLET = "encrypted_wallet"
        const val KEY_SKIP_BACKUP = "skip_backup"
        const val KEY_ENCRYPTED_PASSWORD = "encrypted_password"
        const val KEY_PIN_FAILS = "pin_fails"
        const val KEY_USE_FINGERPRINT = "use_fingerprint"
        const val KEY_ENCRYPTED_PIN = "encrypted_pin"
        const val KEY_LAST_SENT_ADDRESSES = "last_sent_addresses"
        const val KEY_ACCOUNT_FIRST_OPEN = "key_account_first_open"
        const val KEY_DEFAULT_ASSETS = "key_default_assets"
        const val KEY_ENABLE_SPAM_FILTER = "enable_spam_filter"
        const val KEY_SPAM_URL = "spam_url"
        const val KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS = "key_need_update_transaction_after_change_spam_settings"
        const val KEY_SCRIPTED_ACCOUNT = "scripted_account"
        const val KEY_LAST_UPDATE_DEX_INFO = "last_update_dex_info"
        const val KEY_GLOBAL_NODE_COOKIES = "node_cookies"
        const val KEY_DEX_PAIR_SMART_INFO_NOT_SHOW_LIST = "dex_pair_smart_info_not_show_list"
        const val KEY_ASSETS_ZERO = "assets_zero"
        const val KEY_ASSETS_ALL = "assets_all"
    }
}