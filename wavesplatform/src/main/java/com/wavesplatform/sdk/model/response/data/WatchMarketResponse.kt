/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response.data

import android.os.Parcelable
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarketResponse(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
