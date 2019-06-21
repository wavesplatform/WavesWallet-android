package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.model.request.node.BaseTransaction

class AssetScriptTransactionResponse {
    @SerializedName("type")
    val type: Int = BaseTransaction.ASSET_SCRIPT
    @SerializedName("id")
    var id: String? = null
    @SerializedName("sender")
    var sender: String = ""
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("fee")
    var fee: Long = 0L
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("height")
    var height: Long = 0L
    @SerializedName("signature")
    var signature: String = ""
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("chainId")
    val chainId: Byte = WavesPlatform.getEnvironment().scheme
    @SerializedName("version")
    var version: Int = 0

    @SerializedName("assetId")
    val assetId: String = ""
    @SerializedName("script")
    val script: String = ""
}