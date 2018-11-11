package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.wavesplatform.wallet.v1.payload.TickerMarket
import com.wavesplatform.wallet.v1.payload.TradesMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.PairResponse
import kotlinx.android.parcel.Parcelize

/**
 * Created by anonymous on 27.06.17.
 */

@Parcelize
class BuySellData(var watchMarket: WatchMarket? = null,
                  var orderType: Int? = null,
                  var initPrice: Long? = null,
                  var initAmount: Long? = null,
                  var lastPrice: Long? = null,
                  var askPrice: Long? = null,
                  var bidPrice: Long? = null) : Parcelable
