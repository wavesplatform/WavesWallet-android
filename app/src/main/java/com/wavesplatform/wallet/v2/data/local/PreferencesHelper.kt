package com.wavesplatform.wallet.v2.data.local

import android.content.Context
import com.pddstudio.preferences.encrypted.EncryptedPreferences
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import javax.inject.Inject

class PreferencesHelper @Inject constructor(@ApplicationContext context: Context) {

    private val mPref: EncryptedPreferences

    init {
        mPref = EncryptedPreferences.Builder(context).withPreferenceName(PREF_FILE_NAME).withEncryptionPassword("Waves").build()
    }

    fun clear() {
        mPref.edit().clear().apply()
    }

    companion object {
        val PREF_FILE_NAME = "android_waves_pref_file"
        val KEY_TUTORIAL = "keyTutorial"
        val KEY_CURRENT_BLOCKS_HEIGHT = "currentBlocksHeight"
        val KEY_LANGUAGE = "keyLanguage"
        val KEY_ACCOUNT_FIRST_OPEN = "key_account_first_open"
        val KEY_DEFAULT_ASSETS = "key_default_assets"
        val KEY_SHOW_SAVE_SEED_WARNING = "key_show_save_seed_warning"
    }

    var currentBlocksHeight: Int
        get() = mPref.getInt(KEY_CURRENT_BLOCKS_HEIGHT, 0)
        set(value) = mPref.edit().putInt(KEY_CURRENT_BLOCKS_HEIGHT, value).apply()


    fun setTutorialPassed(value: Boolean) {
        mPref.edit().putBoolean(KEY_TUTORIAL, value).apply()
    }

    fun isTutorialPassed(): Boolean {
        return mPref.getBoolean(KEY_TUTORIAL, false)
    }

    fun getLanguage(): Int {
        return mPref.getInt(KEY_LANGUAGE, 0)
    }

    fun setLanguage(lang: Int) {
        mPref.edit().putInt(KEY_LANGUAGE, lang).apply()
    }

    fun isDefaultAssetsAlreadyExist(): Boolean {
        return mPref.getBoolean(KEY_DEFAULT_ASSETS, false)
    }

    fun setDefaultAssetsAlreadyExist(value: Boolean) {
        mPref.edit().putBoolean(KEY_DEFAULT_ASSETS, value).apply()
    }

    fun isAccountFirstOpen(): Boolean {
        return mPref.getBoolean(KEY_ACCOUNT_FIRST_OPEN, false)
    }

    fun setAccountFirstOpen(value: Boolean) {
        mPref.edit().putBoolean(KEY_ACCOUNT_FIRST_OPEN, value).apply()
    }


    fun getShowSaveSeedWarningTime(guid: String): Int {
        return mPref.getInt(KEY_SHOW_SAVE_SEED_WARNING + guid, 0)
    }

    fun setShowSaveSeedWarningTime(guid: String, time: Long) {
        mPref.edit().putLong(KEY_SHOW_SAVE_SEED_WARNING + guid, time).apply()
    }
}
