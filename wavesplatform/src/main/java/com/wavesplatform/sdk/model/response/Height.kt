package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName

data class Height(
        @SerializedName("height") var height: Int = 0
)