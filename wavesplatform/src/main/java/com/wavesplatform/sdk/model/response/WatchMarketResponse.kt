/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarketResponse(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
