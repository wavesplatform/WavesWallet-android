package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.Language

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
        var title: HashMap<String, String>? = hashMapOf()
        @SerializedName("subTitle")
        var subtitle: HashMap<String, String>? = hashMapOf()
    }

    companion object {
        const val URL = "https://github-proxy.wvservices.com/wavesplatform/waves-client-config/mobile/v2.2/notifications.json"
        private const val DEFAULT_LANG_CODE = "en"
        private const val PT_LANG_CODE = "pt-PT"
        private const val BR_LANG_CODE = "pt-BR"

        fun getTitle(langCode: String, notification: Notification): String {
            return getLine(langCode, notification, true)
        }

        fun getSubtitle(langCode: String, notification: Notification): String {
            return getLine(langCode, notification, false)
        }

        private fun getLine(langCode: String, notification: Notification, title: Boolean): String {

            val langKey = when (langCode) {
                Language.PORTUGUESE.code -> PT_LANG_CODE
                Language.BRAZILIAN.code -> BR_LANG_CODE
                else -> langCode
            }

            return if (title) {
                notification.title?.get(langKey)
                        ?: notification.title?.get(DEFAULT_LANG_CODE)
                        ?: ""
            } else {
                notification.subtitle?.get(langKey)
                        ?: notification.subtitle?.get(DEFAULT_LANG_CODE)
                        ?: ""
            }
        }
    }
}