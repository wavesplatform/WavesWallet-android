package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.util.arrayWithSize
import java.nio.charset.Charset

data class CreateLeasingRequest(
        @SerializedName("type") val type: Int = 8,
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("fee") var fee: Long = 0,
        @SerializedName("recipient") var recipient: String = "",
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String? = null,
        @Transient var isAlias: Boolean = false
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    Base58.decode(senderPublicKey),
                    Bytes.concat(byteArrayOf(Constants.VERSION.toByte()),
                            byteArrayOf(Constants.ADDRESS_SCHEME.toByte()),
                            recipient.toByteArray(Charset.forName("UTF-8")).arrayWithSize()).arrayWithSize(),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("CreateLeasingRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

}