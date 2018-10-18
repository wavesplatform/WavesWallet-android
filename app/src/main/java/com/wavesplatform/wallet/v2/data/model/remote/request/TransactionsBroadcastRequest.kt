package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.util.SignUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.arrayWithSize
import com.wavesplatform.wallet.v2.util.clearAlias


class TransactionsBroadcastRequest(
        @SerializedName("assetId") var assetId: String,
        @SerializedName("senderPublicKey") var senderPublicKey: String,
        @SerializedName("recipient") var recipient: String,
        @SerializedName("amount") var amount: Long,
        @SerializedName("timestamp") var timestamp: Long,
        @SerializedName("fee") var fee: Long,
        @SerializedName("attachment") var attachment: String?) {

    @SerializedName("type")
    val type: Int = 4
    @SerializedName("version")
    val version: Int = Constants.VERSION
    @SerializedName("feeAssetId")
    var feeAssetId: String? = ""
    @SerializedName("proofs")
    var proofs = arrayOf("")
    @SerializedName("signature")
    var signature: String = ""

    @SerializedName("sender")
    var sender: String? = ""
    @SerializedName("id")
    var id: String? = ""

    private fun toSignBytes(): ByteArray {
        recipient = recipient.clearAlias()
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
            Log.e("Wallet", "Couldn't create seed", e)
            ByteArray(0)
        }
    }

    private fun getRecipientBytes(recipient: String): ByteArray {
        return if (recipient.length <= 30) {
            val recipientByte = recipient.toByteArray()
            Bytes.concat(
                    byteArrayOf(Constants.ALIAS_VERSION.toByte()),
                    byteArrayOf(Constants.ADDRESS_SCHEME.toByte()),
                    recipientByte.arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }

    fun sign(privateKey: ByteArray) {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        proofs[0] = signature
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