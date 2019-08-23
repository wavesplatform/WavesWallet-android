/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import android.databinding.BaseObservable
import com.github.mikephil.charting.data.CombinedData
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.sdk.model.response.data.CandlesResponse

import java.util.ArrayList
import java.util.Date

class ChartModel : BaseObservable() {

    var candleList: List<CandlesResponse.Data.CandleResponse> = ArrayList()
    var data = CombinedData()
    var lastLoadDate: Date = Date()
    var pairModel: WatchMarketResponse? = null
}
