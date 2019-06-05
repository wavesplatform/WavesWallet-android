/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model

import com.wavesplatform.sdk.R
import pers.victor.ext.findColor

enum class OrderStatus(val status: Int, val color: Int) {
    Accepted(R.string.my_orders_status_open, findColor(R.color.submit400)),
    PartiallyFilled(R.string.my_orders_status_partial_filled, findColor(R.color.submit400)),
    Cancelled(R.string.my_orders_status_canceled, findColor(R.color.error400)),
    Filled(R.string.my_orders_status_filled, findColor(R.color.submit400))
}
