package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class CoinomatLimit(
        @SerializedName("min") var min: String? = null,
        @SerializedName("max") var max: String? = null)