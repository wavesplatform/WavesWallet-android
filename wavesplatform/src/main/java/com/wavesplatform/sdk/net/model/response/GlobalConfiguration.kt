/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class GlobalConfiguration(
    @SerializedName("name") var name: String = "",
    @SerializedName("servers") var servers: Servers = Servers(),
    @SerializedName("scheme") var scheme: String = "",
    @SerializedName("generalAssets") var generalAssets: List<ConfigAsset> = listOf(),
    @SerializedName("assets") var assets: List<ConfigAsset> = listOf()
) {

    data class Servers(
        @SerializedName("nodeUrl") var nodeUrl: String = "",
        @SerializedName("dataUrl") var dataUrl: String = "",
        @SerializedName("spamUrl") var spamUrl: String = "",
        @SerializedName("matcherUrl") var matcherUrl: String = ""
    )

    data class ConfigAsset(
        @SerializedName("assetId") var assetId: String = "",
        @SerializedName("displayName") var displayName: String = "",
        @SerializedName("isFiat") var isFiat: Boolean = false,
        @SerializedName("isGateway") var isGateway: Boolean = false,
        @SerializedName("wavesId") var wavesId: String = "",
        @SerializedName("gatewayId") var gatewayId: String = "",
        @SerializedName("iconUrls") var iconUrls: IconUrls = IconUrls(),
        @SerializedName("addressRegEx") var addressRegEx: String = ""
    ) {
        data class IconUrls(@SerializedName("default") var default: String = "")
    }
}