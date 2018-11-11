package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider

data class CancelLeasingRequest(
        @SerializedName("type") val type: Int = 9,
        @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
        @SerializedName("txId") var txId: String = "",
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String? = null,
        @SerializedName("fee") var fee: Long = 0
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    Base58.decode(senderPublicKey),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    Base58.decode(txId)
            )
        } catch (e: Exception) {
            Log.e("CancelLeasingRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature.isNullOrEmpty()) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

}