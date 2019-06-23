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
 */
class MassTransferTransaction(
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
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    // todo txFields.transfers,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    SignUtil.arrayWithSize(Base58.encode(attachment.toByteArray())))
            // todo check https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 463
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Mass Transfer Transaction", e)
            ByteArray(0)
        }
    }
}