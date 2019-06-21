package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.utils.WavesConstants

abstract class BaseTransactionResponse(@SerializedName("type") val type: Int) {
    @SerializedName("id")
    var id: String? = null
    @SerializedName("sender")
    var sender: String = ""
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""
    @SerializedName("timestamp")
    var timestamp: Long = 0L
    @SerializedName("fee")
    var fee: Long = WavesConstants.WAVES_MIN_FEE
    @SerializedName("version")
    var version: Int = 2
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()
    @SerializedName("signature")
    var signature: String = ""
    @SerializedName("height")
    var height: Long? = null
    @SerializedName("chainId")
    val chainId: Byte? = WavesPlatform.getEnvironment().scheme
}