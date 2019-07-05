package com.wavesplatform.sdk.model.request.node

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.WavesConstants

/**
 * Base transaction
 */
abstract class BaseTransaction(
        /**
         * ID of the transaction type. Correct values in [1; 16] @see[BaseTransaction.Companion]
         */
        @SerializedName("type") val type: Int) {

    /**
     * Account public key of the sender in Base58
     */
    @SerializedName("senderPublicKey")
    var senderPublicKey: String = ""

    /**
     * Unix time of sending of transaction to blockchain, must be in current time +/- 1.5 hour
     */
    @SerializedName("timestamp")
    var timestamp: Long = 0L

    /**
     * A transaction fee is a fee that an account owner pays to send a transaction.
     * Transaction fee in WAVELET
     * [Wiki about Fee](https://docs.wavesplatform.com/en/blockchain/transaction-fee.html)
     */
    @SerializedName("fee")
    var fee: Long = 0L

    /**
     * Version number of the data structure of the transaction.
     * The value has to be equal to 1, 2 or 3
     */
    @SerializedName("version")
    var version: Int = WavesConstants.VERSION

    /**
     * Signatures v2 string set.
     * A transaction signature is a digital signature
     * with which the sender confirms the ownership of the outgoing transaction.
     * If the array is empty, then S= 3. If the array is not empty,
     * then S = 3 + 2 × N + (P1 + P2 + ... + Pn), where N is the number of proofs in the array,
     * Pn is the size on N-th proof in bytes.
     * The maximum number of proofs in the array is 8. The maximum size of each proof is 64 bytes
     */
    @SerializedName("proofs")
    val proofs: MutableList<String> = mutableListOf()

    /**
     * Signature v1. See also [proofs]
     */
    @SerializedName("signature")
    var signature: String? = null

    /**
     * Determines the network where the transaction will be published to.
     * T or 84 in bytes for test network,
     * W or 87 in for main network
     */
    @SerializedName("chainId")
    var chainId: Byte = WavesSdk.getEnvironment().chainId

    /**
     * Gets bytes array to sign of the transaction
     */
    abstract fun toBytes(): ByteArray

    /**
     * Sign the transaction with seed-phrase with current time if null
     * and [WavesConstants.WAVES_MIN_FEE] if it equals 0
     * @param seed Seed-phrase
     */
    open fun sign(seed: String): String {
        if (senderPublicKey == "") {
            senderPublicKey = WavesCrypto.publicKey(seed)
        }
        if (timestamp == 0L) {
            timestamp = WavesSdk.getEnvironment().getTime()
        }
        if (fee == 0L) {
            fee = WavesConstants.WAVES_MIN_FEE
        }
        proofs.clear()
        val signature = getSignedStringWithSeed(seed)
        proofs.add(signature)
        return signature
    }

    fun getSignedBytesWithSeed(seed: String): ByteArray {
        return WavesCrypto.signBytesWithSeed(toBytes(), seed)
    }

    fun getSignedStringWithSeed(seed: String): String {
        return WavesCrypto.base58encode(getSignedBytesWithSeed(seed))
    }

    fun getSignedBytesWithPrivateKey(privateKey: String): ByteArray {
        return WavesCrypto.signBytesWithPrivateKey(toBytes(), privateKey)
    }

    fun getSignedStringWithPrivateKey(privateKey: String): String {
        return WavesCrypto.base58encode(getSignedBytesWithPrivateKey(privateKey))
    }

    companion object {

        const val GENESIS = 1 // Not using
        const val PAYMENT = 2 // Not using
        const val ISSUE = 3
        const val TRANSFER = 4
        const val REISSUE = 5
        const val BURN = 6
        const val EXCHANGE = 7
        const val CREATE_LEASING = 8
        const val CANCEL_LEASING = 9
        const val CREATE_ALIAS = 10
        const val MASS_TRANSFER = 11
        const val DATA = 12
        const val ADDRESS_SCRIPT = 13
        const val SPONSORSHIP = 14
        const val ASSET_SCRIPT = 15
        const val SCRIPT_INVOCATION = 16

        const val SET_SCRIPT_LANG_VERSION: Byte = 1

        fun getNameBy(type: Int): String {
            return when (type) {
                GENESIS -> "Genesis"
                PAYMENT -> "Payment"
                ISSUE -> "Issue"
                TRANSFER -> "Transfer"
                REISSUE -> "Reissue"
                BURN -> "Burn"
                EXCHANGE -> "Exchange"
                CREATE_LEASING -> "Create Leasing"
                CANCEL_LEASING -> "Cancel Leasing"
                CREATE_ALIAS -> "Create Alias"
                MASS_TRANSFER -> "MassTransfer"
                DATA -> "Data"
                ADDRESS_SCRIPT -> "Address Script"
                SPONSORSHIP -> "Sponsorship"
                ASSET_SCRIPT -> "Asset Script"
                SCRIPT_INVOCATION -> "Script Invocation"
                else -> ""
            }
        }
    }
}
