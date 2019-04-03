/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response.coinomat

import com.google.gson.annotations.SerializedName

data class CreateTunnel(
    @SerializedName("ok") var ok: String? = null,
    @SerializedName("tunnel_id") var tunnelId: String? = null,
    @SerializedName("k1") var k1: String? = null,
    @SerializedName("k2") var k2: String? = null
)