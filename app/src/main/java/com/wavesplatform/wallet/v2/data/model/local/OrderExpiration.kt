package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R

enum class OrderExpiration(val timeUI: Int, val timeServer: Long) {
    FIVE_MINUTES(R.string.buy_and_sell_5_min, 5 * 60 * 1000),
    THIRTY_MINUTES(R.string.buy_and_sell_30_min, 30 * 60 * 1000),
    ONE_HOUR(R.string.buy_and_sell_1_hour, 60 * 60 * 1000),
    ONE_DAY(R.string.buy_and_sell_1_day, 24 * 60 * 60 * 1000),
    ONE_WEEK(R.string.buy_and_sell_1_week, 7 * 24 * 60 * 60 * 1000),
    ONE_MONTH(R.string.buy_and_sell_29_days, 29L * 24 * 60 * 60 * 1000)
}
