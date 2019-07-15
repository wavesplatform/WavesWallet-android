/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.parseAlias

/**
 * Transfer transaction sends amount of asset on address.
 * It is used to transfer a specific amount of an asset (WAVES by default)
 * to the recipient (by address or alias).
 */
class TransferTransaction(
    /**
     * Id of transferable asset in Waves blockchain, different for main and test net
     */
    @SerializedName("assetId") var assetId: String,
    /**
     * Address or alias of Waves blockchain
     */
    @SerializedName("recipient") var recipient: String,
    /**
     * Amount of asset in satoshi
     */
    @SerializedName("amount") var amount: Long,
    /**
     * Fee for transaction in satoshi
     */
    fee: Long,
    /**
     * Additional info [0,140] bytes of string encoded in Base58
     */
    @SerializedName("attachment") var attachment: String,
    /**
     * Asset id instead Waves for transaction commission withdrawal
     */
    @SerializedName("feeAssetId") var feeAssetId: String = ""
) : BaseTransaction(TRANSFER) {

    init {
        this.fee = fee
    }

    override fun toBytes(): ByteArray {
        recipient = recipient.parseAlias()

        return try {
            Bytes.concat(
                byteArrayOf(type),
                byteArrayOf(version),
                Base58.decode(senderPublicKey),
                SignUtil.arrayOption(assetId),
                SignUtil.arrayOption(feeAssetId),
                Longs.toByteArray(timestamp),
                Longs.toByteArray(amount),
                Longs.toByteArray(fee),
                SignUtil.recipientBytes(recipient, version, chainId),
                SignUtil.attachmentBytes(attachment)
            )
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Transfer Transaction", e)
            ByteArray(0)
        }
    }


    companion object {

        fun getAttachmentSize(attachment: String?): Int {
            if (attachment == null) {
                return 0
            }
            return try {
                attachment.toByteArray().size
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }

        const val MAX_ATTACHMENT_SIZE = 140
    }
}