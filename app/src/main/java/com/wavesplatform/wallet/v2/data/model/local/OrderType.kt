package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R
import pers.victor.ext.app
import pers.victor.ext.findColor


enum class OrderType(val type: String, val color: Int) {
    BUY(app.getString(R.string.my_orders_type_buy), findColor(R.color.submit400)),
    SELL(app.getString(R.string.my_orders_type_sell), findColor(R.color.error400)),
}
