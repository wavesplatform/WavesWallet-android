/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.OrderType
import com.wavesplatform.sdk.model.TransactionType
import com.wavesplatform.sdk.utils.*
import pers.victor.ext.date
import java.math.BigInteger


open class LeaseResponse(
        @SerializedName("type") var type: Int = 0,
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("fee") var fee: Int = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("signature") var signature: String = "",
        @SerializedName("version") var version: Int = 0,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("recipient") var recipient: String = "",
        @SerializedName("recipientAddress") var recipientAddress: String? = ""
)


open class OrderResponse(
        @SerializedName("id") var id: String = "",
        @SerializedName("sender") var sender: String = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
        @SerializedName("assetPair") var assetPair: AssetPairResponse? = AssetPairResponse(),
        @SerializedName("orderType") var orderType: String = "",
        @SerializedName("price") var price: Long = 0,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("timestamp") var timestamp: Long = 0,
        @SerializedName("expiration") var expiration: Long = 0,
        @SerializedName("matcherFee") var matcherFee: Long = 0,
        @SerializedName("signature") var signature: String = ""
) {

    fun getType(): OrderType {
        return when (orderType) {
            WavesConstants.BUY_ORDER_TYPE -> OrderType.BUY
            WavesConstants.SELL_ORDER_TYPE -> OrderType.SELL
            else -> OrderType.BUY
        }
    }
}


open class AssetPairResponse(
        @SerializedName("amountAsset") var amountAsset: String? = "",
        @SerializedName("amountAssetObject") var amountAssetObject: AssetInfoResponse? = AssetInfoResponse(),
        @SerializedName("priceAsset") var priceAsset: String? = "",
        @SerializedName("priceAssetObject") var priceAssetObject: AssetInfoResponse? = AssetInfoResponse()
)

open class PaymentResponse(
        @SerializedName("amount")
        var amount: Long = 0,
        @SerializedName("assetId")
        var assetId: String? = null,
        @SerializedName("asset")
        var asset: AssetInfoResponse? = AssetInfoResponse()
)

open class TransactionResponse(
        @SerializedName("type")
        var type: Int = 0,
        @SerializedName("id")
        var id: String = "",
        @SerializedName("sender")
        var sender: String = "",
        @SerializedName("senderPublicKey")
        var senderPublicKey: String = "",
        @SerializedName("timestamp")
        var timestamp: Long = 0,
        @SerializedName("amount")
        var amount: Long = 0,
        @SerializedName("signature")
        var signature: String = "",
        @SerializedName("recipient")
        var recipient: String = "",
        @SerializedName("recipientAddress")
        var recipientAddress: String? = "",
        @SerializedName("assetId")
        var assetId: String? = "",
        @SerializedName("leaseId")
        var leaseId: String? = "",
        @SerializedName("alias")
        var alias: String? = "",
        @SerializedName("attachment")
        var attachment: String? = "",
        @SerializedName("status")
        var status: String? = "",
        @SerializedName("lease")
        var lease: LeaseResponse? = LeaseResponse(),
        @SerializedName("fee")
        var fee: Long = 0,
        @SerializedName("feeAssetId")
        var feeAssetId: String? = null,
        @SerializedName("feeAssetObject")
        var feeAssetObject: AssetInfoResponse? = AssetInfoResponse(),
        @SerializedName("quantity")
        var quantity: Long = 0,
        @SerializedName("price")
        var price: Long = 0,
        @SerializedName("height")
        var height: Long = 0,
        @SerializedName("reissuable")
        var reissuable: Boolean = false,
        @SerializedName("buyMatcherFee")
        var buyMatcherFee: Long = 0,
        @SerializedName("sellMatcherFee")
        var sellMatcherFee: Long = 0,
        @SerializedName("order1")
        var order1: OrderResponse? = OrderResponse(),
        @SerializedName("order2")
        var order2: OrderResponse? = OrderResponse(),
        @SerializedName("totalAmount")
        var totalAmount: Long = 0,
        @SerializedName("transfers")
        var transfers: List<TransferResponse> = mutableListOf(),
        @SerializedName("data")
        var data: List<DataResponse> = mutableListOf(),
        @SerializedName("isPending")
        var isPending: Boolean = false,
        @SerializedName("script")
        var script: String? = "",
        @SerializedName("minSponsoredAssetFee")
        var minSponsoredAssetFee: String? = "",
        @SerializedName("payment")
        var payment: List<PaymentResponse> = mutableListOf(),
        @SerializedName("dApp")
        var dApp: String? = "",
        var transactionTypeId: Int = 0,
        var asset: AssetInfoResponse? = AssetInfoResponse()
) {

    val decimals: Int
        get() = 8

    fun getOrderSum(): Long {
        if (transactionType() == TransactionType.EXCHANGE_TYPE) {
            return BigInteger.valueOf(amount).multiply(BigInteger.valueOf(price)).divide(BigInteger.valueOf(100000000)).toLong()
        }
        return 0
    }

    fun isSponsorshipTransaction(): Boolean {
        return transactionType() == TransactionType.RECEIVE_SPONSORSHIP_TYPE ||
                transactionType() == TransactionType.CANCEL_SPONSORSHIP_TYPE
    }

    fun getScaledPrice(amountAssetDecimals: Int?, priceAssetDecimals: Int?): String {
        return MoneyUtil.getScaledPrice(price,
                amountAssetDecimals ?: 8,
                priceAssetDecimals ?: 8).stripZeros()
    }

    fun getScaledTotal(priceAssetDecimals: Int?): String {
        return MoneyUtil.getTextStripZeros(
                BigInteger.valueOf(amount)
                        .multiply(BigInteger.valueOf(price))
                        .divide(BigInteger.valueOf(100000000)).toLong(),
                priceAssetDecimals ?: 8).stripZeros()
    }

    fun getScaledAmount(amountAssetDecimals: Int?): String {
        return MoneyUtil.getScaledText(amount, amountAssetDecimals ?: 8).stripZeros()
    }


    companion object {

        const val GENESIS = 1
        const val PAYMENT = 2
        const val ISSUE = 3
        const val TRANSFER = 4
        const val REISSUE = 5
        const val BURN = 6
        const val EXCHANGE = 7
        const val LEASE = 8
        const val LEASE_CANCEL = 9
        const val CREATE_ALIAS = 10
        const val MASS_TRANSFER = 11
        const val DATA = 12
        const val ADDRESS_SCRIPT = 13
        const val SPONSORSHIP = 14
        const val ASSET_SCRIPT = 15
        const val SCRIPT_INVOCATION = 16

        private fun getNameBy(type: Int): String {
            return when (type) {
                GENESIS -> "Genesis"
                PAYMENT -> "PaymentResponse"
                ISSUE -> "Issue"
                TRANSFER -> "TransferResponse"
                REISSUE -> "Reissue"
                BURN -> "Burn"
                EXCHANGE -> "Exchange"
                LEASE -> "LeaseResponse"
                LEASE_CANCEL -> "LeaseResponse Cancel"
                CREATE_ALIAS -> "Create AliasResponse"
                MASS_TRANSFER -> "Mass TransferResponse"
                DATA -> "DataResponse"
                ADDRESS_SCRIPT -> "Address Script"
                SPONSORSHIP -> "SponsorShip"
                ASSET_SCRIPT -> "Asset Script"
                SCRIPT_INVOCATION -> "Script Invocation"
                else -> ""
            }
        }

        fun getInfo(transaction: TransactionResponse, address: String): String {
            val feeAssetId = if (transaction.feeAssetId == null) {
                ""
            } else {
                " (${transaction.feeAssetId})"
            }
            return "Transaction ID: ${transaction.id}\n" +
                    type(transaction, address) +
                    "Date: ${transaction.timestamp.date("MM/dd/yyyy HH:mm")}\n" +
                    "Sender: ${transaction.sender}\n" +
                    recipient(transaction) +
                    amount(transaction) +
                    exchangePrice(transaction, address) +
                    fee(transaction, feeAssetId) +
                    attachment(transaction)
        }

        private fun type(transaction: TransactionResponse, address: String) =
                "Type: ${transaction.type} (${getNameBy(transaction.type).toLowerCase()}" +
                        if (transaction.type == EXCHANGE) {
                            if (findMyOrder(transaction.order1!!,
                                            transaction.order2!!,
                                            address)
                                            .orderType == WavesConstants.SELL_ORDER_TYPE) {
                                "-${WavesConstants.SELL_ORDER_TYPE})\n"
                            } else {
                                "-${WavesConstants.BUY_ORDER_TYPE})\n"
                            }
                        } else {
                            ")\n"
                        }

        private fun recipient(transaction: TransactionResponse): String {
            return if (transaction.recipient.isNullOrEmpty()) {
                ""
            } else {
                "Recipient: ${transaction.recipient.clearAlias()}\n"
            }
        }

        private fun fee(transaction: TransactionResponse, feeAssetId: String): String {
            return "Fee: ${MoneyUtil.getScaledText(transaction.fee, transaction.feeAssetObject)
                    .stripZeros()} ${transaction.feeAssetObject?.name}" + feeAssetId
        }

        private fun attachment(transaction: TransactionResponse): String {
            return if (transaction.attachment.isNullOrEmpty()) {
                ""
            } else {
                "\nAttachment: ${String(Base58.decode(transaction.attachment ?: ""))}"
            }
        }

        private fun amount(transaction: TransactionResponse): String {
            val amountAsset = if (transaction.type == EXCHANGE) {
                transaction.order1?.assetPair?.amountAssetObject
            } else {
                transaction.asset
            }
            return "Amount: ${MoneyUtil.getScaledText(transaction.amount, transaction.asset)
                    .stripZeros()} ${amountAsset?.name}" +
                    if (amountAsset?.id.isNullOrEmpty()) {
                        "\n"
                    } else {
                        " (${amountAsset?.id})\n"
                    }
        }

        private fun exchangePrice(transaction: TransactionResponse, address: String): String {
            return if (transaction.type == EXCHANGE) {
                val myOrder = findMyOrder(transaction.order1!!, transaction.order2!!, address)
                val priceAsset = myOrder.assetPair?.priceAssetObject
                val priceValue = MoneyUtil.getScaledText(
                        transaction.amount.times(transaction.price).div(100000000),
                        priceAsset).stripZeros()

                "Price: ${MoneyUtil.getScaledText(transaction.price,
                        myOrder.assetPair?.priceAssetObject)
                        .stripZeros()} " +
                        "${priceAsset?.name} " +
                        if (priceAsset?.id.isNullOrEmpty()) {
                            "\n"
                        } else {
                            " (${priceAsset?.id})\n"
                        } +
                        "Total price: $priceValue ${priceAsset?.name}\n"
            } else {
                ""
            }
        }
    }
}

open class DataResponse(
        @SerializedName("key") var key: String = "",
        @SerializedName("type") var type: String = "",
        @SerializedName("value") var value: String = ""
)


open class TransferResponse(
        @SerializedName("recipient")
        var recipient: String = "",
        @SerializedName("recipientAddress")
        var recipientAddress: String? = "",
        @SerializedName("amount")
        var amount: Long = 0
)