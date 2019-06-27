/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.request.node.TransferTransaction.Companion.MAX_ATTACHMENT_SIZE
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.arrayWithSize
import com.wavesplatform.sdk.utils.parseAlias
import java.nio.charset.Charset

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
         * Amount of Waves in satoshi
         */
        @SerializedName("amount") var amount: Long,
        /**
         * Fee for transaction in satoshi
         */
        fee: Long,
        /**
         * Additional info [0,[MAX_ATTACHMENT_SIZE]] bytes of string or byte array
         */
        @SerializedName("attachment") var attachment: String?,
        /**
         * Asset id instead Waves for transaction commission withdrawal
         */
        @SerializedName("feeAssetId") var feeAssetId: String = "")
    : BaseTransaction(TRANSFER) {

    init {
        this.fee = fee
    }

    override fun toBytes(): ByteArray {
        recipient = recipient.parseAlias()
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    SignUtil.arrayOption(assetId),
                    SignUtil.arrayOption(feeAssetId),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    getRecipientBytes(recipient),
                    SignUtil.arrayWithSize(Base58.encode((attachment ?: "").toByteArray())))
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

        fun getRecipientBytes(recipient: String): ByteArray {
            return if (recipient.length <= 30) {
                Bytes.concat(byteArrayOf(WavesConstants.VERSION.toByte()),
                        byteArrayOf(WavesSdk.getEnvironment().scheme),
                        recipient.parseAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize())
            } else {
                Base58.decode(recipient)
            }
        }
    }
}