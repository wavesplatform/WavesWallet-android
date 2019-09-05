package com.wavesplatform.wallet.v2.util

import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse
import kotlin.math.floor

class TransactionCommissionUtil {

    companion object {

        fun countCommission(
                commission: GlobalTransactionCommissionResponse,
                params: GlobalTransactionCommissionResponse.ParamsResponse
        ): Long {

            val type = params.transactionType!!

            val feeRules = when (type) {
                BaseTransaction.ISSUE -> commission.calculateFeeRules.issue
                BaseTransaction.REISSUE -> commission.calculateFeeRules.reissue
                BaseTransaction.EXCHANGE -> commission.calculateFeeRules.exchange
                BaseTransaction.MASS_TRANSFER -> commission.calculateFeeRules.massTransfer
                BaseTransaction.DATA -> commission.calculateFeeRules.data
                BaseTransaction.ADDRESS_SCRIPT -> commission.calculateFeeRules.script
                BaseTransaction.SPONSORSHIP -> commission.calculateFeeRules.sponsor
                BaseTransaction.ASSET_SCRIPT -> commission.calculateFeeRules.assetScript
                else -> commission.calculateFeeRules.default
            }

            return when (type) {
                BaseTransaction.ISSUE,
                BaseTransaction.REISSUE,
                BaseTransaction.CREATE_LEASING,
                BaseTransaction.CANCEL_LEASING,
                BaseTransaction.CREATE_ALIAS,
                BaseTransaction.ADDRESS_SCRIPT,
                BaseTransaction.SPONSORSHIP,
                BaseTransaction.ASSET_SCRIPT ->
                    getAccountCommission(feeRules, params, commission)
                BaseTransaction.TRANSFER,
                BaseTransaction.BURN ->
                    getAssetAccountCommission(feeRules, params, commission)
                BaseTransaction.EXCHANGE ->
                    getExchangeCommission(feeRules, params, commission)
                BaseTransaction.MASS_TRANSFER ->
                    getMassTransferCommission(feeRules, params, commission)
                BaseTransaction.DATA -> {
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
            total += floor(1 + (params.bytesCount!!.toDouble() - 1) / 1024) * feeRules.fee
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