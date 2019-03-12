package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wavesplatform.wallet.R
import java.util.*

/**
 * Created by anonymous on 16.12.17.
 */

enum class Language(
    @DrawableRes var image: Int,
    @StringRes var title: Int,
    var code: String
) {
    ENGLISH(R.drawable.ic_flag_18_britain, R.string.choose_language_english, "en"),
    RUSSIAN(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, "ru"),
    KOREAN(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, "ko"),
    CHINESE(R.drawable.ic_flag_18_china, R.string.choose_language_china, "zh"),
    TURKISH(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, "tr"),
    DUTCH(R.drawable.ic_flag_18_nederland, R.string.choose_language_nederlands, "nl"),
    HINDI(R.drawable.ic_flag_18_hindi, R.string.choose_language_hindi, "hi"),
    SPANISH(R.drawable.ic_flag_18_spain, R.string.choose_language_spain, "es"),
    // DANISH(R.drawable.ic_flag_18_danish, R.string.choose_language_danish, "dn"),
    INDONESIAN(R.drawable.ic_flag_18_indonesia, R.string.choose_language_indonesian, "in"),
    GERMAN(R.drawable.ic_flag_18_germany, R.string.choose_language_german, "de"),
    JAPAN(R.drawable.ic_flag_18_japan, R.string.choose_language_japan, "ja"),
    PORTUGUESE(R.drawable.ic_flag_18_portugal, R.string.choose_language_portuguese, "pt"),
    BRAZILIAN(R.drawable.ic_flag_18_brazil, R.string.choose_language_brazilian, "pt_BR"),
    // POLISH(R.drawable.ic_flag_18_polszczyzna, R.string.choose_language_polish, "pl")
    ;

    companion object {

        fun getLanguagesItems(): ArrayList<LanguageItem> {
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

        fun getLocale(code: String): Locale {
            return if (code.contains("_")) {
                val locale = code.split("_")
                Locale(locale[0], locale[1])
            } else {
                Locale(code)
            }
        }
    }
}