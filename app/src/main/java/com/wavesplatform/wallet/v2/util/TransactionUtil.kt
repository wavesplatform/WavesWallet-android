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

        fun countCommission(commission: GlobalTransactionCommission,
                            params: GlobalTransactionCommission.Params): Long {

            val type = params.transactionType!!

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
                Transaction.ISSUE,
                Transaction.REISSUE,
                Transaction.LEASE,
                Transaction.LEASE_CANCEL,
                Transaction.CREATE_ALIAS,
                Transaction.SET_SCRIPT,
                Transaction.SPONSOR_FEE,
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

        private fun getDataCommission(params: GlobalTransactionCommission.Params, feeRules: GlobalTransactionCommission.FeeRules, commission: GlobalTransactionCommission): Long {
            var total = 0.0
            total += Math.floor(1 + (params.bytesCount!!.toDouble() - 1) / 1024) * feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total.toLong()
        }

        private fun getMassTransferCommission(feeRules: GlobalTransactionCommission.FeeRules, params: GlobalTransactionCommission.Params, commission: GlobalTransactionCommission): Long {
            val total = getAssetAccountCommission(feeRules, params, commission)
            var transfersPrice = (params.transfersCount!! * feeRules.pricePerTransfer!!).toDouble()
            val minPriceStep = feeRules.minPriceStep.toDouble()
            if (transfersPrice.rem(minPriceStep) != 0.0) {
                transfersPrice = Math.ceil((transfersPrice / minPriceStep)) * minPriceStep
            }
            return (transfersPrice + total).toLong()
        }

        private fun getExchangeCommission(feeRules: GlobalTransactionCommission.FeeRules, params: GlobalTransactionCommission.Params, commission: GlobalTransactionCommission): Long {
            var total = feeRules.fee
            if (params.smartPriceAsset!!) {
                total += commission.smartAssetExtraFee
            }
            if (params.smartAmountAsset!!) {
                total += commission.smartAssetExtraFee
            }
            return total
        }

        private fun getAccountCommission(feeRules: GlobalTransactionCommission.FeeRules, params: GlobalTransactionCommission.Params, commission: GlobalTransactionCommission): Long {
            var total = feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total
        }

        private fun getAssetAccountCommission(
                feeRules: GlobalTransactionCommission.FeeRules,
                params: GlobalTransactionCommission.Params,
                commission: GlobalTransactionCommission): Long {
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