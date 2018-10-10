package com.wavesplatform.wallet.v2.data.model.remote.response.coinomat

import com.google.gson.annotations.SerializedName

data class Xrate(
        @SerializedName("xrate") var xrate: Double? = null,
        @SerializedName("in_min") var inMin: Double? = null,
        @SerializedName("in_def") var inDef: Double? = null,
        @SerializedName("in_max") var inMax: Double? = null,
        @SerializedName("fee_in") var feeIn: Double? = null,
        @SerializedName("fee_out") var feeOut: Double? = null,
        @SerializedName("from_txt") var fromTxt: String? = null,
        @SerializedName("to_txt") var toTxt: String? = null,
        @SerializedName("in_prec") var inPrec: Prec? = null,
        @SerializedName("out_prec") var outPrec: Prec? = null,
        @SerializedName("extra_note") var extraNote: String? = null,
        @SerializedName("fee") var fee: String? = null) {

    data class Prec(
            @SerializedName("dec") var dec: Double? = null,
            @SerializedName("correction") var correction: String? = null,
            @SerializedName("fee") var fee: Double? = null
    )
}

        
        