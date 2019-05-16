/*
 * Created by Ershov Aleksandr on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

class NewsResponse {

    @SerializedName("notifications")
    var notifications: List<NotificationResponse> = listOf()

    class NotificationResponse {

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
        const val URL_TEST = "https://github-proxy.wvservices.com/wavesplatform/waves-client-config/mobile/v2.3/notifications_test_android.json"
        private const val DEFAULT_LANG_CODE = "en"

        fun getTitle(langCode: String, notification: NotificationResponse): String {
            return notification.title?.get(langCode)
                    ?: notification.title?.get(DEFAULT_LANG_CODE)
                    ?: ""
        }

        fun getSubtitle(langCode: String, notification: NotificationResponse): String {
            return notification.subtitle?.get(langCode)
                    ?: notification.subtitle?.get(DEFAULT_LANG_CODE)
                    ?: ""
        }
    }
}