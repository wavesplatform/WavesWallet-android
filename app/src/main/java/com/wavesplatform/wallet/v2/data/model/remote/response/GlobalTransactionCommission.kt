package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

/**
 * https://wavesplatform.atlassian.net/wiki/spaces/WAVES/pages/1305280576
 */
data class GlobalTransactionCommission(

    @SerializedName("smart_asset_extra_fee") var smartAssetExtraFee: Long = 400000,
    @SerializedName("smart_account_extra_fee") var smartAccountExtraFee: Long = 400000,
    @SerializedName("calculate_fee_rules") var calculateFeeRules: CalculateFeeRules = CalculateFeeRules()) {

    data class CalculateFeeRules(
        @SerializedName("default") var default: FeeRules = FeeRules(),
        @SerializedName("3") var issue: FeeRules = FeeRules(fee = 100000000),
        @SerializedName("5") var reissue: FeeRules = FeeRules(fee = 100000000),
        @SerializedName("7") var exchange: FeeRules = FeeRules(addSmartAccountFee = false, fee = 300000),
        @SerializedName("11") var massTransfer: FeeRules = FeeRules(pricePerTransfer = 50000),
        @SerializedName("12") var data: FeeRules = FeeRules(pricePerKb = 100000),
        @SerializedName("13") var script: FeeRules = FeeRules(fee = 1000000),
        @SerializedName("14") var sponsor: FeeRules = FeeRules(fee = 100000000),
        @SerializedName("15") var assetScript: FeeRules = FeeRules(fee = 100000000)
    )

    data class FeeRules(
        @SerializedName("smart_asset_extra_fee") var smartAssetExtraFee: Long? = null,
        @SerializedName("smart_account_extra_fee") var smartAccountExtraFee: Long? = null,
        @SerializedName("add_smart_asset_fee") var addSmartAssetFee: Boolean = true,
        @SerializedName("add_smart_account_fee") var addSmartAccountFee: Boolean = true,
        @SerializedName("min_price_step") var minPriceStep: Long = 100000,
        @SerializedName("fee") var fee: Long = 100000,
        @SerializedName("price_per_transfer") var pricePerTransfer: Long? = null,
        @SerializedName("price_per_kb") var pricePerKb: Long? = null
    )

    class Params(
        var transactionType: Int? = null,
        var smartAccount: Boolean? = null,
        var smartAsset: Boolean? = null,
        var transfersCount: Int? = null,
        var bytesCount: Int? = null,
        var smartPriceAsset: Boolean? = null,
        var smartAmountAsset: Boolean? = null
    )
}