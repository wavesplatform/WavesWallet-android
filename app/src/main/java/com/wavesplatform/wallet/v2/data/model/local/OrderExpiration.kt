package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R
import pers.victor.ext.app
import pers.victor.ext.findColor


enum class OrderExpiration(val timeUI: String, val timeServer: Long) {
    FIVE_MINUTES(app.getString(R.string.buy_and_sell_5_min), 5*60*1000),
    THIRTY_MINUTES(app.getString(R.string.buy_and_sell_30_min), 30*60*1000),
    ONE_HOUR(app.getString(R.string.buy_and_sell_1_hour), 60*60*1000),
    ONE_DAY(app.getString(R.string.buy_and_sell_1_day), 24*60*60*1000),
    ONE_WEEK(app.getString(R.string.buy_and_sell_1_week), 7*24*60*60*1000),
    ONE_MONTH(app.getString(R.string.buy_and_sell_30_days), 30L*24*60*60*1000)
}
