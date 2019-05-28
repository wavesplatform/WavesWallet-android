/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.utils


import com.wavesplatform.sdk.net.model.response.AssetInfoResponse
import com.wavesplatform.sdk.net.model.response.GlobalTransactionCommissionResponse
import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.sdk.net.model.TransactionType
import javax.inject.Inject

class TransactionUtil @Inject constructor() {

    companion object {

        fun getTransactionType(transaction: TransactionResponse, address: String): Int =
                if (transaction.type == TransactionResponse.TRANSFER &&
                        transaction.sender != address &&
                        transaction.asset?.isSpam == true) {
                    WavesConstants.ID_SPAM_RECEIVE_TYPE
                } else if (transaction.type == TransactionResponse.TRANSFER &&
                        transaction.sender != address &&
                        transaction.recipientAddress != address) {
                    WavesConstants.ID_RECEIVE_SPONSORSHIP_TYPE
                } else if (transaction.type == TransactionResponse.MASS_TRANSFER &&
                        transaction.sender != address &&
                        transaction.asset?.isSpam == true) {
                    WavesConstants.ID_MASS_SPAM_RECEIVE_TYPE
                } else if (transaction.type == TransactionResponse.LEASE_CANCEL &&
                        !transaction.leaseId.isNullOrEmpty()) {
                    WavesConstants.ID_CANCELED_LEASING_TYPE
                } else if ((transaction.type == TransactionResponse.TRANSFER || transaction.type == 9) &&
                        transaction.sender != address) {
                    WavesConstants.ID_RECEIVED_TYPE
                } else if (transaction.type == TransactionResponse.TRANSFER &&
                        transaction.sender == transaction.recipientAddress) {
                    WavesConstants.ID_SELF_TRANSFER_TYPE
                } else if (transaction.type == TransactionResponse.TRANSFER &&
                        transaction.sender == address) {
                    WavesConstants.ID_SENT_TYPE
                } else if (transaction.type == TransactionResponse.LEASE &&
                        transaction.recipientAddress != address) {
                    WavesConstants.ID_STARTED_LEASING_TYPE
                } else if (transaction.type == TransactionResponse.EXCHANGE) {
                    WavesConstants.ID_EXCHANGE_TYPE
                } else if (transaction.type == TransactionResponse.ISSUE) {
                    WavesConstants.ID_TOKEN_GENERATION_TYPE
                } else if (transaction.type == TransactionResponse.BURN) {
                    WavesConstants.ID_TOKEN_BURN_TYPE
                } else if (transaction.type == TransactionResponse.REISSUE) {
                    WavesConstants.ID_TOKEN_REISSUE_TYPE
                } else if (transaction.type == TransactionResponse.CREATE_ALIAS) {
                    WavesConstants.ID_CREATE_ALIAS_TYPE
                } else if (transaction.type == TransactionResponse.LEASE &&
                        transaction.recipientAddress == address) {
                    WavesConstants.ID_INCOMING_LEASING_TYPE
                } else if (transaction.type == TransactionResponse.MASS_TRANSFER &&
                        transaction.sender == address) {
                    WavesConstants.ID_MASS_SEND_TYPE
                } else if (transaction.type == TransactionResponse.MASS_TRANSFER &&
                        transaction.sender != address) {
                    WavesConstants.ID_MASS_RECEIVE_TYPE
                } else if (transaction.type == TransactionResponse.DATA) {
                    WavesConstants.ID_DATA_TYPE
                } else if (transaction.type == TransactionResponse.ADDRESS_SCRIPT) {
                    if (transaction.script == null) {
                        WavesConstants.ID_CANCEL_ADDRESS_SCRIPT_TYPE
                    } else {
                        WavesConstants.ID_SET_ADDRESS_SCRIPT_TYPE
                    }
                } else if (transaction.type == TransactionResponse.SPONSORSHIP) {
                    if (transaction.minSponsoredAssetFee == null) {
                        WavesConstants.ID_CANCEL_SPONSORSHIP_TYPE
                    } else {
                        WavesConstants.ID_SET_SPONSORSHIP_TYPE
                    }
                } else if (transaction.type == TransactionResponse.ASSET_SCRIPT) {
                    WavesConstants.ID_UPDATE_ASSET_SCRIPT_TYPE
                } else if (transaction.type == TransactionResponse.SCRIPT_INVOCATION) {
                    WavesConstants.ID_SCRIPT_INVOCATION_TYPE
                } else {
                    WavesConstants.ID_UNRECOGNISED_TYPE
                }

        fun getTransactionAmount(transaction: TransactionResponse, decimals: Int = 8, round: Boolean = true): String {

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

        fun getScaledText(amount: Long, assetInfo: AssetInfoResponse?): String {
            val afterDot = MoneyUtil.getScaledText(amount, assetInfo)
                    .substringAfter(".").clearBalance().toLong()
            return if (afterDot == 0L) {
                MoneyUtil.getScaledText(amount, assetInfo).substringBefore(".")
            } else {
                MoneyUtil.getScaledText(amount, assetInfo)
            }
        }

        fun countCommission(
                commission: GlobalTransactionCommissionResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse
        ): Long {

            val type = params.transactionType!!

            val feeRules = when (type) {
                TransactionResponse.ISSUE -> commission.calculateFeeRules.issue
                TransactionResponse.REISSUE -> commission.calculateFeeRules.reissue
                TransactionResponse.EXCHANGE -> commission.calculateFeeRules.exchange
                TransactionResponse.MASS_TRANSFER -> commission.calculateFeeRules.massTransfer
                TransactionResponse.DATA -> commission.calculateFeeRules.data
                TransactionResponse.ADDRESS_SCRIPT -> commission.calculateFeeRules.script
                TransactionResponse.SPONSORSHIP -> commission.calculateFeeRules.sponsor
                TransactionResponse.ASSET_SCRIPT -> commission.calculateFeeRules.assetScript
                else -> commission.calculateFeeRules.default
            }

            return when (type) {
                TransactionResponse.ISSUE,
                TransactionResponse.REISSUE,
                TransactionResponse.LEASE,
                TransactionResponse.LEASE_CANCEL,
                TransactionResponse.CREATE_ALIAS,
                TransactionResponse.ADDRESS_SCRIPT,
                TransactionResponse.SPONSORSHIP,
                TransactionResponse.ASSET_SCRIPT ->
                    getAccountCommission(feeRules, params, commission)
                TransactionResponse.TRANSFER,
                TransactionResponse.BURN ->
                    getAssetAccountCommission(feeRules, params, commission)
                TransactionResponse.EXCHANGE ->
                    getExchangeCommission(feeRules, params, commission)
                TransactionResponse.MASS_TRANSFER ->
                    getMassTransferCommission(feeRules, params, commission)
                TransactionResponse.DATA -> {
                    getDataCommission(params, feeRules, commission)
                }
                else -> commission.calculateFeeRules.default.fee
            }
        }

        private fun getDataCommission(
                params: GlobalTransactionCommissionResponse.ParamsResponse,
                feeRules: GlobalTransactionCommissionResponse.FeeRulesResponse,
                commission: GlobalTransactionCommissionResponse
        ): Long {
            var total = 0.0
            total += Math.floor(1 + (params.bytesCount!!.toDouble() - 1) / 1024) * feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total.toLong()
        }

        private fun getMassTransferCommission(
                feeRules: GlobalTransactionCommissionResponse.FeeRulesResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse,
                commission: GlobalTransactionCommissionResponse
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
                feeRules: GlobalTransactionCommissionResponse.FeeRulesResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse,
                commission: GlobalTransactionCommissionResponse
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
                feeRules: GlobalTransactionCommissionResponse.FeeRulesResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse,
                commission: GlobalTransactionCommissionResponse
        ): Long {
            var total = feeRules.fee
            if (params.smartAccount!!) {
                total += commission.smartAccountExtraFee
            }
            return total
        }

        private fun getAssetAccountCommission(
                feeRules: GlobalTransactionCommissionResponse.FeeRulesResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse,
                commission: GlobalTransactionCommissionResponse
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