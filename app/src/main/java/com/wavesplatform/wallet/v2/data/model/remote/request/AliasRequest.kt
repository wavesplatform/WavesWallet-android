package com.wavesplatform.wallet.v2.data.model.remote.request
import com.google.gson.annotations.SerializedName



data class AliasRequest(
    @SerializedName("type") var type: Int = 0,
    @SerializedName("id") var id: String? = null,
    @SerializedName("sender") var sender: String? = null,
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("fee") var fee: Int = 0,
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("signature") var signature: String = "",
    @SerializedName("version") var version: Int? = null,
    @SerializedName("alias") var alias: String = ""
)