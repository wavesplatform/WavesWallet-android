package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.ColorRes

import com.wavesplatform.wallet.R

/**
 * Created by anonymous on 11.07.17.
 */

enum class OrderStatus private constructor(val status: String, @param:ColorRes @field:ColorRes val color: Int) {
    Accepted("Open", R.color.dex_orderbook_left_bg),
    PartiallyFilled("Partial", R.color.dex_orderbook_left_bg),
    Cancelled("Cancelled", R.color.dex_orderbook_right_bg),
    Filled("Filled", R.color.dex_orderbook_right_bg)
}
