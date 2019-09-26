/*
 * Created by Eduard Zaydel on 26/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarketResponse(var market: MarketResponse,
                          var pairResponse: SearchPairResponse.Pair.Data? = null) : Parcelable
