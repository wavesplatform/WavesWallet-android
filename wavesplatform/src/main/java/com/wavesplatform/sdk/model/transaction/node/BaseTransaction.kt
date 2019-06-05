package com.wavesplatform.sdk.model.transaction.node

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.Wavesplatform
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

    fun sign(privateKey: String) {
        if (senderPublicKey == "") {
            senderPublicKey = Wavesplatform.crypto().publicKey(privateKey)
        }
        if (timestamp == 0L) {
            timestamp = Wavesplatform.getEnvironment().getTime()
        }
        if (fee == 0L) {
            fee = WavesConstants.WAVES_MIN_FEE
        }
        if (version == 0) {
            version = WavesConstants.VERSION
        }
        proofs.add(getSignedString(privateKey))
    }

    fun getSignedBytes(privateKey: String): ByteArray {
        return Wavesplatform.crypto().signBytesWithSeed(toBytes(), privateKey)
    }

    fun getSignedString(privateKey: String): String {
        return Wavesplatform.crypto().base58encode(getSignedBytes(privateKey))
    }

    companion object {

        const val GENESIS = 1
        const val PAYMENT = 2
        const val ISSUE = 3
        const val TRANSFER = 4
        const val REISSUE = 5
        const val BURN = 6
        const val EXCHANGE = 7
        const val LEASE = 8
        const val LEASE_CANCEL = 9
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
                PAYMENT -> "PaymentResponse"
                ISSUE -> "Issue"
                TRANSFER -> "TransferResponse"
                REISSUE -> "Reissue"
                BURN -> "Burn"
                EXCHANGE -> "Exchange"
                LEASE -> "LeaseResponse"
                LEASE_CANCEL -> "LeaseResponse Cancel"
                CREATE_ALIAS -> "Create AliasResponse"
                MASS_TRANSFER -> "Mass TransferResponse"
                DATA -> "DataResponse"
                ADDRESS_SCRIPT -> "Address Script"
                SPONSORSHIP -> "SponsorShip"
                ASSET_SCRIPT -> "Asset Script"
                SCRIPT_INVOCATION -> "Script Invocation"
                else -> ""
            }
        }
    }
}
