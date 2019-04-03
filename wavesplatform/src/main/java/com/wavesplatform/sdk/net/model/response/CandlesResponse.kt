/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class CandlesResponse(
    @SerializedName("candles")
    var candles: List<Candle> = listOf(),
    @SerializedName("timeEnd")
    var timeEnd: Long = 0,
    @SerializedName("timeStart")
    var timeStart: Long = 0
) {
    data class Candle(
        @SerializedName("close")
        var close: Double? = 0.0,
        @SerializedName("high")
        var high: Double? = 0.0,
        @SerializedName("low")
        var low: Double? = 0.0,
        @SerializedName("open")
        var openValue: Double? = 0.0,
        @SerializedName("time")
        var time: Long? = 0,
        @SerializedName("txsCount")
        var txsCount: Int? = 0,
        @SerializedName("volume")
        var volume: Double? = 0.0
    )
}