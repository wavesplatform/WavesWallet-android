/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.matcher

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.model.response.matcher.OrderBookResponse
import com.wavesplatform.sdk.WavesPlatform

data class CreateOrderRequest(
        @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("assetPair") var assetPair: OrderBookResponse.PairResponse = OrderBookResponse.PairResponse(),
        @SerializedName("orderType") var orderType: Int = 0,
        @SerializedName("price") var price: Long = 0L,
        @SerializedName("amount") var amount: Long = 0L,
        @SerializedName("timestamp") var timestamp: Long = WavesPlatform.getEnvironment().getTime(),
        @SerializedName("expiration") var expiration: Long = 0L,
        @SerializedName("matcherFee") var matcherFee: Long = 300000,
        @SerializedName("version") var version: Int = WavesConstants.VERSION,
        @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    private fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(WavesConstants.VERSION.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(matcherPublicKey),
                    assetPair.toBytes(),
                    byteArrayOf(orderType.toByte()),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(expiration),
                    Longs.toByteArray(matcherFee))
        } catch (e: Exception) {
            Log.e("CreateOrderRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}