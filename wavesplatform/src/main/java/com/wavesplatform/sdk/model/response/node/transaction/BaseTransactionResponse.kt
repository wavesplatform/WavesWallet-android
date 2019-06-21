package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction

abstract class BaseTransactionResponse {

    @SerializedName("type")
    val type: Int = BaseTransaction.CREATE_ALIAS
    @SerializedName("id")
    var id: String? = null
    @SerializedName("sender")
    var sender: String = ""
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("fee")
    var fee: Long = 0L
    @SerializedName("version")
    var version: Int = 2
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("signature")
    var signature: String = ""
    @SerializedName("height")
    var height: Long = 0L
}