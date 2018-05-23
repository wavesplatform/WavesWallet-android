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
    }

    fun setTutorialPassed(value: Boolean){
        mPref.edit().putBoolean(KEY_TUTORIAL, value).apply()
    }

    fun isTutorialPassed() : Boolean{
        return mPref.getBoolean(KEY_TUTORIAL, false)
    }

}
