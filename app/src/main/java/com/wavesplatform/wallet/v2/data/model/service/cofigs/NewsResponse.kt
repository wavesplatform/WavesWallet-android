/*
 * Created by Ershov Aleksandr on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.service.cofigs

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.Constants

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
        fun getTitle(langCode: String, notification: NotificationResponse): String {
            return notification.title?.get(langCode)
                    ?: notification.title?.get(Constants.News.DEFAULT_LANG_CODE)
                    ?: ""
        }

        fun getSubtitle(langCode: String, notification: NotificationResponse): String {
            return notification.subtitle?.get(langCode)
                    ?: notification.subtitle?.get(Constants.News.DEFAULT_LANG_CODE)
                    ?: ""
        }
    }
}