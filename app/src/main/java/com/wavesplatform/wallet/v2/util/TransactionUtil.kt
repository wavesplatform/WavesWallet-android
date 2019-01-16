package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalTransactionCommission
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import javax.inject.Inject

/**
 * Created by anonymous on 07.03.18.
 */

class TransactionUtil @Inject constructor() {

    fun getTransactionType(transaction: Transaction): Int =
            if (transaction.type == 4 && transaction.sender != App.getAccessManager().getWallet()?.address && transaction.asset?.isSpam == true) Constants.ID_SPAM_RECEIVE_TYPE
            else if (transaction.type == 11 && transaction.sender != App.getAccessManager().getWallet()?.address && transaction.asset?.isSpam == true) Constants.ID_MASS_SPAM_RECEIVE_TYPE
            else if (transaction.type == 9 && !transaction.leaseId.isNullOrEmpty()) Constants.ID_CANCELED_LEASING_TYPE
            else if ((transaction.type == 4 || transaction.type == 9) && transaction.sender != App.getAccessManager().getWallet()?.address) Constants.ID_RECEIVED_TYPE
            else if (transaction.type == 4 && transaction.sender == transaction.recipientAddress) Constants.ID_SELF_TRANSFER_TYPE
            else if (transaction.type == 4 && transaction.sender == App.getAccessManager().getWallet()?.address) Constants.ID_SENT_TYPE
            else if (transaction.type == 8 && transaction.recipientAddress != App.getAccessManager().getWallet()?.address) Constants.ID_STARTED_LEASING_TYPE
            else if (transaction.type == 7) Constants.ID_EXCHANGE_TYPE
            else if (transaction.type == 3) Constants.ID_TOKEN_GENERATION_TYPE
            else if (transaction.type == 6) Constants.ID_TOKEN_BURN_TYPE
            else if (transaction.type == 5) Constants.ID_TOKEN_REISSUE_TYPE
            else if (transaction.type == 10) Constants.ID_CREATE_ALIAS_TYPE
            else if (transaction.type == 8 && transaction.recipientAddress == App.getAccessManager().getWallet()?.address) Constants.ID_INCOMING_LEASING_TYPE
            else if (transaction.type == 11 && transaction.sender == App.getAccessManager().getWallet()?.address) Constants.ID_MASS_SEND_TYPE
            else if (transaction.type == 11 && transaction.sender != App.getAccessManager().getWallet()?.address) Constants.ID_MASS_RECEIVE_TYPE
            else if (transaction.type == 12) Constants.ID_DATA_TYPE
            else if (transaction.type == 13) Constants.ID_SET_SCRIPT_TYPE
            else if (transaction.type == 14) Constants.ID_SET_SPONSORSHIP_TYPE
            else Constants.ID_UNRECOGNISED_TYPE

    companion object {

        fun getCommission(type: Int, commission: GlobalTransactionCommission,
                          smartAccount: Boolean, smartAsset: Boolean,
                          transfersCount: Int,
                          bytesCount: Int,
                          smartPriceAsset: Boolean, smartAmountAsset: Boolean): Long {

            val feeRules = when (type) {
                Transaction.ISSUE -> commission.calculateFeeRules.issue
                Transaction.REISSUE -> commission.calculateFeeRules.reissue
                Transaction.EXCHANGE -> commission.calculateFeeRules.exchange
                Transaction.MASS_TRANSFER -> commission.calculateFeeRules.massTransfer
                Transaction.DATA -> commission.calculateFeeRules.data
                Transaction.SET_SCRIPT -> commission.calculateFeeRules.script
                Transaction.SPONSOR_FEE -> commission.calculateFeeRules.sponsor
                Transaction.ASSET_SCRIPT -> commission.calculateFeeRules.assetScript
                else -> commission.calculateFeeRules.default
            }

            return when (type) {
                Transaction.TRANSFER -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    if (smartAsset) {
                        total += commission.smartAssetExtraFee
                    }
                    total
                }
                Transaction.ISSUE -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.REISSUE -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    if (smartAsset) {
                        total += commission.smartAssetExtraFee
                    }
                    total
                }
                Transaction.EXCHANGE -> {
                    var total = feeRules.fee
                    if (smartPriceAsset) {
                        total += commission.smartAssetExtraFee
                    }
                    if (smartAmountAsset) {
                        total += commission.smartAssetExtraFee
                    }
                    total
                }
                Transaction.BURN -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    if (smartAsset) {
                        total += commission.smartAssetExtraFee
                    }
                    total
                }
                Transaction.LEASE -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.LEASE_CANCEL -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.CREATE_ALIAS -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.MASS_TRANSFER -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    if (smartAsset) {
                        total += commission.smartAssetExtraFee
                    }

                    var transfersPrice = (transfersCount * feeRules.pricePerTransfer!!).toDouble()
                    val minPriceStep = feeRules.minPriceStep.toDouble()
                    if (transfersPrice.rem(minPriceStep) != 0.0) {
                        transfersPrice = Math.ceil((transfersPrice / minPriceStep)) * minPriceStep
                    }
                    (transfersPrice + total).toLong()
                }
                Transaction.DATA -> {
                    var total = 0.0
                    total += Math.floor(1 + (bytesCount.toDouble() - 1) / 1024) * feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total.toLong()
                }
                Transaction.SET_SCRIPT -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.SPONSOR_FEE -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                Transaction.ASSET_SCRIPT -> {
                    var total = feeRules.fee
                    if (smartAccount) {
                        total += commission.smartAccountExtraFee
                    }
                    total
                }
                else -> commission.calculateFeeRules.default.fee
            }
        }
    }
}