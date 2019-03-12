package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class Height(
    @SerializedName("height") var height: Int = 0
)