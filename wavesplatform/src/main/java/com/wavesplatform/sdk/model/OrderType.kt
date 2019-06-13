/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.R

enum class OrderType(private val typeServerByte: Int, val typeUI: Int, val color: Int, var directionSign: String) {
    @SerializedName("buy")
    BUY(0, R.string.my_orders_type_buy, Color.parseColor("#1F5AF6"), "+"),
    @SerializedName("sell")
    SELL(1, R.string.my_orders_type_sell, Color.parseColor("#E5494D"), "-");

    fun toBytes(): ByteArray {
        return byteArrayOf(typeServerByte.toByte())
    }
}
