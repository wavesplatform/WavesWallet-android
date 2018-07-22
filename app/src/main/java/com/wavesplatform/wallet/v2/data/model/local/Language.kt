package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wavesplatform.wallet.R


/**
 * Created by anonymous on 16.12.17.
 */


enum class Language(@DrawableRes var image: Int,
                    @StringRes var title: Int,
                    @StringRes var code: Int) {
    ENGLISH(R.drawable.ic_flag_18_britain, R.string.choose_language_english, R.string.choose_language_english_code),
    RUSSIAN(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, R.string.choose_language_russia_code),
    TURKISH(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, R.string.choose_language_turkey_code),
    KOREAN(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, R.string.choose_language_korea_code),
    CHINESE(R.drawable.ic_flag_18_china, R.string.choose_language_china, R.string.choose_language_china_code),
    HINDI(R.drawable.ic_flag_18_hindi, R.string.choose_language_hindi, R.string.choose_language_hindi_code),
    DANISH(R.drawable.ic_flag_18_danish, R.string.choose_language_dansk, R.string.choose_language_dansk_code),
    DUTCH(R.drawable.ic_flag_18_nederland, R.string.choose_language_nederlands, R.string.choose_language_nederlands_code);

    companion object {
        fun getLanguagesItems() : ArrayList<LanguageItem> {
            return Language.values().mapTo(ArrayList(), { LanguageItem(it, false) })
        }

        fun getLanguageByCode(code: Int): Language {
            Language.values().forEach {
                if (it.code == code) return it
            }
            return Language.ENGLISH
        }

        fun getLanguageItemByCode(code: Int): LanguageItem {
            return LanguageItem(getLanguageByCode(code), false)
        }
    }
}