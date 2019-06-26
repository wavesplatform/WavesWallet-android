package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.request.node.TransferTransaction.Companion.MAX_ATTACHMENT_SIZE
import com.wavesplatform.sdk.model.response.node.transaction.MassTransferTransactionResponse
import com.wavesplatform.sdk.utils.SignUtil


/**
 * Mass-Transfer transaction sends a lot of transactions of asset for recipients set
 *
 * Fee depends of mass transactions count
 * 0.001 + 0.0005 × N, N is the number of transfers inside of a transaction
 */
internal class MassTransferTransaction(
        /**
         * Id of transferable asset in Waves blockchain, different for main and test net
         */
        @SerializedName("assetId") var assetId: String = "",
        /**
         * Additional info [0,[MAX_ATTACHMENT_SIZE]] bytes of string or byte array
         */
        @SerializedName("attachment") var attachment: String,
        @SerializedName("transferCount") var transferCount: Int, // todo check
        @SerializedName("totalAmount") var totalAmount: Long, // todo check
        /**
         * Collection of recipients with amount each
         */
        @SerializedName("transfers") var transfers: Array<MassTransferTransactionResponse.Transfer>)
    : BaseTransaction(MASS_TRANSFER) {

    override fun toBytes(): ByteArray {


        val transfers = listOf<Transfers>()

        val byteList = arrayListOf<ByteArray>()
        for (transfer in transfers) {
            val recipient = TransferTransaction.getRecipientBytes(transfer.recipient)
            byteList.add(recipient)
            val amount = transfer.amount
            byteList.add(Longs.toByteArray(amount))
        }



        //TransferTransaction.getRecipientBytes()


        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),

                    // todo txFields.transfers,

                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(fee),
                    SignUtil.arrayWithSize(Base58.encode(attachment.toByteArray())))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Mass Transfer Transaction", e)
            ByteArray(0)
        }
    }

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    private fun bytesTransfers(transfers: List<Transfers>) {
        for (transfer in transfers) {
            val recipient = TransferTransaction.getRecipientBytes(transfer.recipient)
            val amount = transfer.amount
        }
    }

    class Transfers(var recipient: String = "",
                    var amount: Long = 0L
    )
}