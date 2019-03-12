package com.wavesplatform.wallet.v2.data.local

import android.content.Context
import android.media.MediaFormat.KEY_LANGUAGE
import com.pddstudio.preferences.encrypted.EncryptedPreferences
import com.wavesplatform.wallet.v2.data.model.local.Language
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

    var currentBlocksHeight: Int
        get() = mPref.getInt(KEY_CURRENT_BLOCKS_HEIGHT, 0)
        set(value) = mPref.edit().putInt(KEY_CURRENT_BLOCKS_HEIGHT, value).apply()

    fun setTutorialPassed(value: Boolean) {
        mPref.edit().putBoolean(KEY_TUTORIAL, value).apply()
    }

    fun isTutorialPassed(): Boolean {
        return mPref.getBoolean(KEY_TUTORIAL, false)
    }

    fun getLanguage(): String {
        return mPref.getString(KEY_LANGUAGE, Language.ENGLISH.code)
    }

    fun setLanguage(lang: String) {
        mPref.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun getShowSaveSeedWarningTime(guid: String): Long {
        return mPref.getLong(KEY_SHOW_SAVE_SEED_WARNING + guid, 0)
    }

    fun setShowSaveSeedWarningTime(guid: String, time: Long) {
        mPref.edit().putLong(KEY_SHOW_SAVE_SEED_WARNING + guid, time).apply()
    }

    companion object {
        const val PREF_FILE_NAME = "android_waves_pref_file"
        const val KEY_TUTORIAL = "keyTutorial"
        const val KEY_CURRENT_BLOCKS_HEIGHT = "currentBlocksHeight"
        const val KEY_LANGUAGE = "keyLanguage"
        const val KEY_SHOW_SAVE_SEED_WARNING = "key_show_save_seed_warning"
    }
}
