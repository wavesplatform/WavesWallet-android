package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class GlobalConfiguration(
    @SerializedName("name") var name: String = "",
    @SerializedName("servers") var servers: Servers = Servers(),
    @SerializedName("scheme") var scheme: String = "",
    @SerializedName("generalAssetIds") var generalAssetIds: List<GeneralAssetId> = listOf()
) {

    data class Servers(
        @SerializedName("nodeUrl") var nodeUrl: String = "",
        @SerializedName("dataUrl") var dataUrl: String = "",
        @SerializedName("spamUrl") var spamUrl: String = "",
        @SerializedName("matcherUrl") var matcherUrl: String = ""
    )

    data class GeneralAssetId(
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