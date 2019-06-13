package com.wavesplatform.sdk.model.transaction.node

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.WavesConstants

abstract class BaseTransaction(@SerializedName("type")
                               val type: Int) {

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

    abstract fun toBytes(): ByteArray

    fun sign(seed: String) {
        if (senderPublicKey == "") {
            senderPublicKey = WavesCrypto.publicKey(seed)
        }
        if (timestamp == 0L) {
            timestamp = WavesPlatform.getEnvironment().getTime()
        }
        if (fee == 0L) {
            fee = WavesConstants.WAVES_MIN_FEE
        }
        if (version == 0) {
            version = WavesConstants.VERSION
        }
        proofs.add(getSignedStringWithSeed(seed))
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

        private fun getNameBy(type: Int): String {
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
                SPONSORSHIP -> "SponsorShip"
                ASSET_SCRIPT -> "Asset Script"
                SCRIPT_INVOCATION -> "Script Invocation"
                else -> ""
            }
        }
    }
}
