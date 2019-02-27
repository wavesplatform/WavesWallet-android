package com.wavesplatform.sdk.model.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset
import com.wavesplatform.sdk.utils.clearAlias


class TransactionsBroadcastRequest(
        @SerializedName("assetId") var assetId: String,
        @SerializedName("senderPublicKey") var senderPublicKey: String,
        @SerializedName("recipient") var recipient: String,
        @SerializedName("amount") var amount: Long,
        @SerializedName("timestamp") var timestamp: Long,
        @SerializedName("fee") var fee: Long,
        @SerializedName("attachment") var attachment: String?,
        @SerializedName("feeAssetId") var feeAssetId: String = "") {

    @SerializedName("type")
    val type: Int = 4
    @SerializedName("version")
    val version: Int = Constants.VERSION
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
                    SignUtil.arrayWithSize(Base58.encode((attachment ?: "").toByteArray())))
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create transaction sign", e)
            ByteArray(0)
        }
    }

    private fun getRecipientBytes(recipient: String): ByteArray {
        return if (recipient.length <= 30) {
            Bytes.concat(byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(Constants.NET_CODE),
                    recipient.clearAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize())
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