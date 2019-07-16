/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.node

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.utils.*
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*


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
        @SerializedName("matcherFeeAssetId") var matcherFeeAssetId: String? = null,
        @SerializedName("matcherFee") var matcherFee: Long = 0,
        @SerializedName("signature") var signature: String = ""
)


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

open class HistoryTransactionResponse(
        @SerializedName("type")
        var type: Byte = 0,
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
        @SerializedName("matcherFeeAssetId")
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

        fun getInfo(transaction: HistoryTransactionResponse, address: String): String {
            val feeAssetId = if (transaction.feeAssetId == null) {
                ""
            } else {
                " (${transaction.feeAssetId})"
            }

            val time = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
                    .format(Date(transaction.timestamp))

            return "Transaction ID: ${transaction.id}\n" +
                    type(transaction, address) +
                    "Date: $time\n" +
                    "Sender: ${transaction.sender}\n" +
                    recipient(transaction) +
                    amount(transaction) +
                    exchangePrice(transaction, address) +
                    fee(transaction, feeAssetId) +
                    attachment(transaction)
        }

        private fun type(transaction: HistoryTransactionResponse, address: String) =
                "Type: ${transaction.type} (${BaseTransaction.getNameBy(transaction.type).toLowerCase()}" +
                        if (transaction.type == BaseTransaction.EXCHANGE) {
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

        private fun recipient(transaction: HistoryTransactionResponse): String {
            return if (transaction.recipient.isNullOrEmpty()) {
                ""
            } else {
                "Recipient: ${transaction.recipient.parseAlias()}\n"
            }
        }

        private fun fee(transaction: HistoryTransactionResponse, feeAssetId: String): String {
            return "Fee: ${MoneyUtil.getScaledText(transaction.fee, transaction.feeAssetObject)
                    .stripZeros()} ${transaction.feeAssetObject?.name}" + feeAssetId
        }

        private fun attachment(transaction: HistoryTransactionResponse): String {
            return if (transaction.attachment.isNullOrEmpty()) {
                ""
            } else {
                "\nAttachment: ${String(Base58.decode(transaction.attachment ?: ""))}"
            }
        }

        private fun amount(transaction: HistoryTransactionResponse): String {
            val amountAsset = if (transaction.type == BaseTransaction.EXCHANGE) {
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

        private fun exchangePrice(transaction: HistoryTransactionResponse, address: String): String {
            return if (transaction.type == BaseTransaction.EXCHANGE) {
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