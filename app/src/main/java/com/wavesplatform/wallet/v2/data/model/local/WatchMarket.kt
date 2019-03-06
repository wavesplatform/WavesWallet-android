package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.PairResponse
import kotlinx.android.parcel.Parcelize

/**
 * Created by anonymous on 27.06.17.
 */

@Parcelize
class WatchMarket(var market: MarketResponse, var pairResponse: PairResponse? = null) : Parcelable
