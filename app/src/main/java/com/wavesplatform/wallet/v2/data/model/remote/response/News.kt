package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.model.local.Language

class News {

    @SerializedName("notifications")
    var notifications: List<Notification> = listOf()

    class Notification {

        @SerializedName("startDate")
        var startDate: Long? = null
        @SerializedName("endDate")
        var endDate: Long? = null
        @SerializedName("logoUrl")
        var logoUrl: String = ""
        @SerializedName("id")
        var id: String = ""
        @SerializedName("title")
        var title: Language? = null
        @SerializedName("subTitle")
        var subTitle: Language? = null

        class Language {
            @SerializedName("en")
            var en: String = ""
            @SerializedName("ru")
            var ru: String = ""
            @SerializedName("ko")
            var ko: String = ""
            @SerializedName("zh")
            var zh: String = ""
            @SerializedName("tr")
            var tr: String = ""
            @SerializedName("nl")
            var nl: String = ""
            @SerializedName("hi")
            var hi: String = ""
            @SerializedName("es")
            var es: String = ""
            @SerializedName("in")
            var ind: String = ""
            @SerializedName("de")
            var de: String = ""
            @SerializedName("jp")
            var jp: String = ""
            @SerializedName("pt")
            var pt: String = ""
            @SerializedName("br")
            var br: String = ""
        }
    }

    companion object {
        const val URL = "https://github-proxy.wvservices.com/wavesplatform/waves-client-config/mobile/v2.2/notifications.json"

        fun getTitle(langCode: String, notification: Notification): String {
            return getLine(langCode, notification, true)
        }

        fun getSubtitle(langCode: String, notification: Notification): String {
            return getLine(langCode, notification, false)
        }

        private fun getLine(langCode: String, notification: Notification, title: Boolean): String {
            val result: String
            when (langCode) {
                Language.ENGLISH.code -> {
                    result = if (title) {
                        notification.title?.en ?: ""
                    } else {
                        notification.subTitle?.en ?: ""
                    }
                }
                Language.RUSSIAN.code -> {
                    result = if (title) {
                        notification.title?.ru ?: ""
                    } else {
                        notification.subTitle?.ru ?: ""
                    }
                }
                Language.KOREAN.code -> {
                    result = if (title) {
                        notification.title?.ko ?: ""
                    } else {
                        notification.subTitle?.ko ?: ""
                    }
                }
                Language.CHINESE.code -> {
                    result = if (title) {
                        notification.title?.zh ?: ""
                    } else {
                        notification.subTitle?.zh ?: ""
                    }
                }
                Language.TURKISH.code -> {
                    result = if (title) {
                        notification.title?.tr ?: ""
                    } else {
                        notification.subTitle?.tr ?: ""
                    }
                }
                Language.DUTCH.code -> {
                    result = if (title) {
                        notification.title?.nl ?: ""
                    } else {
                        notification.subTitle?.nl ?: ""
                    }
                }
                Language.HINDI.code -> {
                    result = if (title) {
                        notification.title?.hi ?: ""
                    } else {
                        notification.subTitle?.hi ?: ""
                    }
                }
                Language.SPANISH.code -> {
                    result = if (title) {
                        notification.title?.es ?: ""
                    } else {
                        notification.subTitle?.es ?: ""
                    }
                }
                Language.INDONESIAN.code -> {
                    result = if (title) {
                        notification.title?.ind ?: ""
                    } else {
                        notification.subTitle?.ind ?: ""
                    }
                }
                Language.GERMAN.code -> {
                    result = if (title) {
                        notification.title?.de ?: ""
                    } else {
                        notification.subTitle?.de ?: ""
                    }
                }
                Language.JAPAN.code -> {
                    result = if (title) {
                        notification.title?.jp ?: ""
                    } else {
                        notification.subTitle?.jp ?: ""
                    }
                }
                Language.PORTUGUESE.code -> {
                    result = if (title) {
                        notification.title?.pt ?: ""
                    } else {
                        notification.subTitle?.pt ?: ""
                    }
                }
                Language.BRAZILIAN.code -> {
                    result = if (title) {
                        notification.title?.br ?: ""
                    } else {
                        notification.subTitle?.br ?: ""
                    }
                }
                else -> {
                    result = if (title) {
                        notification.title?.en ?: ""
                    } else {
                        notification.subTitle?.en ?: ""
                    }
                }
            }
            return result
        }

    }
}