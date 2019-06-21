package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.model.request.node.BaseTransaction

class CancelLeasingTransactionResponse {

    @SerializedName("type")
    var type: Int = BaseTransaction.CANCEL_LEASING
    @SerializedName("id")
    var id: String? = ""
    @SerializedName("sender")
    var sender: String = ""
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("fee")
    var fee: Long = 0L
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("version")
    var version: Int = 0
    @SerializedName("height")
    var height: Long = 0L
    @SerializedName("signature")
    var signature: String = ""
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("chainId")
    val chainId: Byte = WavesPlatform.getEnvironment().scheme
    @SerializedName("leaseId")
    var leaseId: String = ""



    @SerializedName("lease")
    var lease: Unit? = null
}