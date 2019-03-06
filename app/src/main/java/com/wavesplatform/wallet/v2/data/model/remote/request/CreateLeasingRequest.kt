package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.arrayWithSize
import com.wavesplatform.wallet.v2.util.clearAlias
import java.nio.charset.Charset

data class CreateLeasingRequest(
    @SerializedName("type") val type: Int = Transaction.LEASE,
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("scheme") var scheme: String? = EnvironmentManager.globalConfiguration.scheme,
    @SerializedName("amount") var amount: Long = 0,
    @SerializedName("fee") var fee: Long = 0,
    @SerializedName("recipient") var recipient: String = "",
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("version") var version: Int = Constants.VERSION,
    @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    fun toSignBytes(recipientIsAlias: Boolean): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(0.toByte()),
                    Base58.decode(senderPublicKey),
                    resolveRecipientBytes(recipientIsAlias),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("CreateLeasingRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    private fun resolveRecipientBytes(recipientIsAlias: Boolean): ByteArray? {
        return if (recipientIsAlias) {
            Bytes.concat(byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(EnvironmentManager.netCode),
                    recipient.clearAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize())
        } else {
            Base58.decode(recipient)
        }
    }

    fun sign(privateKey: ByteArray, recipientIsAlias: Boolean) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes(recipientIsAlias))))
    }
}