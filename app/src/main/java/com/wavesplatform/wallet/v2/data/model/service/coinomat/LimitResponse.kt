package com.wavesplatform.wallet.v2.data.model.service.coinomat

import com.google.gson.annotations.SerializedName

data class LimitResponse(
        @SerializedName("min") var min: String? = null,
        @SerializedName("max") var max: String? = null)