package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R
import pers.victor.ext.app
import pers.victor.ext.findColor

enum class OrderStatus(val status: String, val color: Int) {
    Accepted(app.getString(R.string.my_orders_status_open), findColor(R.color.submit400)),
    PartiallyFilled(app.getString(R.string.my_orders_status_partial_filled),findColor( R.color.submit400)),
    Cancelled(app.getString(R.string.my_orders_status_canceled), findColor(R.color.error400)),
    Filled(app.getString(R.string.my_orders_status_filled), findColor(R.color.submit400))
}
