package com.wavesplatform.sdk.net.model

import android.os.Parcelable
import com.wavesplatform.sdk.net.model.response.MarketResponse
import com.wavesplatform.sdk.net.model.response.PairResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
class WatchMarket(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
