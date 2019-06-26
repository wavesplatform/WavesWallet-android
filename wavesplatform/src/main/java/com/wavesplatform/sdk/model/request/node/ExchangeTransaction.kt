package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.response.node.transaction.ExchangeTransactionResponse

/**
 * Not available now
 *
 * Exchange transaction is a transaction type that creates a sell or buy order
 * for exchange by specifying the following:
 *
 * The asset.
 * The price of asset to sell or buy.
 * The amount which the user is offering.
 * The asset and the amount which the user requests in return.
 */
internal class ExchangeTransaction(
        @SerializedName("order1")
        var order1: ExchangeTransactionResponse.Order,
        @SerializedName("order2")
        var order2: ExchangeTransactionResponse.Order,
        @SerializedName("price")
        var price: Long,
        /**
         * Amount of Waves in satoshi
         */
        @SerializedName("amount")
        var amount: Long,
        @SerializedName("buyMatcherFee")
        var buyMatcherFee: Long,
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

                    Longs.toByteArray(fee),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(buyMatcherFee),
                    Longs.toByteArray(sellMatcherFee),
                    Longs.toByteArray(timestamp))


            /*['order1', {
                fromBytes: () => ({value: undefined, shift: 4}),
                toBytes: (order: any) => INT(serializerFromSchema(orderSchemaV0WithSignature)(order).length)
            }],
            ['order2', {
                fromBytes: () => ({value: undefined, shift: 4}),
                toBytes: (order: any) => INT(serializerFromSchema(orderSchemaV0WithSignature)(order).length)
            }],
            ['order1', orderSchemaV0WithSignature],
            ['order2', orderSchemaV0WithSignature],*/

            // todo check https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 414

        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Exchange Transaction", e)
            ByteArray(0)
        }
    }
}