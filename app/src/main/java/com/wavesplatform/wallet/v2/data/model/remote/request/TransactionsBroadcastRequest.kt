package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.util.SignUtil


class TransactionsBroadcastRequest(
        @SerializedName("assetId") var assetId: String,
        @SerializedName("senderPublicKey") var senderPublicKey: String,
        @SerializedName("recipient") var recipient: String,
        @SerializedName("amount") var amount: Long,
        @SerializedName("timestamp") var timestamp: Long,
        @SerializedName("fee") var fee: Long,
        attachment: String?) {

    @SerializedName("type")
    val type: Int = 4
    @SerializedName("version")
    val version: Int = API_VERSION
    @SerializedName("feeAssetId")
    var feeAssetId: String? = ""
    @SerializedName("attachment")
    var attachment: String = attachment ?: ""
    @SerializedName("proofs")
    var proofs = arrayOf("")
    @SerializedName("signature")
    var signature: String = ""

    @SerializedName("sender")
    var sender: String? = null
    @SerializedName("id")
    var id: String? = null

    private fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    SignUtil.arrayOption(assetId),
                    SignUtil.arrayOption(feeAssetId),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    Base58.decode(recipient),
                    SignUtil.arrayWithSize(attachment))
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create seed", e)
            ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        proofs[0] = signature
    }

    companion object {
        const val API_VERSION = 2

        fun getAttachmentSize(attachment: String?): Int {
            if (attachment == null) {
                return 0
            }
            return try {
                Base58.decode(attachment).size
            } catch (invalidBase58: Base58.InvalidBase58) {
                invalidBase58.printStackTrace()
                0
            }
        }
    }
}