package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by anonymous on 27.06.17.
 */

@Parcelize
class BuySellData(
    var watchMarket: WatchMarket? = null,
    var orderType: Int? = null,
    var initPrice: Long? = null,
    var initAmount: Long? = null,
    var lastPrice: Long? = null,
    var askPrice: Long? = null,
    var bidPrice: Long? = null
) : Parcelable
