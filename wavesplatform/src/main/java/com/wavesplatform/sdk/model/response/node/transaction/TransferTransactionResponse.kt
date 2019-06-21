package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction

class TransferTransactionResponse {

    @SerializedName("type")
    val type: Int = BaseTransaction.TRANSFER
    @SerializedName("assetId")
    var assetId: String = ""
    @SerializedName("recipient")
    var recipient: String = ""
    @SerializedName("amount")
    var amount: Long = 0L
    @SerializedName("attachment")
    var attachment: String = ""
    @SerializedName("feeAssetId")
    var feeAssetId: String = ""
    @SerializedName("sender")
    var sender: String = ""

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


    @SerializedName("id")
    var id: String? = ""
}