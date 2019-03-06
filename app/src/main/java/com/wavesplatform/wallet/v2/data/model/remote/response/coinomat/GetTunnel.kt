package com.wavesplatform.wallet.v2.data.model.remote.response.coinomat

import com.google.gson.annotations.SerializedName

data class GetTunnel(

        // @SerializedName("history") var history: String? = null,
    @SerializedName("tunnel") var tunnel: Tunnel? = null
) {

    data class Tunnel(
        @SerializedName("xt_id") var xtId: String? = null,
        @SerializedName("xr") var xr: String? = null,
        @SerializedName("xrate_fixed") var xrateFixed: String? = null,
        @SerializedName("xr_fix_tm") var xrFixTm: String? = null,
        @SerializedName("wallet_from") var walletFrom: String? = null,
        @SerializedName("wallet_to") var walletTo: String? = null,
        @SerializedName("currency_from") var currencyFrom: String? = null,
        @SerializedName("currency_to") var currencyTo: String? = null,
        @SerializedName("added") var added: String? = null,
        @SerializedName("fee_in") var feeIn: String? = null,
        @SerializedName("fee_out") var feeOut: String? = null,
        @SerializedName("further_info") var furtherInfo: String? = null,
        @SerializedName("monero_payment_id") var moneroPaymentId: String? = null,
        @SerializedName("xcase") var xcase: String? = null,
        @SerializedName("xrate") var xrate: String? = null,
        @SerializedName("in_min") var inMin: String? = null,
        @SerializedName("in_def") var inDef: String? = null,
        @SerializedName("in_max") var inMax: String? = null,
        @SerializedName("fixtm_till") var fixtmTill: String? = null,
        @SerializedName("uptodate") var uptodate: String? = null,
        @SerializedName("currency_from_txt") var currencyFromTxt: String? = null,
        @SerializedName("currency_to_txt") var currencyToTxt: String? = null,
        @SerializedName("in_prec") var inPrec: Prec? = null,
        @SerializedName("out_prec") var outPrec: Prec? = null,
        @SerializedName("attachment") var attachment: String? = null
    ) {

        data class Prec(
            @SerializedName("dec") var dec: String? = null,
            @SerializedName("correction") var correction: String? = null,
            @SerializedName("fee") var fee: String? = null
        )
    }
}