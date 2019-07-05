/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.PairResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarket(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
