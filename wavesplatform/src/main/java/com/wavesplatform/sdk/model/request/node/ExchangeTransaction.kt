package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.response.node.transaction.ExchangeTransactionResponse
import com.wavesplatform.sdk.utils.arrayWithSize

/**
 * Not available now
 *
 * Exchange transaction is a transaction type that creates a sell or buy order
 * for exchange by specifying the following:
 *
 * The asset.
 * The price of asset to sell(1) or buy(0).
 * The amount which the user is offering.
 * The asset and the amount which the user requests in return.
 */
internal class ExchangeTransaction(
        /**
         * 1st order
         */
        @SerializedName("order1")
        var order1: ExchangeTransactionResponse.Order,
        /**
         * 2nd order
         */
        @SerializedName("order2")
        var order2: ExchangeTransactionResponse.Order,
        /**
         * Price for amount
         */
        @SerializedName("price")
        var price: Long,
        /**
         * Amount of asset in satoshi
         */
        @SerializedName("amount")
        var amount: Long,
        /**
         * Fee from buyer order to mutcher
         */
        @SerializedName("buyMatcherFee")
        var buyMatcherFee: Long,
        /**
         * Fee from seller order to mutcher
         */
        @SerializedName("sellMatcherFee")
        var sellMatcherFee: Long)
    : BaseTransaction(EXCHANGE) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(0.toByte()),
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    orderArray(order1),
                    orderArray(order2),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(buyMatcherFee),
                    Longs.toByteArray(sellMatcherFee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Exchange Transaction", e)
            ByteArray(0)
        }
    }

    private fun orderArray(order: ExchangeTransactionResponse.Order): ByteArray {
        val schema1 = Bytes.concat(
                Base58.decode(order.senderPublicKey),
                Base58.decode(order.matcherPublicKey),
                Base58.decode(order.assetPair.amountAsset),
                Base58.decode(order.assetPair.priceAsset),
                byteArrayOf(order.orderType[0].toByte()),
                Longs.toByteArray(order.price),
                Longs.toByteArray(order.amount),
                Longs.toByteArray(order.timestamp),
                Longs.toByteArray(order.expiration),
                Longs.toByteArray(order.matcherFee))

        return Bytes.concat(
                byteArrayOf(1),
                Bytes.concat(
                        byteArrayOf(1),
                        schema1,
                        Base58.decode(order.proofs[0]))
                        .arrayWithSize(),
                byteArrayOf(2),
                Bytes.concat(
                        byteArrayOf(version.toByte()),
                        schema1,
                        byteArrayOf(1),
                        Base58.decode(order.proofs[0]))
                        .arrayWithSize(),
                byteArrayOf(version.toByte()),
                byteArrayOf(4)
        )
    }
}