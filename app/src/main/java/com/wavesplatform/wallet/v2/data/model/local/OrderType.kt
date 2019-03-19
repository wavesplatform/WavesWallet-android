package com.wavesplatform.wallet.v2.data.model.local

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.R
import pers.victor.ext.findColor

enum class OrderType(private val typeServerByte: Int, val typeUI: Int, val color: Int) {
    @SerializedName("buy")
    BUY(0, R.string.my_orders_type_buy, findColor(R.color.submit400)),
    @SerializedName("sell")
    SELL(1, R.string.my_orders_type_sell, findColor(R.color.error400));

    fun toBytes(): ByteArray {
        return byteArrayOf(typeServerByte.toByte())
    }
}
