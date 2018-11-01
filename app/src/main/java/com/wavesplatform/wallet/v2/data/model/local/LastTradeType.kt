package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.ColorRes

import com.wavesplatform.wallet.R

enum class LastTradeType(val type: String, @param:ColorRes @field:ColorRes val color: Int) {
    BUY("buy", R.color.submit400),
    SELL("sell", R.color.error400),
}
