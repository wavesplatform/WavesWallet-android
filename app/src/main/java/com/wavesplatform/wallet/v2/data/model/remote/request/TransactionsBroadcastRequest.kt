/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.SignUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.arrayWithSize
import com.wavesplatform.wallet.v2.util.makeAsAlias
import com.wavesplatform.wallet.v2.util.parseAlias
import java.nio.charset.Charset

class TransactionsBroadcastRequest(
        @SerializedName("assetId") var assetId: String,
        @SerializedName("senderPublicKey") var senderPublicKey: String,
        @SerializedName("recipient") var recipient: String,
        @SerializedName("amount") var amount: Long,
        @SerializedName("timestamp") var timestamp: Long,
        @SerializedName("fee") var fee: Long,
        @SerializedName("attachment") var attachment: String?,
        @SerializedName("feeAssetId") var feeAssetId: String? = "",
        @SerializedName("sender") var sender: String? = ""
) {

    @SerializedName("type")
    val type: Int = 4
    @SerializedName("version")
    var version: Int = Constants.VERSION
    @Transient
    var scheme: Byte = EnvironmentManager.netCode
    @SerializedName("proofs")
    var proofs = arrayOf("")
    @SerializedName("signature")
    var signature: String? = null
    @SerializedName("id")
    var id: String? = null

    private fun checkAliasAndAttachment() {
        attachment = Base58.encode((attachment ?: "").toByteArray())
        if (recipient.length <= 30) {
            recipient = recipient.makeAsAlias()
        }
    }

    private fun toSignBytes(): ByteArray {
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
                    SignUtil.arrayWithSize(attachment))
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create transaction sign", e)
            ByteArray(0)
        }
    }

    private fun getRecipientBytes(recipient: String): ByteArray {
        return if (recipient.length <= 30) {
            Bytes.concat(byteArrayOf(version.toByte()),
                    byteArrayOf(scheme),
                    recipient.toByteArray(Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }

    fun sign(privateKey: ByteArray) {
        checkAliasAndAttachment()

        val signedBytes = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        proofs[0] = signedBytes
        signature = signedBytes
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
    }
}