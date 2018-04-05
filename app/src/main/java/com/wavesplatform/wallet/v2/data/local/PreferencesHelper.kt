package com.wavesplatform.wallet.v2.data.local

import android.content.Context
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext

import com.pddstudio.preferences.encrypted.EncryptedPreferences
import javax.inject.Inject

class PreferencesHelper @Inject constructor(@ApplicationContext context: Context) {

    private val mPref: EncryptedPreferences

    init {
        mPref = EncryptedPreferences.Builder(context).withPreferenceName(PREF_FILE_NAME).withEncryptionPassword("Example").build() // TODO: change Password
    }

    fun clear() {
        mPref.edit().clear().apply()
    }

    companion object {
        val PREF_FILE_NAME = "android_boilerplate_pref_file"
    }

}
