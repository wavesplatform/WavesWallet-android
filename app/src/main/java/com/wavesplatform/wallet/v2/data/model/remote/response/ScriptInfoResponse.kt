package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class ScriptInfoResponse(
        @SerializedName("address")
        var address: String = "",
        @SerializedName("complexity")
        var complexity: Double = 0.0,
        @SerializedName("extraFee")
        var extraFee: Double = 0.0
)