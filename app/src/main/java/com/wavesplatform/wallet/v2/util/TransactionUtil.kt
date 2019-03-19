package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalTransactionCommission
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import javax.inject.Inject

/**
 * Created by anonymous on 07.03.18.
 */

class TransactionUtil @Inject constructor() {

    fun getTransactionType(transaction: Transaction): Int =
            if (transaction.type == Transaction.TRANSFER &&
                    transaction.sender != App.getAccessManager().getWallet()?.address &&
                    transaction.asset?.isSpam == true) {
                Constants.ID_SPAM_RECEIVE_TYPE
            } else if (transaction.type == Transaction.TRANSFER &&
                    transaction.sender != App.getAccessManager().getWallet()?.address &&
                    transaction.recipientAddress != App.getAccessManager().getWallet()?.address) {
                Constants.ID_RECEIVE_SPONSORSHIP_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER &&
                    transaction.sender != App.getAccessManager().getWallet()?.address &&
                    transaction.asset?.isSpam == true) {
                Constants.ID_MASS_SPAM_RECEIVE_TYPE
            } else if (transaction.type == Transaction.LEASE_CANCEL &&
                    !transaction.leaseId.isNullOrEmpty()) {
                Constants.ID_CANCELED_LEASING_TYPE
            } else if ((transaction.type == Transaction.TRANSFER || transaction.type == 9) &&
                    transaction.sender != App.getAccessManager().getWallet()?.address) {
                Constants.ID_RECEIVED_TYPE
            } else if (transaction.type == Transaction.TRANSFER &&
                    transaction.sender == transaction.recipientAddress) {
                Constants.ID_SELF_TRANSFER_TYPE
            } else if (transaction.type == Transaction.TRANSFER &&
                    transaction.sender == App.getAccessManager().getWallet()?.address) {
                Constants.ID_SENT_TYPE
            } else if (transaction.type == Transaction.LEASE &&
                    transaction.recipientAddress != App.getAccessManager().getWallet()?.address) {
                Constants.ID_STARTED_LEASING_TYPE
            } else if (transaction.type == Transaction.EXCHANGE) {
                Constants.ID_EXCHANGE_TYPE
            } else if (transaction.type == Transaction.ISSUE) {
                Constants.ID_TOKEN_GENERATION_TYPE
            } else if (transaction.type == Transaction.BURN) {
                Constants.ID_TOKEN_BURN_TYPE
            } else if (transaction.type == Transaction.REISSUE) {
                Constants.ID_TOKEN_REISSUE_TYPE
            } else if (transaction.type == Transaction.CREATE_ALIAS) {
                Constants.ID_CREATE_ALIAS_TYPE
            } else if (transaction.type == Transaction.LEASE &&
                    transaction.recipientAddress == App.getAccessManager().getWallet()?.address) {
                Constants.ID_INCOMING_LEASING_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER &&
                    transaction.sender == App.getAccessManager().getWallet()?.address) {
                Constants.ID_MASS_SEND_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER &&
                    transaction.sender != App.getAccessManager().getWallet()?.address) {
                Constants.ID_MASS_RECEIVE_TYPE
            } else if (transaction.type == Transaction.DATA) {
                Constants.ID_DATA_TYPE
            } else if (transaction.type == Transaction.ADDRESS_SCRIPT) {
                if (transaction.script == null) {
                    Constants.ID_CANCEL_ADDRESS_SCRIPT_TYPE
                } else {
                    Constants.ID_SET_ADDRESS_SCRIPT_TYPE
                }
            } else if (transaction.type == Transaction.SPONSORSHIP) {
                if (transaction.minSponsoredAssetFee == null) {
                    Constants.ID_CANCEL_SPONSORSHIP_TYPE
                } else {
                    Constants.ID_SET_SPONSORSHIP_TYPE
                }
            } else if (transaction.type == Transaction.ASSET_SCRIPT) {
                Constants.ID_UPDATE_ASSET_SCRIPT_TYPE
            } else {
                Constants.ID_UNRECOGNISED_TYPE
            }

    companion object {

        fun getTransactionAmount(transaction: Transaction, decimals: Int = 8, round: Boolean = true): String {

            var sign = "-"
            if (transaction.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE ||
                    transaction.transactionType() == TransactionType.MASS_RECEIVE_TYPE) {
                sign = "+"
            }

            return sign + if (transaction.transfers.isNotEmpty()) {
                val sumString = if (round) {
                    getScaledAmount(transaction.transfers.sumByLong { it.amount }, decimals)
                } else {
                    MoneyUtil.getScaledText(
                            transaction.transfers.sumByLong { it.amount }, transaction.asset)
                            .stripZeros()
                }
                if (sumString.isEmpty()) {
                    ""
                } else {
                    sumString
                }
            } else {
                if (round) {
                    getScaledAmount(transaction.amount, decimals)
                } else {
                    MoneyUtil.getScaledText(transaction.amount, transaction.asset).stripZeros()
                }
            }
        }

        fun getScaledText(amount: Long, assetInfo: AssetInfo?): String {
            val afterDot = MoneyUtil.getScaledText(amount, assetInfo)
                    .substringAfter(".").clearBalance().toLong()
            return if (afterDot == 0L) {
                MoneyUtil.getScaledText(amount, assetInfo).substringBefore(".")
            } else {
                MoneyUtil.getScaledText(amount, assetInfo)
            }
        }

        fun countCommission(
            commission: GlobalTransactionCommission,
            params: GlobalTransactionCommission.Params
        ): Long {

            val type = params.transactionType!!

            val feeRules = when (type) {
                Transaction.ISSUE -> commission.calculateFeeRules.issue
                Transaction.REISSUE -> commission.calculateFeeRules.reissue
                Transaction.EXCHANGE -> commission.calculateFeeRules.exchange
                Transaction.MASS_TRANSFER -> commission.calculateFeeRules.massTransfer
                Transaction.DATA -> commission.calculateFeeRules.data
                Transaction.ADDRESS_SCRIPT -> commission.calculateFeeRules.script
                Transaction.SPONSORSHIP -> commission.calculateFeeRules.sponsor
                Transaction.ASSET_SCRIPT -> commission.calculateFeeRules.assetScript
                else -> commission.calculateFeeRules.default
            }

            return when (type) {
                Transaction.ISSUE,
                Transaction.REISSUE,
                Transaction.LEASE,
                Transaction.LEASE_CANCEL,
                Transaction.CREATE_ALIAS,
                Transaction.ADDRESS_SCRIPT,
                Transaction.SPONSORSHIP,
                Transaction.ASSET_SCRIPT ->
                    getAccountCommission(feeRules, params, commission)
                Transaction.TRANSFER,
                Transaction.BURN ->
                    getAssetAccountCommission(feeRules, params, commission)
                Transaction.EXCHANGE ->
                    getExchangeCommission(feeRules, params, commission)
                Transaction.MASS_TRANSFER ->
                    getMassTransferCommission(feeRules, params, commission)
                Transaction.DATA -> {
                    getDataCommission(params, feeRules, commission)
                }
                else -> commission.calculateFeeRules.default.fee
            }
        }

        private fun getDataCommission(
            params: GlobalTransactionCommission.Params,
            feeRules: GlobalTransactionCommission.FeeRules,
            commission: GlobalTransactionCommission
        ): Long {
            var total = 0.0
            total += Math.floor(1 + (params.bytesCount!!.toDouble() - 1) / 1024) * feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total.toLong()
        }

        private fun getMassTransferCommission(
            feeRules: GlobalTransactionCommission.FeeRules,
            params: GlobalTransactionCommission.Params,
            commission: GlobalTransactionCommission
        ): Long {
            val total = getAssetAccountCommission(feeRules, params, commission)
            var transfersPrice = (params.transfersCount!! * feeRules.pricePerTransfer!!).toDouble()
            val minPriceStep = feeRules.minPriceStep.toDouble()
            if (transfersPrice.rem(minPriceStep) != 0.0) {
                transfersPrice = Math.ceil((transfersPrice / minPriceStep)) * minPriceStep
            }
            return (transfersPrice + total).toLong()
        }

        private fun getExchangeCommission(
            feeRules: GlobalTransactionCommission.FeeRules,
            params: GlobalTransactionCommission.Params,
            commission: GlobalTransactionCommission
        ): Long {
            var total = feeRules.fee
            if (params.smartPriceAsset!!) {
                total += commission.smartAssetExtraFee
            }
            if (params.smartAmountAsset!!) {
                total += commission.smartAssetExtraFee
            }
            return total
        }

        private fun getAccountCommission(
            feeRules: GlobalTransactionCommission.FeeRules,
            params: GlobalTransactionCommission.Params,
            commission: GlobalTransactionCommission
        ): Long {
            var total = feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total
        }

        private fun getAssetAccountCommission(
            feeRules: GlobalTransactionCommission.FeeRules,
            params: GlobalTransactionCommission.Params,
            commission: GlobalTransactionCommission
        ): Long {
            var total = feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            if (params.smartAsset!!) {
                total += commission.smartAssetExtraFee
            }
            return total
        }
    }
}