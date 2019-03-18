package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class AddressAssetBalance(
    @SerializedName("address") var address: String = "",
    @SerializedName("assetId") var assetId: String = "",
    @SerializedName("balance") var balance: Long = 0L
)