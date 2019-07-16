package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.utils.WavesConstants

abstract class BaseTransactionResponse(
        /**
         * ID of the transaction type. Correct values in [1; 16]
         * see also Companion Object of BaseTransaction
         */
        @SerializedName("type") val type: Byte) {

    /**
     * Hash of all transaction data
     */
    @SerializedName("id")
    var id: String? = null

    /**
     * Sender address
     */
    @SerializedName("sender")
    var sender: String = ""

    /**
     * Account public key of the sender
     */
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""

    /**
     * Unix time of sending of transaction to blockchain
     */
    @SerializedName("timestamp")
    var timestamp: Long = 0L

    /**
     * A transaction fee is a fee that an account owner pays to send a transaction.
     * Transaction fee in WAVELET
     */
    @SerializedName("fee")
    var fee: Long = WavesConstants.WAVES_MIN_FEE

    /**
     * Determines the network where the transaction will be published to.
     * [WavesCrypto.TEST_NET_CHAIN_ID] for test network,
     * [WavesCrypto.MAIN_NET_CHAIN_ID] for main network
     */
    @SerializedName("chainId")
    val chainId: Byte? = WavesSdk.getEnvironment().chainId

    /**
     * Version number of the data structure of the transaction.
     * The value has to be equal to 2
     */
    @SerializedName("version")
    var version: Byte = 2

    /**
     * Transaction signature fo v2
     * If the array is empty, then S= 3. If the array is not empty,
     * then S = 3 + 2 × N + (P1 + P2 + ... + Pn), where N is the number of proofs in the array,
     * Pn is the size on N-th proof in bytes.
     * The maximum number of proofs in the array is 8. The maximum size of each proof is 64 bytes
     */
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()

    /**
     * Transaction signature fo v1
     */
    @SerializedName("signature")
    var signature: String = ""

    /**
     * Transaction blockchain height
     */
    @SerializedName("height")
    var height: Long? = null
}