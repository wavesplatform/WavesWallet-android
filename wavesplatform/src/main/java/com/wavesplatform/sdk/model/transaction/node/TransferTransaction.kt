/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.transaction.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.arrayWithSize
import com.wavesplatform.sdk.utils.parseAlias
import java.nio.charset.Charset

class TransferTransaction(
    @SerializedName("assetId") var assetId: String,
    @SerializedName("recipient") var recipient: String,
    @SerializedName("amount") var amount: Long,
    fee: Long,
    @SerializedName("attachment") var attachment: String?,
    @SerializedName("feeAssetId") var feeAssetId: String = "")
    : BaseTransaction(TRANSFER) {

    @SerializedName("sender")
    var sender: String? = ""
    @SerializedName("id")
    var id: String? = ""

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

    private fun getRecipientBytes(recipient: String): ByteArray {
        return if (recipient.length <= 30) {
            Bytes.concat(byteArrayOf(WavesConstants.VERSION.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    recipient.parseAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
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

        const val SignatureLength = 64
        const val KeyLength = 32
        const val MaxAttachmentSize = 140
        const val MinFee = 100000
    }
}