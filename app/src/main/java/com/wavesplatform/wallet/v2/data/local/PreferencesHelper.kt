package com.wavesplatform.wallet.v2.data.local

import android.content.Context
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext

import com.pddstudio.preferences.encrypted.EncryptedPreferences
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
        val KEY_LANGUAGE = "keyLanguage"
        val KEY_DEFAULT_ASSETS = "key_default_assets"
    }

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

}
