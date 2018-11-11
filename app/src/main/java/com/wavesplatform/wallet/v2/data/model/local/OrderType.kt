package com.wavesplatform.wallet.v2.data.model.local

import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.R
import pers.victor.ext.app
import pers.victor.ext.findColor


enum class OrderType(val typeServerByte: Int, val typeUI: String, val color: Int) {
    @SerializedName("buy")
    BUY(0, app.getString(R.string.my_orders_type_buy), findColor(R.color.submit400)),
    @SerializedName("sell")
    SELL(1, app.getString(R.string.my_orders_type_sell), findColor(R.color.error400));

    fun toBytes(): ByteArray {
        return byteArrayOf(typeServerByte.toByte())
    }

}
