package com.wavesplatform.sdk.model.request.node

import android.os.Parcelable
import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.response.node.transaction.ExchangeTransactionResponse
import com.wavesplatform.sdk.utils.arrayWithSize
import kotlinx.android.parcel.Parcelize

/**
 * Not available now!
 *
 * Exchange transaction matches a sell and buy orders
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
        var order1: Order,
        /**
         * 2nd order
         */
        @SerializedName("order2")
        var order2: Order,
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

    private fun orderArray(order: Order): ByteArray {
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

    @Parcelize
    class Order(
            /**
             * Id of order, hash of data, returns after send Exchange transaction
             */
            @SerializedName("id")
            var id: String,
            /**
             * Address of sender, returns after send Exchange transaction
             */
            @SerializedName("sender")
            var sender: String,
            /**
             * Account public key of the sender
             */
            @SerializedName("senderPublicKey")
            var senderPublicKey: String,
            /**
             * Matcher Public Key, available in MatcherService.matcherPublicKey() for DEX
             */
            @SerializedName("matcherPublicKey")
            var matcherPublicKey: String,
            /**
             * Exchangeable pair. We sell or buy always amount asset and we always give price asset
             */
            @SerializedName("assetPair")
            var assetPair: AssetPair,
            /**
             * Order type buy(0) or sell(1)
             */
            @SerializedName("orderType")
            var orderType: String,
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
             * Unix time of creation
             */
            @SerializedName("timestamp")
            var timestamp: Long = 0L,
            @SerializedName("expiration")
            /**
             * Unix time of order expiration. Until the time order will be work.
             */
            var expiration: Long,
            @SerializedName("matcherFee")
            /**
             * Amount matcher fee of Waves in satoshi
             */
            var matcherFee: Long,
            /**
             * Signature v1. See also [proofs]
             */
            @SerializedName("signature")
            var signature: String?,
            /**
             * Signatures v2 string set.
             * A transaction signature is a digital signature
             * with which the sender confirms the ownership of the outgoing transaction.
             * If the array is empty, then S= 3. If the array is not empty,
             * then S = 3 + 2 × N + (P1 + P2 + ... + Pn), where N is the number of proofs in the array,
             * Pn is the size on N-th proof in bytes.
             * The maximum number of proofs in the array is 8. The maximum size of each proof is 64 bytes
             */
            @SerializedName("proofs")
            val proofs: MutableList<String> = mutableListOf()
    ) : Parcelable


    @Parcelize
    class AssetPair(
            @SerializedName("amountAsset")
            var amountAsset: String,
            @SerializedName("priceAsset")
            var priceAsset: String
    ) : Parcelable
}