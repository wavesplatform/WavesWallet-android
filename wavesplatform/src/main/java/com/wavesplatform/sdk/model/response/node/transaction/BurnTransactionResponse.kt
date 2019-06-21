package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.model.request.node.BaseTransaction

class BurnTransactionResponse {

    @SerializedName("type")
    val type: Int = BaseTransaction.BURN
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("fee")
    var fee: Long = 0L
    @SerializedName("version")
    var version: Int = 0
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("assetId")
    val assetId: String = ""
    @SerializedName("quantity")
    var quantity: Long = 0
    @SerializedName("chainId")
    val chainId: Byte = WavesPlatform.getEnvironment().scheme
    @SerializedName("id")
    var id: String? = null
}