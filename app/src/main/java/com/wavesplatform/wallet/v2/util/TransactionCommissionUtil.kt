package com.wavesplatform.wallet.v2.util

import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse

class TransactionCommissionUtil {

    companion object {

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