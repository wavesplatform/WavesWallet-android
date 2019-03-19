package com.wavesplatform.sdk.net.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wavesplatform.sdk.R
import java.util.*


enum class Language(
        @DrawableRes var image: Int,
        @StringRes var title: Int,
        var code: String,
        var iso: String
) {
    ENGLISH(R.drawable.ic_flag_18_britain, R.string.choose_language_english, "en", "EN"),
    RUSSIAN(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, "ru", "RU"),
    KOREAN(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, "ko", "KO"),
    CHINESE_SIMPLIFIED(R.drawable.ic_flag_18_china, R.string.choose_language_china, "zh_CN", "ZH"),
    TURKISH(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, "tr", "TR"),
    DUTCH(R.drawable.ic_flag_18_nederland, R.string.choose_language_nederlands, "nl", "NL"),
    HINDI(R.drawable.ic_flag_18_hindi, R.string.choose_language_hindi, "hi", "HI"),
    SPANISH(R.drawable.ic_flag_18_spain, R.string.choose_language_spain, "es", "ES"),
    PORTUGUESE(R.drawable.ic_flag_18_portugal, R.string.choose_language_portuguese, "pt", "PT"),
    BRAZILIAN(R.drawable.ic_flag_18_brazil, R.string.choose_language_brazilian, "pt_BR", "BR"),
    // DANISH(R.drawable.ic_flag_18_danish, R.string.choose_language_danish, "dn", "dn"),
    GERMAN(R.drawable.ic_flag_18_germany, R.string.choose_language_german, "de", "DE"),
    INDONESIAN(R.drawable.ic_flag_18_indonesia, R.string.choose_language_indonesian, "in", "ID"),
    JAPAN(R.drawable.ic_flag_18_japan, R.string.choose_language_japan, "ja", "JA"),
    // POLISH(R.drawable.ic_flag_18_polszczyzna, R.string.choose_language_polish, "pl"),
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

        fun getLanguageByCode(code: String): Language {
            return Language.values().firstOrNull { it.code == code } ?: Language.ENGLISH
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