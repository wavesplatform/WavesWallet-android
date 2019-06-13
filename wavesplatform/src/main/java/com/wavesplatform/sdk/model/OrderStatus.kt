/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model

import android.graphics.Color
import com.wavesplatform.sdk.R

enum class OrderStatus(val status: Int, val color: Int) {
    Accepted(R.string.my_orders_status_open, Color.parseColor("#1F5AF6")),
    PartiallyFilled(R.string.my_orders_status_partial_filled, Color.parseColor("#1F5AF6")),
    Cancelled(R.string.my_orders_status_canceled, Color.parseColor("#E5494D")),
    Filled(R.string.my_orders_status_filled, Color.parseColor("#E5494D"))
}
