package com.wavesplatform.sdk.model

import android.os.Parcelable
import com.wavesplatform.sdk.model.response.MarketResponse
import com.wavesplatform.sdk.model.response.PairResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarket(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
