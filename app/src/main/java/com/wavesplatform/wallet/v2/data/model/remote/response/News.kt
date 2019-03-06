package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

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
        const val URL = "https://github-proxy.wvservices.com/wavesplatform/waves-client-config/mobile/v2.2/notifications_android.json"
        private const val DEFAULT_LANG_CODE = "en"

        fun getTitle(langCode: String, notification: Notification): String {
            return notification.title?.get(langCode)
                    ?: notification.title?.get(DEFAULT_LANG_CODE)
                    ?: ""
        }

        fun getSubtitle(langCode: String, notification: Notification): String {
            return notification.subtitle?.get(langCode)
                    ?: notification.subtitle?.get(DEFAULT_LANG_CODE)
                    ?: ""
        }
    }
}