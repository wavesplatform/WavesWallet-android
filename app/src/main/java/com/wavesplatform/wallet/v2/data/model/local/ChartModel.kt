package com.wavesplatform.wallet.v2.data.model.local

import android.databinding.BaseObservable

import com.github.mikephil.charting.data.CombinedData
import com.wavesplatform.wallet.v2.data.model.remote.response.CandlesResponse

import java.util.ArrayList
import java.util.Date

class ChartModel : BaseObservable() {

    var candleList: List<CandlesResponse.Candle> = ArrayList()
    var data = CombinedData()
    var lastLoadDate: Date = Date()
    var pairModel: WatchMarket? = null
}
