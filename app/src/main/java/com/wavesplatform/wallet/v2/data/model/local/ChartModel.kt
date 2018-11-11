package com.wavesplatform.wallet.v2.data.model.local

import android.databinding.BaseObservable

import com.github.mikephil.charting.data.CombinedData
import com.wavesplatform.wallet.v1.payload.Candle

import java.util.ArrayList
import java.util.Date

class ChartModel : BaseObservable() {

    var candleList: List<Candle> = ArrayList()
    var data = CombinedData()
    var lastLoadDate: Date = Date()
    var pairModel: WatchMarket? = null

}
