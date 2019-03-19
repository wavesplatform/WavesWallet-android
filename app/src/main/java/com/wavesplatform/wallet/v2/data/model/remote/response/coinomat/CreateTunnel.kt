package com.wavesplatform.wallet.v2.data.model.remote.response.coinomat

import com.google.gson.annotations.SerializedName

data class CreateTunnel(
    @SerializedName("ok") var ok: String? = null,
    @SerializedName("tunnel_id") var tunnelId: String? = null,
    @SerializedName("k1") var k1: String? = null,
    @SerializedName("k2") var k2: String? = null
)