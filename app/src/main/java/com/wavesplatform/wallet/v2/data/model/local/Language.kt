package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wavesplatform.wallet.R
import java.util.*


/**
 * Created by anonymous on 16.12.17.
 */


enum class Language(@DrawableRes var image: Int,
                    @StringRes var title: Int,
                    var code: String) {
    ENGLISH(R.drawable.ic_flag_18_britain, R.string.choose_language_english, "en"),
    RUSSIAN(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, "ru"),
    SPANISH(R.drawable.ic_flag_18_spain, R.string.choose_language_spain, "es"),
    // TURKISH(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, "tr"),
    KOREAN(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, "ko"),
    // CHINESE(R.drawable.ic_flag_18_china, "zh),
    // HINDI(R.drawable.ic_flag_18_hindi, "hi),
    // DANISH(R.drawable.ic_flag_18_danish, "dn),
    DUTCH(R.drawable.ic_flag_18_nederland, R.string.choose_language_nederlands, "nl");

    companion object {

        fun getLanguagesItems() : ArrayList<LanguageItem> {
            return Language.values().mapTo(ArrayList()) { LanguageItem(it, false) }
        }

        fun getLanguageItemByCode(code: String): LanguageItem {
            Language.values().forEach {
                if (it.code == code) {
                    return LanguageItem(it, false)
                }
            }
            return LanguageItem(Language.ENGLISH, false)
        }
    }
}