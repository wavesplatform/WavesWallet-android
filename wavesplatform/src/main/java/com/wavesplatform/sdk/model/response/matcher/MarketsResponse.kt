/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.matcher

import com.google.gson.annotations.SerializedName

data class MarketsResponse(
    @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
    @SerializedName("markets") var markets: List<MarketResponse> = listOf()
)