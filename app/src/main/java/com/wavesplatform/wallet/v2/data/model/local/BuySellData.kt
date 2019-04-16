/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.wavesplatform.sdk.net.model.response.WatchMarketResponse
import kotlinx.android.parcel.Parcelize

/**
 * Created by anonymous on 27.06.17.
 */

@Parcelize
class BuySellData(
        var watchMarket: WatchMarketResponse? = null,
        var orderType: Int? = null,
        var initPrice: Long? = null,
        var initAmount: Long? = null,
        var initSum: Long? = null,
        var lastPrice: Long? = null,
        var askPrice: Long? = null,
        var bidPrice: Long? = null
) : Parcelable
