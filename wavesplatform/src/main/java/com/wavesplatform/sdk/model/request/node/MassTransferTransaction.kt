package com.wavesplatform.sdk.model.request.node

import android.os.Parcelable
import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.node.TransferTransaction.Companion.MAX_ATTACHMENT_SIZE
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.arrayWithSize
import kotlinx.android.parcel.Parcelize


/**
 * Not available now
 *
 * Mass-Transfer transaction sends a lot of transactions of asset for recipients set
 *
 * Transfer transaction is used to combine several ordinary transfer transactions
 * that share single sender and asset ID (it has a list of recipients,
 * and an amount to be transferred to each recipient).
 * The maximum number of recipients in a single transaction is 100.
 *
 * The transfers to self are allowed, as well as zero valued transfers.
 * In the recipients list, a recipient can occur several times, this is not considered an error.
 *
 * Fee depends of mass transactions count
 * 0.001 + 0.0005 × N, N is the number of transfers inside of a transaction
 */
class MassTransferTransaction(
        /**
         * Id of transferable asset in Waves blockchain, different for main and test net
         */
        @SerializedName("assetId") var assetId: String?,
        /**
         * Additional info in Base58 converted string
         * [0,[MAX_ATTACHMENT_SIZE]] bytes of string or byte array
         */
        @SerializedName("attachment") var attachment: String,
        /**
         * Collection of recipients with amount each
         */
        @SerializedName("transfers") var transfers: List<Transfer> = mutableListOf())
    : BaseTransaction(MASS_TRANSFER) {

    /**
     * Total count of transfers, optional
     */
    @SerializedName("transferCount") var transferCount: Int = 0
    /**
     * Total amount of transfers, optional
     */
    @SerializedName("totalAmount") var totalAmount: Long? = null

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    override fun toBytes(): ByteArray {

        val assetIdArray = if (assetId.isNullOrEmpty()) {
            byteArrayOf(0)
        } else {
            SignUtil.arrayOption(assetId!!)
        }

        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    assetIdArray,
                    transfersArray(),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(fee),
                    WavesCrypto.base58decode(attachment).arrayWithSize())
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Mass Transfer Transaction", e)
            ByteArray(0)
        }
    }

    private fun transfersArray(): ByteArray {
        var recipientAmountChainArray = byteArrayOf()
        for (transfer in transfers) {
            val recipient = TransferTransaction.getRecipientBytes(transfer.recipient)
            val amount = Longs.toByteArray(transfer.amount)
            recipientAmountChainArray = Bytes.concat(recipientAmountChainArray, recipient, amount)
        }
        val lengthBytes = Shorts.toByteArray(transfers.size.toShort())
        return Bytes.concat(lengthBytes, recipientAmountChainArray)
    }

    @Parcelize
    class Transfer(
            /**
             * Address or alias of Waves blockchain
             */
            @SerializedName("recipient") var recipient: String = "",
            /**
             * Amount of asset in satoshi
             */
            @SerializedName("amount") var amount: Long = 0L) : Parcelable
}