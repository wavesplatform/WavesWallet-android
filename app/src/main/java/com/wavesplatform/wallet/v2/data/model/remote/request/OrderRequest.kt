package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import pers.victor.ext.currentTimeMillis

data class OrderRequest(
    @SerializedName("matcherPublicKey") var matcherPublicKey: String? = "",
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("assetPair") var assetPair: OrderBook.Pair = OrderBook.Pair(),
    @SerializedName("orderType") var orderType: OrderType = OrderType.BUY,
    @SerializedName("price") var price: Long = 0L,
    @SerializedName("amount") var amount: Long = 0L,
    @SerializedName("timestamp") var timestamp: Long = currentTimeMillis,
    @SerializedName("expiration") var expiration: Long = 0L,
    @SerializedName("matcherFee") var matcherFee: Long = 300000,
    @SerializedName("version") var version: Int = Constants.VERSION,
    @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(Constants.VERSION.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(matcherPublicKey),
                    assetPair.toBytes(),
                    orderType.toBytes(),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(expiration),
                    Longs.toByteArray(matcherFee))
        } catch (e: Exception) {
            Log.e("OrderRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}