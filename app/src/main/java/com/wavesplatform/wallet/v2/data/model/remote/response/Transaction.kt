package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.util.clearAlias
import com.wavesplatform.wallet.v2.util.findMyOrder
import com.wavesplatform.wallet.v2.util.stripZeros
import com.wavesplatform.wallet.v2.util.transactionType
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import pers.victor.ext.date
import java.math.BigInteger

@RealmClass
open class Lease(
    @SerializedName("type") var type: Int = 0,
    @PrimaryKey
    @SerializedName("id") var id: String = "",
    @SerializedName("sender") var sender: String = "",
    @SerializedName("senderPublicKey") var senderPublicKey: String = "",
    @SerializedName("fee") var fee: Int = 0,
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("signature") var signature: String = "",
    @SerializedName("version") var version: Int = 0,
    @SerializedName("amount") var amount: Long = 0,
    @SerializedName("recipient") var recipient: String = ""
) : RealmModel

@RealmClass
open class Order(
    @PrimaryKey
    @SerializedName("id") var id: String = "",
    @SerializedName("sender") var sender: String = "",
    @SerializedName("senderPublicKey") var senderPublicKey: String = "",
    @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
    @SerializedName("assetPair") var assetPair: AssetPair? = AssetPair(),
    @SerializedName("orderType") var orderType: String = "",
    @SerializedName("price") var price: Long = 0,
    @SerializedName("amount") var amount: Long = 0,
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("expiration") var expiration: Long = 0,
    @SerializedName("matcherFee") var matcherFee: Long = 0,
    @SerializedName("signature") var signature: String = ""
) : RealmModel {

    fun getType(): OrderType {
        return when (orderType) {
            Constants.BUY_ORDER_TYPE -> OrderType.BUY
            Constants.SELL_ORDER_TYPE -> OrderType.SELL
            else -> OrderType.BUY
        }
    }
}

@RealmClass
open class AssetPair(
    @SerializedName("amountAsset") var amountAsset: String? = "",
    @SerializedName("amountAssetObject") var amountAssetObject: AssetInfo? = AssetInfo(),
    @SerializedName("priceAsset") var priceAsset: String? = "",
    @SerializedName("priceAssetObject") var priceAssetObject: AssetInfo? = AssetInfo()
) : RealmModel

@RealmClass
open class Transaction(
    @SerializedName("type")
    var type: Int = 0,
    @PrimaryKey
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
    var lease: Lease? = Lease(),
    @SerializedName("fee")
    var fee: Long = 0,
    @SerializedName("feeAssetId")
    var feeAssetId: String? = null,
    @SerializedName("feeAssetObject")
    var feeAssetObject: AssetInfo? = AssetInfo(),
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
    var order1: Order? = Order(),
    @SerializedName("order2")
    var order2: Order? = Order(),
    @SerializedName("totalAmount")
    var totalAmount: Long = 0,
    @SerializedName("transfers")
    var transfers: RealmList<Transfer> = RealmList(),
    @SerializedName("data")
    var data: RealmList<Data> = RealmList(),
    @SerializedName("isPending")
    var isPending: Boolean = false,
    @SerializedName("script")
    var script: String? = "",
    @SerializedName("minSponsoredAssetFee")
    var minSponsoredAssetFee: String? = "",
    var transactionTypeId: Int = 0,
    var asset: AssetInfo? = AssetInfo()
) : RealmModel {

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

        private fun getNameBy(type: Int): String {
            return when (type) {
                GENESIS -> "Genesis"
                PAYMENT -> "Payment"
                ISSUE -> "Issue"
                TRANSFER -> "Transfer"
                REISSUE -> "Reissue"
                BURN -> "Burn"
                EXCHANGE -> "Exchange"
                LEASE -> "Lease"
                LEASE_CANCEL -> "Lease Cancel"
                CREATE_ALIAS -> "Create Alias"
                MASS_TRANSFER -> "Mass Transfer"
                DATA -> "Data"
                ADDRESS_SCRIPT -> "Script"
                SPONSORSHIP -> "SponsorShip"
                ASSET_SCRIPT -> "Asset Script"
                else -> ""
            }
        }

        fun getInfo(transaction: Transaction): String {
            val feeAssetId = if (transaction.feeAssetId == null) {
                ""
            } else {
                " (${transaction.feeAssetId})"
            }
            return "Transaction ID: ${transaction.id}\n" +
                    type(transaction) +
                    "Date: ${transaction.timestamp.date("MM/dd/yyyy HH:mm")}\n" +
                    "Sender: ${transaction.sender}\n" +
                    recipient(transaction) +
                    amount(transaction) +
                    exchangePrice(transaction) +
                    fee(transaction, feeAssetId) +
                    attachment(transaction)
        }

        private fun type(transaction: Transaction) =
                "Type: ${transaction.type} (${getNameBy(transaction.type).toLowerCase()}" +
                        if (transaction.type == EXCHANGE) {
                            if (findMyOrder(transaction.order1!!,
                                            transaction.order2!!,
                                            App.getAccessManager().getWallet()?.address!!)
                                            .orderType == Constants.SELL_ORDER_TYPE) {
                                "-${Constants.SELL_ORDER_TYPE})\n"
                            } else {
                                "-${Constants.BUY_ORDER_TYPE})\n"
                            }
                        } else {
                            ")\n"
                        }

        private fun recipient(transaction: Transaction): String {
            return if (transaction.recipient.isNullOrEmpty()) {
                ""
            } else {
                "Recipient: ${transaction.recipient.clearAlias()}\n"
            }
        }

        private fun fee(transaction: Transaction, feeAssetId: String): String {
            return "Fee: ${MoneyUtil.getScaledText(transaction.fee, transaction.feeAssetObject)
                    .stripZeros()} ${transaction.feeAssetObject?.name}" + feeAssetId
        }

        private fun attachment(transaction: Transaction): String {
            return if (transaction.attachment.isNullOrEmpty()) {
                ""
            } else {
                "\nAttachment: ${String(Base58.decode(transaction.attachment))}"
            }
        }

        private fun amount(transaction: Transaction): String {
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

        private fun exchangePrice(transaction: Transaction): String {
            return if (transaction.type == EXCHANGE) {
                val myOrder = findMyOrder(transaction.order1!!, transaction.order2!!,
                        App.getAccessManager().getWallet()?.address!!)
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

@RealmClass
open class Data(
    @SerializedName("key") var key: String = "",
    @SerializedName("type") var type: String = "",
    @SerializedName("value") var value: String = ""
) : RealmModel

@RealmClass
open class Transfer(
    @SerializedName("recipient")
    var recipient: String = "",
    @SerializedName("recipientAddress")
    var recipientAddress: String? = "",
    @SerializedName("amount")
    var amount: Long = 0
) : RealmModel