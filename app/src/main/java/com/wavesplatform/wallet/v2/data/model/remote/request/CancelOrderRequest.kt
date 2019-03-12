package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log

import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider

class CancelOrderRequest(
    @SerializedName("orderId") var orderId: String? = "",
    @SerializedName("sender") var sender: String? = "",
    @SerializedName("signature") var signature: String? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    Base58.decode(sender),
                    Base58.decode(orderId)
            )
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create CancelOrderRequest bytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
    }
}
